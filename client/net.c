#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

#include <json-c/json_object.h>
#include <json-c/json_tokener.h>

#include "util.h"
#include "constants.h"
#include "space_invaders.h"

/**
 * Send a message in the form of a JSON Object to the server
 * Writes a message as a JSON object to the file representing the output stream to the Server socket
 * @param items key-value pairs that make up the object to send
 */
void transmit(const struct key_value *items)
{
	// Translate key-value structures to a JSON object
	struct json_object *root = json_object_new_object();
	for(; items->key; ++items)
	{
		json_object_object_add(root, items->key, items->value);
	}

	// This JSON object is sent (remember that net_file arose from an fdopen())
	fprintf(game.net_file, "%s\n", json_object_to_json_string(root));
	fflush(game.net_file);

	// Memory is freed
	json_object_put(root);
}

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
)
{
	struct json_object *value = json_object_object_get(parent, key); 
	if(value)
	{
		if(json_object_get_type(value) != type)
		{
			fprintf(stderr, "Error: mismatched JSON value type for key '%s'\n", key);
			quit(EXIT_FAILURE);
		}
	} else if(required)
	{
		fprintf(stderr, "Error: expected JSON key '%s'\n", key);
		quit(EXIT_FAILURE);
	}

	return value;
}

/**
 * Gets the value of the id field of the given JSON object
 * Given a JSON object, extract the value associated with the "id" key. Used to process messages from the server
 * @param message JSON object from which the id value will be extracted. It's a server message
 * @return int id value extracted from JSON message
 */
static int expect_id(struct json_object *message)
{
	return json_object_get_int(expect_key(message, CMD_ID, json_type_int, true));
}

/**
 * Gets the entity referenced by an id contained in a message in JSON format
 *
 * Given a message in JSON format that refers to an entity, extracts the id value of the message
 * and uses that value to get a pointer to the entity identified by that id
 *
 * @param message server message referring to the entity to be obtained
 * @return struct entity* pointer to the entity identified by the given identifier
 */
static struct entity *expect_entity(struct json_object *message)
{
	int id = expect_id(message);

	struct entity *entity = hash_map_get(&game.entities, id);
	if(!entity)
	{
		fprintf(stderr, "Error: no entity has ID %d\n", id);
		quit(EXIT_FAILURE);
	}

	return entity;
}

/**
 * Extract a mathematical reason from a message in JSON format
 * 
 * Given a message in JSON format that contains information regarding a mathematical
 * ratio, it extracts the information of the nominator and denominator of said mathematical
 * ratio from the object, and creates a struct ratio value based on said information
 *
 * @param message JSON object containing the fields to be extracted
 * @param num_key string of characters used as the key of the value that identifies the numerator
 * @param denom_key string of characters used as the key of the value that identifies the denominator
 * @return struct ratio mathematical reason extracted from the JSON object
 */
static struct ratio expect_ratio(struct json_object *message, const char *num_key, const char *denom_key)
{
	int num = json_object_get_int(expect_key(message, num_key, json_type_int, true));
	int denom = json_object_get_int(expect_key(message, denom_key, json_type_int, true));

	// 0/0 is accepted for the case of a static entity
	if(denom < 0 || (num != 0 && denom == 0))
	{
		fprintf(stderr, "Error: bad speed ratio: %d:%d\n", num, denom);
		quit(EXIT_FAILURE);
	}

	struct ratio ratio =
	{
		.numerator   = num,
		.denominator = denom
	};

	return ratio;
}

/**
 * Extract a sequence of sprite id's from a message in JSON format
 * 
 * Given a message in JSON format that contains a field with a "seq" key that has as its
 * associated value a JSON array of members of type integer, it extracts said array and loads it onto a vector
 *
 * @param message JSON object containing the sequence array to be extracted
 * @param sequence output parameter that takes the value of the sequence of ID's of sprites
 */
static void expect_sequence(struct json_object *message, struct vec *sequence)
{
	struct json_object *sequence_ids = expect_key(message, CMD_SEQUENCE, json_type_array, true);
	if(json_object_array_length(sequence_ids) == 0)
	{
		fputs("Error: empty sequence array\n", stderr);
	}

	// Loop through the json array
	for(size_t i = 0; i < json_object_array_length(sequence_ids); ++i)
	{
		struct json_object *id_object = json_object_array_get_idx(sequence_ids, i);
		if(json_object_get_type(id_object) != json_type_int) 
		{
			fputs("Error: expected int in sequence array\n", stderr);
			quit(EXIT_FAILURE);// Fail if an integer value is not obtained
		}

		int id = json_object_get_int(id_object);
		if(!hash_map_get(&game.sprites, id))
		{
			fprintf(stderr, "Error: no sprite has ID %d\n", id);
			quit(EXIT_FAILURE);
		}

		// Add element to return vector
		*(int*)vec_emplace(sequence) = id;
	}
}

