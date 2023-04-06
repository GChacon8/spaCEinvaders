#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <assert.h>
#include <stdbool.h>
#include <inttypes.h>

#include <glob.h>
#include <poll.h>
#include <fcntl.h>
#include <netdb.h>
#include <unistd.h>
#include <getopt.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <sys/timerfd.h>

#include <X11/Xlib.h>

#include <SDL2/SDL.h>
#include <SDL2/SDL_image.h>
#include <SDL2/SDL_syswm.h>

#include <json-c/json_object.h>
#include <json-c/json_tokener.h>

#include "util.h"
#include "constants.h"
#include "space_invaders.h"

/**
 * Describes the state of the client. The values put here are the initial ones
 */
struct game game =
{
	.state       = GAME_STATE_HANDSHAKE_WHOAMI,
	.flags       = GAME_FLAG_ZERO,
	.net_fd      = FD_INVALID,
	.x11_fd      = FD_INVALID,
	.timer_fd    = FD_INVALID,
	.net_file    = NULL,
	.window      = NULL,
	.renderer    = NULL,
	.font        = NULL,
	.ticks       = 0,
	.stats_label = { .texture = NULL, .surface = NULL },
	.max_depth   = 0
};

/**
 * Moves an entity, given the correct number of ticks elapsed
 * 
 * Given a coordinate and the speed (amount of movement/ticks) at that coordinate of an entity,
 * decide whether or not that entity should move. If the number of ticks is a factor of the
 * velocity denominator, move the entity the number of units of length specified by the velocity
 * numerator. Returns a boolean value that indicates if there was movement or not
 * 
 * @param coordinate pointer to a coordinate axis field of a feature (x or y)
 * @param speed fraction of velocity expressed as (amount of movement/ticks)
 * @return true entity moved on this tick
 * @return false entity did not move at this tick
 */
bool move_on_tick(int *coordinate, const struct ratio *speed)
{
	// The numerator is a distance and the denominator is a time in ticks
	if(speed->denominator > 0 && game.ticks % speed->denominator == 0)
	{
		// Each speed->denominator ticks the numerator is added
		int jump = abs(speed->numerator);
		jump = jump > 0 ? jump : JUMP_DEFAULT; // Case 0/n
		jump = speed->numerator > 0 ? jump : -jump;

		*coordinate += jump;
		return true;
	}

	return false;
}

/**
 * Updates the statistics label on the screen
 * @param lives number of lives remaining
 * @param score score
 */
void update_stats(int lives, int score)
{
	// The old label is released
	if(game.stats_label.texture)
	{
		SDL_DestroyTexture(game.stats_label.texture);
		SDL_FreeSurface(game.stats_label.surface);
	}

	// The label text is generated and rendered
	char text[STATS_LABEL_MAX_CHARS];
	snprintf(text, sizeof text, STATS_LABEL_FORMAT, score, lives);

	SDL_Color fg = STATS_LABEL_COLOR;
	SDL_Color bg = { .r = COLOR_BLACK, .g = COLOR_BLACK, .b = COLOR_BLACK, .a = COLOR_BLACK };
	if(!(game.stats_label.surface = TTF_RenderUTF8_Shaded(game.font, text, fg, bg)))
	{
		sdl_ttf_fatal();
	} else if(!(game.stats_label.texture = SDL_CreateTextureFromSurface(game.renderer, game.stats_label.surface)))
	{
		sdl_fatal();
	}
}

/**
 * Responsible for handling keypress events
 * 
 * Given a key press event, if said key is part of the game controls, it sends a message
 * to the server that communicates the type of event and the key that caused it, so that
 * the server can resolve the instructions to issue based on said event
 *
 * @param event pointer to the structure that describes the keypress event
 */
void handle_key(const SDL_KeyboardEvent *event)
{
	// Only the moment of pressure is handled, not repetitions
	if(event->repeat > 0)
	{
		return;
	}

	const char *operation = event->state == SDL_PRESSED ? CMD_PRESS : CMD_RELEASE;
	const char *key = CMD_UNKNOWN;

	switch(event->keysym.sym)
	{
		case SDLK_LEFT:
		case SDLK_a:
			key = KEY_LEFT;
			break;

		case SDLK_RIGHT:
		case SDLK_d:
			key = KEY_RIGHT;
			break;

		case SDLK_SPACE:
        case SDLK_w:
			key = KEY_SHOOT;
			break;

		default:
			return;
	}

	struct key_value items[] =
	{
		{CMD_OP,  json_object_new_string(operation)},
		{CMD_KEY, json_object_new_string(key)},
		{NULL,    NULL}
	};

	transmit(items);
}

