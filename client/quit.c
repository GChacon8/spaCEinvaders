#include <stdio.h>
#include <stdlib.h>

#include <unistd.h>

#include <SDL2/SDL.h>
#include <SDL2/SDL_ttf.h>
#include <SDL2/SDL_image.h>

#include <json-c/json_object.h>

#include "util.h"
#include "constants.h"
#include "space_invaders.h"

/**
 * Handles the closing of the game window
 * This function destroys/frees all resources in struct game
 * @param exit_code code that communicates the cause of the closing of the game window
 */
void quit(int exit_code)
{
	// Clock
	close(game.timer_fd);

	// Socket
	if(game.net_file)
	{
		fclose(game.net_file);
	}

	// Window
	if(game.window)
	{
		SDL_DestroyWindow(game.window);
	}

	// Entity sequences
	for(struct hash_map_iter iter = hash_map_iter(&game.entities); iter.cell; hash_map_iter_next(&iter))
	{
		struct entity *entity = hash_map_iter_value(&iter);
		vec_clear(&entity->sequence);
	}

	// Sprite textures
	for(struct hash_map_iter iter = hash_map_iter(&game.sprites); iter.cell; hash_map_iter_next(&iter))
	{
		struct sprite *sprite = hash_map_iter_value(&iter);
		SDL_DestroyTexture(sprite->texture);
		SDL_FreeSurface(sprite->surface);
	}

	// Maps
	hash_map_clear(&game.sprites);
	hash_map_clear(&game.entities);

	// Label text
	if(game.stats_label.texture)
	{
		SDL_DestroyTexture(game.stats_label.texture);
		SDL_FreeSurface(game.stats_label.surface);
	}

	// Font
	if(game.font)
	{
		TTF_CloseFont(game.font);
	}

	// All libraries are des-initialized
	TTF_Quit();
	IMG_Quit();
	SDL_Quit();

	exit(exit_code);
}

/**
 * Writes to stdout an error found by SDL
 */
void sdl_fatal(void)
{
	fprintf(stderr, "Fatal SDL error: %s\n", SDL_GetError());
	quit(EXIT_FAILURE);
}

/**
 * Writes to stdout an SDL error associated with an image
 */
void sdl_image_fatal(void)
{
	fprintf(stderr, "Fatal SDL_image error: %s\n", IMG_GetError());
	quit(EXIT_FAILURE);
}

/**
 * Writes to stdout about an SDL error associated with text
 */
void sdl_ttf_fatal(void)
{
	fprintf(stderr, "Fatal SDL_ttf error: %s\n", TTF_GetError());
	quit(EXIT_FAILURE);
}

/**
 * Reports a fatal error status and closes the application immediately
 */
void sys_fatal(void)
{
	perror("Fatal error");
	quit(EXIT_FAILURE);
}

/**
 * Communicate a farewell to the Server
 * Writes a JSON object that communicates a goodbye to the file representing the output stream to the socket server
 */
void bye(void)
{
	struct key_value items[] =
	{
		{CMD_OP, json_object_new_string(CMD_BYE)},
		{NULL,   NULL}
	};

	transmit(items);
}