/**
 * @brief Extract vertical and horizontal position values from a JSON object
 *
 * Given a message in JSON format coming from the server, it obtains the values
 * associated to the "x" and "y" keys, which refer to screen positions
 *
 * @param message Message in JSON format coming from the server
 * @param x return parameter in which the extracted horizontal position value is stored
 * @param y Return parameter in which the extracted vertical position value is stored
 */
static void expect_position(struct json_object *message, int *x, int *y)
{
	*x = json_object_get_int(expect_key(message, CMD_X, json_type_int, true));
	*y = json_object_get_int(expect_key(message, CMD_Y, json_type_int, true));
}

/**
 * Handle commands coming from the server
 * 
 * Given a message in JSON format coming from the server, it analyzes it and determines
 * the actions to take to carry out what is specified by the command. Commands are
 * identified by the value associated with the "op" key in the message. If that
 * key-value pair is not found in the message or contains an invalid command, stop program execution
 *
 * @param message message in JSON format coming from the server
 */
static void handle_command(struct json_object *message)
{
	// Main dispatch of commands coming from the server
	const char *operation = json_object_get_string(expect_key(message, CMD_OP, json_type_string, true));
	if(strcmp(operation, CMD_PUT) == 0) // command to create entities
	{
		int id = expect_id(message);

		// A new entity is created (assuming it does not exist)
		struct entity new = { 0 };
		struct entity *existing = hash_map_get(&game.entities, id);
		struct entity *entity = existing ? existing : &new;

		// If it does exist, it is simply overwritten
		if(existing)
		{
			vec_resize(&existing->sequence, 0);
		} else
		{
			new.sequence = vec_new(sizeof(int));
			entity = hash_map_put(&game.entities, id, &new);
		}

		// Other values included in the message are inserted
		entity->next_sprite = 0;
		expect_position(message, &entity->x, &entity->y);
		expect_sequence(message, &entity->sequence);

		// Depth
		entity->z = json_object_get_int(expect_key(message, CMD_Z, json_type_int, true));
		if(entity->z > game.max_depth)
		{
			game.max_depth = entity->z;
		}

		entity->speed_x = expect_ratio(message, "num_x", "denom_x");
		entity->speed_y = expect_ratio(message, "num_y", "denom_y");
	} else if(strcmp(operation, CMD_MOVE) == 0) //command to move an entity
	{
		struct entity *entity = expect_entity(message);
		expect_position(message, &entity->x, &entity->y);
	} else if(strcmp(operation, CMD_DELETE) == 0) //command to delete an entity
	{
		int id = expect_id(message);

		struct entity *entity = hash_map_get(&game.entities, id);
		if(entity)
		{
			vec_clear(&entity->sequence);
			hash_map_delete(&game.entities, id);
		}
	} else if(strcmp(operation, CMD_STATS) == 0) // Statistics update
	{
		int lives = json_object_get_int(expect_key(message, CMD_LIVES, json_type_int, true));
		int score = json_object_get_int(expect_key(message, CMD_SCORE, json_type_int, true));

		update_stats(lives, score);
	} else if(strcmp(operation, CMD_HIGHLIGHT) == 0) // guide highlight
	{
		expect_entity(message)->highlight = true;
	} else if(strcmp(operation, CMD_UNHIGHLIGHT) == 0) // remove highlight
	{
		expect_entity(message)->highlight = false;
	} else if(strcmp(operation, CMD_BYE) == 0) // Server says goodbye to client
	{
		puts("Connection terminated by server");
		quit(EXIT_SUCCESS);
	} else
	{
		fprintf(stderr, "Error: unknown command '%s'\n", operation);
		quit(EXIT_FAILURE);
	}
}

/**
 * Processes a message sent by the server
 *
 * Processes a message sent from the server, expects plain text that can describe a JSON object.
 * Attempt to parse the JSON object, and if the parse is successful, process the message as either
 * the initial handshake, the game start handshake, or as a command
 *
 * @param line message sent from the server as a string
 */
void receive(const char *line)
{
	struct json_object *root = json_tokener_parse(line);
	if(!root || json_object_get_type(root) != json_type_object)
	{
		// The server sent a malformed message
		fprintf(stderr, "Error: bad JSON: %s\n", line);
		quit(EXIT_FAILURE);
	}

	// The possibility of an error message is always considered first
	struct json_object *error = expect_key(root, CMD_ERROR, json_type_string, false);
	if(error)
	{
		fprintf(stderr, "Error: server failure: %s\n", json_object_get_string(error));
		quit(EXIT_FAILURE);
	}

	// Different commands are expected depending on the initialization and handshake status
	switch(game.state)
	{
		case GAME_STATE_HANDSHAKE_WHOAMI:
			start_or_watch_game(root);
			game.state = GAME_STATE_HANDSHAKE_INIT;
			break;

		case GAME_STATE_HANDSHAKE_INIT:
			init_graphics(root);
			init_sprites();
			init_clock();

			game.state = GAME_STATE_READY;
			break;

		default:
			handle_command(root);
			break;
	}

	json_object_put(root);
}