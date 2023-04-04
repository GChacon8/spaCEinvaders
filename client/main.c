#include <stdio.h>
#include <stdlib.h>

#include <getopt.h>

#include "constants.h"
#include "space_invaders.h"

/**
 * Write an error message in response to misuse of the executable
 * If the command line parameters are wrong, communicate that the given
 * command can be run with a "--help" flag to request more information
 * @param argv0 command executed improperly
 */
static void usage(const char *argv0)
{
	fprintf(stderr, "Run '%s --help' for more information\n", argv0);
}

/**
 * Program entry point
 * Handles the game startup routine and initializes the game after the game is finished
 * @param argc number of command line arguments
 * @param argv array containing the command line arguments
 * @return int exit code indicating execution result
 */
int main(int argc, char *argv[])
{
	// First the command line is parsed
	const struct option CMDLINE_OPTIONS[] =
	{
		{CMDLINE_HELP,            no_argument, NULL, CMDLINE_OPT_HELP},
		{CMDLINE_VERSION,         no_argument, NULL, CMDLINE_OPT_VERSION},
		{CMDLINE_FULLSCREEN,      no_argument, NULL, CMDLINE_OPT_FULLSCREEN},
		{CMDLINE_FULLSCREEN_FAKE, no_argument, NULL, CMDLINE_OPT_FULLSCREEN_FAKE},
		{NULL,                    0,           NULL, 0}
	};

	game.sprites = hash_map_new(DEFAULT_MAP_ORDER, sizeof(struct sprite));
	game.entities = hash_map_new(DEFAULT_MAP_ORDER, sizeof(struct entity));

	int option;
	while((option = getopt_long(argc, argv, CMDLINE_ALL_SHORTS, CMDLINE_OPTIONS, NULL)) != -1)
	{
		switch(option)
		{
			case CMDLINE_OPT_HELP:
				fprintf
				(
					stderr,
					"Usage: %s [OPTION]... <host> <port>\n"
					"\n"
					"    -f|--fullscreen       Enters fullscreen through Kernel Mode Setting\n"
					"    -F|--fake-fullscreen  Displays a maximized and borderless X11 window\n",
					argv[0]
				);

				return 0;

			case CMDLINE_OPT_VERSION:
				puts("SpaCEInvaders. v1.0.0");
				return 0;

			case CMDLINE_OPT_FULLSCREEN:
				game.flags |= GAME_FLAG_FULLSCREEN_MODESET;
				break;

			case CMDLINE_OPT_FULLSCREEN_FAKE:
				game.flags |= GAME_FLAG_FULLSCREEN_FAKE;
				break;

			case '?':
				usage(argv[0]);
				return EXIT_FAILURE;
		}
	}

	if(argc - optind != 2)
	{
		fprintf(stderr, "%s: missing host or port\n", argv[0]);
		usage(argv[0]);

		return EXIT_FAILURE;
	}

	// Start of the gameStart of the game
	init_sdl();
	init_net(argv[optind], argv[optind + 1]);

	event_loop();
	quit(EXIT_SUCCESS);
}