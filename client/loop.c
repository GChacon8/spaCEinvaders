#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <assert.h>
#include <stdbool.h>

#include <poll.h>
#include <unistd.h>

#include <SDL2/SDL.h>
#include <SDL2/SDL_image.h>

#include <json-c/json_object.h>

#include "util.h"
#include "constants.h"
#include "space_invaders.h"

/**
 * Renders a texture at a position
 * @param sprite
 * @param x
 * @param y
 */
static void render(const struct sprite *sprite, int x, int y)
{
	// Bounds are derived from positions and sizes
	struct SDL_Rect destination =
	{
		.x = x,
		.y = y,
		.w = sprite->surface->w,
		.h = sprite->surface->h
	};

	if(SDL_RenderCopy(game.renderer, sprite->texture, NULL, &destination) < 0)
	{
		sdl_fatal();
	}
}

/**
 * Renders an entity at its position
 * @param entity
 * @param sprite
 */
static void render_entity(const struct entity *entity, const struct sprite *sprite)
{
	render(sprite, entity->x, entity->y);

	// Highlighted if necessary
	if(entity->highlight)
	{
		struct SDL_Rect area =
		{
			.x = entity->x,
			.y = entity->y,
			.w = sprite->surface->w,
			.h = sprite->surface->h
		};

		if(SDL_SetRenderDrawColor(game.renderer, COLOR_WHITE, COLOR_WHITE, COLOR_WHITE, ALPHA_HIGHLIGHT) < 0
		|| SDL_RenderFillRect(game.renderer, &area) < 0
		|| SDL_SetRenderDrawColor(game.renderer, COLOR_BLACK, COLOR_BLACK, COLOR_BLACK, SDL_ALPHA_OPAQUE) < 0)
		{
			sdl_fatal();
		}
	}
}

/**
 * Redraw the game screen
 * Redraws all game entities registered in the hashmap of entities belonging to the game state
 */
void redraw(void)
{
	// The screen is cleaned before each frame
	if(SDL_RenderClear(game.renderer) < 0)
	{
		sdl_fatal();
	}

	// Each of the 'layers' is covered, going through all the entities in each iteration
	for(int depth = 0; depth <= game.max_depth; ++depth)
	{
		for(struct hash_map_iter iter = hash_map_iter(&game.entities); iter.cell; hash_map_iter_next(&iter))
		{
			int id = hash_map_iter_key(&iter);
			struct entity *entity = hash_map_iter_value(&iter);

			if(entity->z != depth)
			{
				continue;
			}

			// Avoid short-circuit logic
			bool moved = move_on_tick(&entity->x, &entity->speed_x);
			moved = move_on_tick(&entity->y, &entity->speed_y) || moved;

			int sprite_id = *(int*)vec_get(&entity->sequence, entity->next_sprite);
			struct sprite *sprite = hash_map_get(&game.sprites, sprite_id);
			assert(sprite);

			// Animation loop
			if(moved && ++entity->next_sprite == entity->sequence.length)
			{
				entity->next_sprite = 0;
			}

			render_entity(entity, sprite);

			// This is an optimization, the server would ignore a viewer message anyway
			if(!(game.flags & GAME_FLAG_SPECTATOR) && moved)
			{
				struct key_value items[] =
				{
					{CMD_OP, json_object_new_string(CMD_MOVE)},
					{CMD_ID, json_object_new_int(id)},
					{CMD_X,  json_object_new_int(entity->x)},
					{CMD_Y,  json_object_new_int(entity->y)},
					{NULL,   NULL}
				};

				transmit(items);
			}
		}
	}

	// Statistics label
	if(game.stats_label.texture)
	{
		render(&game.stats_label, STATS_LABEL_X, STATS_LABEL_Y);
	}

	SDL_RenderPresent(game.renderer);
}

/**
 * Add an event to the SDL event queue
 * @param type integer identifying the type of event to add
 */
static void push_sdl_event(int type)
{
	SDL_Event event = { .type = type };
	if(SDL_PushEvent(&event) != 1)
	{
		sdl_fatal();
	}
}

/**
 * Control the general loop of the game once started
 */
void event_loop(void)
{
	// File descriptors to be polled
	struct pollfd pollfds[] =
	{
		{
			.fd = game.net_fd,
			.events = POLLIN
		},
		{
			.fd = FD_INVALID,
			.events = POLLIN
		},
		{
			.fd = FD_INVALID,
			.events = POLLIN
		}
	};

	struct pollfd *net_pollfd = &pollfds[0];
	struct pollfd *x11_pollfd = &pollfds[1];
	struct pollfd *timer_pollfd = &pollfds[2];

	char input_line[MAX_INPUT_LINE_SIZE];
	size_t input_offset = 0;

	// Main loop of events
	while(true)
	{
		SDL_Event event;
		while(SDL_PollEvent(&event) > 0)
		{
			switch(event.type)
			{
				case SDL_QUIT:
					bye();
					return;

				case SDL_KEYUP:
				case SDL_KEYDOWN:
					handle_key(&event.key);
					break;

				case SDL_MOUSEBUTTONDOWN:
					handle_click(&event.button);
					break;

				case X11_EVENT:
					errno = 0;
					while(fgets(input_line + input_offset, sizeof input_line - input_offset, game.net_file))
					{
						// This happens if there is a short read condition
						if(!strchr(input_line + input_offset, NEW_LINE))
						{
							input_offset += strlen(input_line + input_offset);
							if(input_offset == sizeof input_line - 1)
							{
								fprintf(stderr, "Error: buffer overflow detected: %s\n", input_line);
								quit(EXIT_FAILURE);
							}

							// And this case is equivalent to an EAGAIN for non-blocking I/O
							errno = EAGAIN;
							break;
						}

						receive(input_line);
						input_offset = 0;
					}

					// EAGAIN indicates missing data, wait in loop again
					if(errno == EAGAIN)
					{
						clearerr(game.net_file);
						continue;
					} else if(feof(game.net_file))
					{
						puts("The server has closed the connection");
						return;
					} else
					{
						sys_fatal();
					}

					break;

				case TIMER_EVENT:
				{
					// See the timerfd man page
					uint64_t expirations = 0;
					read(game.timer_fd, &expirations, sizeof expirations);

					// Unexpected time lapse
					if(expirations > 1)
					{
						fprintf(stderr, "Warning: %lu clock tick(s) missed\n", expirations - 1);
					}

					++game.ticks;
					redraw();

					break;
				}
			}
		}

		// These two can change during execution
		x11_pollfd->fd = game.x11_fd;
		timer_pollfd->fd = game.timer_fd;

		// This is where you actually wait for an event
		if(poll(pollfds, sizeof pollfds / sizeof(struct pollfd), -1) < 0 && errno != EINTR)
		{
			sys_fatal();
		}

		// If there are exit events, it is added to the queue
		if(net_pollfd->revents)
		{
			push_sdl_event(X11_EVENT);
		}

		// Same but for the timer
		if(timer_pollfd->revents)
		{
			push_sdl_event(TIMER_EVENT);
		}
	}
}