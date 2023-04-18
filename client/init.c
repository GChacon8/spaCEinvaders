#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include <glob.h>
#include <fcntl.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <sys/timerfd.h>

#include <X11/Xlib.h>

#include <SDL2/SDL.h>
#include <SDL2/SDL_ttf.h>
#include <SDL2/SDL_image.h>
#include <SDL2/SDL_syswm.h>

#include <json-c/json_object.h>

#include "util.h"
#include "constants.h"
#include "space_invaders.h"

/**
 * Initialize the game sprites
 * Load the images to be used in the game as sprites so that they can be associated later with a game entity
 */
void init_sprites(void)
{
	glob_t paths = { 0 };
	if(glob("../../assets/sprites/*/\?\?-*.png", GLOB_FLAGS, NULL, &paths) != 0)
	{
		fprintf(stderr, "Error: sprite glob failed (bad cwd?)\n");
		quit(EXIT_FAILURE);
	}

	// Search for each of the possible sprites
	for(char **path = paths.gl_pathv; *path; ++path)
	{
		// It's about matching the filename pattern with the sprite id
		int sprite_id;
		if(sscanf(strrchr(*path, PATH_SEPARATOR), SPRITE_PATH_PATTERN, &sprite_id) != 1)
		{
			fprintf(stderr, "Error: bad sprite path (expected ../NN-*.png): %s\n", *path);
			quit(EXIT_FAILURE);
		}

		// This file is now translated into a usable texture
		SDL_Surface *surface = IMG_Load(*path);
		if(!surface)
		{
			sdl_image_fatal();
		}

		SDL_Texture *texture = SDL_CreateTextureFromSurface(game.renderer, surface);
		if(!texture)
		{
			sdl_fatal();
		}

		// Finally, it is added to the known textures
		struct sprite sprite =
		{
			.surface = surface,
			.texture = texture
		};

		hash_map_put(&game.sprites, sprite_id, &sprite);
	}

	globfree(&paths);
}

/**
 * Initialize the game screen
 *
 * Based on a message from the server indicating the game's graphic parameters,
 * it creates a window under X11 and performs its initial configuration
 * @param message server message containing parameters for the window to create
 */
void init_graphics(struct json_object *message)
{
	// The server indicates the dimensions of the playing area
	int width = json_object_get_int(expect_key(message, CMD_WIDTH, json_type_int, true));
	int height = json_object_get_int(expect_key(message, CMD_HEIGHT, json_type_int, true));

	// A precondition is that this has not been done before.
	assert(!game.window && !game.renderer);

	// screen size information
	SDL_SysWMinfo wm_info;
	SDL_VERSION(&wm_info.version);

	// The window is created and graphic options are adjusted
	if(SDL_CreateWindowAndRenderer(width, height, 0, &game.window, &game.renderer) != 0
	|| SDL_SetRenderDrawBlendMode(game.renderer, SDL_BLENDMODE_BLEND) < 0
	|| !SDL_GetWindowWMInfo(game.window, &wm_info))
	{
		sdl_fatal();
	} else if(wm_info.subsystem != SDL_SYSWM_X11)
	{
		fputs("Error: requires X11\n", stderr);
		quit(EXIT_FAILURE);
	}

	// The X11 descriptor file is used to have our own loop instead of SDL's
	game.x11_fd = XConnectionNumber(wm_info.info.x11.display);
	SDL_SetWindowTitle(game.window, "SpaCEInvaders.");

	// Regular vs fullscreen window cases
	if(!(game.flags & GAME_FLAG_FULLSCREEN_MODESET) && !(game.flags & GAME_FLAG_FULLSCREEN_FAKE))
	{
		SDL_SetWindowPosition(game.window, SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED); // center the window
	} else
	{
		SDL_DisplayMode display_mode;
		int mode = (game.flags & GAME_FLAG_FULLSCREEN_FAKE) ? SDL_WINDOW_FULLSCREEN_DESKTOP : SDL_WINDOW_FULLSCREEN;

		if(SDL_SetWindowFullscreen(game.window, mode) != 0
		|| SDL_GetCurrentDisplayMode(0, &display_mode) != 0
		|| SDL_RenderSetScale(game.renderer, (float)display_mode.w / width, (float)display_mode.h / height) != 0)
		{
			sdl_fatal();
		}
	}
}

/**
 * Starts a clock to keep track of elapsed time
 * Sets and initializes the game timer used to keep track of elapsed ticks
 */
void init_clock(void)
{
	struct timespec timer_period =
	{
		.tv_sec  = 0,
		.tv_nsec = NANOS_PER_TICK
	};

	// Both initial and recurring aspects of expiration
	struct itimerspec timer_expiration =
	{
		.it_interval = timer_period,
		.it_value    = timer_period
	};

	// The clock is created and initialized
	if((game.timer_fd = timerfd_create(CLOCK_MONOTONIC, TFD_NONBLOCK | TFD_CLOEXEC)) == -1
	|| timerfd_settime(game.timer_fd, 0, &timer_expiration, NULL) != 0)
	{
		sys_fatal();
	}
}

/**
 * Initialize the connection to the server
 * @param node ip address of the server
 * @param service port on which the server listens
 */
void init_net(const char *node, const char *service)
{
	struct addrinfo *server_addrinfo = NULL;

	// Resolves the host/port pair to a usable structure
	int addrinfo_result = getaddrinfo(node, service, NULL, &server_addrinfo);
	if(addrinfo_result != 0)
	{
		fprintf(stderr, "Network lookup error: %s\n", gai_strerror(addrinfo_result));
		quit(EXIT_FAILURE);
	}

	// TCP socket
	game.net_fd = socket(server_addrinfo->ai_family, SOCK_STREAM, 0);

	// Try to connect to the server
	if(game.net_fd < 0
	|| connect(game.net_fd, server_addrinfo->ai_addr, server_addrinfo->ai_addrlen) < 0
	// Since there is no exact order of events between server and user, it must be non-blocking.
	|| fcntl(game.net_fd, F_SETFL, O_NONBLOCK) < 0)
	{
		sys_fatal();
	}

	freeaddrinfo(server_addrinfo);

	// A FILE* is created from this fd, for easy use of stdio.h instead of unistd.h
	game.net_file = fdopen(game.net_fd, F_OPEN_MODE_APPEND);
	assert(game.net_file);
}

/**
 * Initializes SDL2 components
 */
void init_sdl(void)
{
	if(SDL_Init(SDL_INIT_VIDEO) != 0)
	{
		sdl_fatal();
	} else if(IMG_Init(IMG_INIT_PNG) != IMG_INIT_PNG)
	{
		sdl_image_fatal();
	} else if(TTF_Init() != 0 || !(game.font = TTF_OpenFont("../../assets/arcade_n.ttf", FONT_POINT_SIZE)))
	{
		sdl_ttf_fatal();
	}
}