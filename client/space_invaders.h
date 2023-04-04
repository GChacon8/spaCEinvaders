#ifndef SPACE_INVADERS_H
#define SPACE_INVADERS_H

#include <stdio.h>
#include <stddef.h>
#include <stdbool.h>

#include <X11/Xlib.h>

#include <SDL2/SDL.h>
#include <SDL2/SDL_ttf.h>
#include <SDL2/SDL_image.h>

#include <json-c/json_object.h>

#include "util.h"

/**
 * Represents the components of a sprite
 */
struct sprite
{
	SDL_Surface *surface;
	SDL_Texture *texture;
};

/**
 * Represents a numerical fraction
 */
struct ratio
{
	int      numerator;
	unsigned denominator;
};

/**
 * Represents an entity
 */
struct entity
{
	int          x;
	int          y;
	int          z;
	struct vec   sequence;
	size_t       next_sprite;
	struct ratio speed_x;
	struct ratio speed_y;
	bool         highlight;
};

/**
 * Represents a key-value pair
 */
struct key_value
{
	const char         *key;
	struct json_object *value;
};

/**
 * Describes the state of the client
 */
extern struct game
{
	/**
	 * Possible states in which a player can be found
	 */
	enum
	{
        GAME_STATE_HANDSHAKE_WHOAMI,
        GAME_STATE_HANDSHAKE_INIT,
        GAME_STATE_READY
	} state;

	/**
	 * Bitflags that indicate different options under which the client can operate.
	 */
	enum
	{
		GAME_FLAG_ZERO               = 0x00,
		GAME_FLAG_FULLSCREEN_MODESET = 0x01,
		GAME_FLAG_FULLSCREEN_FAKE    = 0x02,
		GAME_FLAG_SPECTATOR          = 0x04
	} flags;

	int               net_fd;
	int               x11_fd;
	int               timer_fd;
	FILE             *net_file;
	SDL_Window       *window;
	SDL_Renderer     *renderer;
	TTF_Font         *font;
	size_t            ticks;
	struct hash_map   sprites;
	struct hash_map   entities;
	struct sprite     stats_label;
	int               max_depth;
} game;

/**
 * Initialize game sprites
 * Load the images to be used in the game as sprites so that they can be associated later with a game entity
 */
void init_sprites(void);

/**
 * Initialize the game screen
 *
 * Based on a message from the server indicating the game's graphic parameters,
 * it creates a window under X11 and performs its initial configuration
 *
 * @param message server message containing parameters for the window to create
 */
void init_graphics(struct json_object *message);

/**
 * Starts a clock to keep track of elapsed time
 * Sets and initializes the game timer used to keep track of elapsed ticks
 */
void init_clock(void);

/**
 * Initialize the connection to the server
 * 
 * @param node ip address of the server
 * @param service port on which the server listens
 */
void init_net(const char *node, const char *service);

/**
 * Initializes SDL2 components
 */
void init_sdl(void);

/**
 * Redraw the game screen
 * Redraws all the game entities registered in the hash map of entities belonging to the game state
 */
void redraw(void);

/**
 * Control the general loop of the game once started
 */
void event_loop(void);

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
bool move_on_tick(int *coordinate, const struct ratio *speed);

/**
 * Updates the statistics label on the screen
 *
 * @param lives number of lives remaining
 * @param score score
 */
void update_stats(int lives, int score);

/**
 * Responsible for handling keypress events
 * 
 * Given a key press event, if said key is part of the game controls, it sends a message
 * to the server that communicates the type of event and the key that caused it, so that
 * the server can resolve the instructions to issue based on said event
 *
 * @param event pointer to the structure that describes the keypress event
 */
void handle_key(const SDL_KeyboardEvent *event);

/**
 * Reacts to a click (enumeration of IDs)
 * @param event mouse event
 */
void handle_click(const SDL_MouseButtonEvent *event);

/**
 * Issues the message that tells the server the client's mode of operation and which game the client wants to join
 *
 * Controls the startup routine of the client after receiving the initial message from the Server.
 * Given the state of the server, it gives certain options to the user to initialize a game
 *
 * @param message initial message sent from the Server to the client
 */
void start_or_watch_game(struct json_object *message);

/**
 * Send a message in the form of a JSON Object to the server
 * Writes a message as a JSON object to the file representing the output stream to the Server socket
 * @param items key-value pairs that make up the object to send
 */
void transmit(const struct key_value *items);

/**
 * Processes a message sent by the server
 *
 * Processes a message sent from the server, expects plain text that can describe a JSON object.
 * Attempt to parse the JSON object, and if the parse is successful, process the message as either
 * the initial handshake, the game start handshake, or as a command
 *
 * @param line message sent from the server as a string
 */
void receive(const char *line);

/**
 * Gets a key-value record of a JSON object and returns it as a separate JSON object
 *
 * @param parent JSON object containing the key-value pair
 * @param key key that identifies the pair
 * @param type data type stored in the pair value
 * @param required Indicates whether obtaining registration is required. If so, print an error to stdout
 * @return struct json_object* json object containing only the searched key-value pair
 */
struct json_object *expect_key
(
	struct json_object *parent, const char *key, enum json_type type, bool required
);

/**
 * Handles the closing of the game window
 * This function destroys/frees all resources in struct game
 * @param exit_code code that communicates the cause of the closing of the game window
 */
void quit(int exit_code);

/**
 * Writes to stdout an error found by SDL
 */
void sdl_fatal(void);

/**
 * Writes to stdout an SDL error associated with an image
 */
void sdl_image_fatal(void);

/**
 * Writes to stdout about an SDL error associated with text
 */
void sdl_ttf_fatal(void);

/**
 * Reports a fatal error status and closes the application immediately
 */
void sys_fatal(void);

/**
 * Communicate a farewell to the Server
 * Writes a JSON object that communicates a goodbye to the file representing the output stream to the socket server
 */
void bye(void);

#endif