/**
 * Reacts to a click (enumeration of IDs)
 * @param event mouse event
 */
void handle_click(const SDL_MouseButtonEvent *event)
{
	// Only left clicks are accepted
	if(event->button != SDL_BUTTON_LEFT)
	{
		return;
	}

	// In fullscreen the scale change
	float scale_x;
	float scale_y;
	SDL_RenderGetScale(game.renderer, &scale_x, &scale_y);

	// Adjusts according to scaleAdjusts according to scale
	int click_x = event->x / scale_x;
	int click_y = event->y / scale_y;

	bool found = false;
	for(struct hash_map_iter iter = hash_map_iter(&game.entities); iter.cell; hash_map_iter_next(&iter))
	{
		int id = hash_map_iter_key(&iter);
		struct entity *entity = hash_map_iter_value(&iter);
		struct sprite *sprite = hash_map_get(&game.sprites, *(int*)vec_get(&entity->sequence, entity->next_sprite));

		if(click_x < entity->x || click_x >= entity->x + sprite->surface->w
		|| click_y < entity->y || click_y >= entity->y + sprite->surface->h)
		{
            // Outside the entity area
			continue;
		}

		if(!found)
		{
			found = true;
			printf("Click at (%d, %d) matches these IDs: %d", click_x, click_y, id);
		} else
		{
			printf(", %d", id);
		}
	}

	found ? putchar(NEW_LINE) : printf("No IDs match click at (%d, %d)\n", click_x, click_y);
}

/**
 * Ask the user for a game id to initialize the client
 * 
 * Given a client id and a series of id's identifying active games, the user is asked for
 * the game id they want to join. If the user types an id registered as a game, the user
 * joins an already active game as a spectator. If you put your own id, try to start a game
 * as a player. Given an invalid input, it will raise an error and close the game
 *
 * @param client_id id assigned to the current client
 * @param games JSON object containing the list of active games
 * @return int32_t game id associated with the client
 */
static int32_t select_game(int32_t client_id, struct json_object *games)
{
	for(size_t i = 0; i < json_object_array_length(games); ++i)
	{
		struct json_object *game = json_object_array_get_idx(games, i);
		if(json_object_get_type(game) != json_type_int)
		{
			fprintf(stderr, "Error: 'games[%zu]' is not an integer\n", i);
			quit(EXIT_FAILURE);
		}

		printf("- Game %d is running\n", json_object_get_int(game));
	}

	putchar(NEW_LINE);

	int32_t game_id;
	while(true)
	{
		printf("Enter a game ID to watch, or %d to start a new game\n> ", client_id);

		// SCNd32 is required because it is int32_t
		int scanned = scanf(" %" SCNd32, &game_id);
		if(scanned == EOF)
		{
			bye(); // You don't want to start a game, it closes without error
			quit(EXIT_SUCCESS);
		}

		// The rest of the input line is read and discarded
		while(getchar() != NEW_LINE)
		{
			continue;
		}

		bool valid = scanned == 1 && game_id == client_id;
		if(scanned == 1 && !valid)
		{
			for(size_t i = 0; i < json_object_array_length(games); ++i) //
			{
				if(json_object_get_int(json_object_array_get_idx(games, i)) == game_id)
				{
					game.flags |= GAME_FLAG_SPECTATOR;
					puts("This client is a spectator");

					valid = true;
					break;
				}
			}
		}

		if(valid)
		{
			break;
		}

		fputs("Error: bad input\n", stderr);
	}

	return game_id;
}

/**
 * Issues the message that tells the server the client's mode of operation and which game the client wants to join
 *
 * Controls the startup routine of the client after receiving the initial message from the Server.
 * Given the state of the server, it gives certain options to the user to initialize a game
 *
 * @param message initial message sent from the Server to the client
 */
void start_or_watch_game(struct json_object *message)
{
	int32_t client_id = json_object_get_int(expect_key(message, CMD_WHOAMI, json_type_int, true));
	struct json_object *games = expect_key(message, CMD_GAMES, json_type_array, true);

	// PRId32 is handled by being int32_t
	printf("This is client %" PRId32 "\n", client_id);

	int32_t game_id;
	if(json_object_array_length(games) == 0)
	{
		printf("No games are currently running, starting game %d...\n", client_id);
		game_id = client_id;
	} else
	{
		game_id = select_game(client_id, games);
	}

	struct key_value items[] =
	{
		{CMD_INIT, json_object_new_int(game_id)},
		{NULL,     NULL}
	};

	transmit(items);
}