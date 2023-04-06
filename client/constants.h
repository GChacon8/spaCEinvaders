#ifndef CONSTANTS_H
#define CONSTANTS_H

#include <glob.h>

#include <SDL2/SDL.h>

// Clock frequency and period
#define CLOCK_HZ       30
#define NANOS_PER_TICK (1000000000 / CLOCK_HZ)

// SDL User Event Constants
#define X11_EVENT   (SDL_USEREVENT + 0)
#define TIMER_EVENT (SDL_USEREVENT + 1)

// Maximum supported size for a line sent by the server
#define MAX_INPUT_LINE_SIZE 512

// Default jump in the case of speed rates of the form 0/n, n != 0
#define JUMP_DEFAULT 1

// RGBA constants
#define COLOR_BLACK     0
#define COLOR_WHITE     255
#define ALPHA_HIGHLIGHT 96

// Parameters associated with the statistics label
#define FONT_FILE             "assets/arcade_n.ttf"
#define FONT_POINT_SIZE       8
#define STATS_LABEL_X         8
#define STATS_LABEL_Y         10
#define STATS_LABEL_FORMAT    "SCORE: %04d           LIVES: %d"
#define STATS_LABEL_MAX_CHARS 32
#define STATS_LABEL_COLOR     { .r = COLOR_WHITE, .g = COLOR_WHITE, .b = COLOR_WHITE, .a = COLOR_WHITE }

// Keys, command and protocol parameters with the server
#define CMD_OP          "op"
#define CMD_KEY         "key"
#define CMD_ERROR       "error"
#define CMD_BYE         "bye"
#define CMD_MOVE        "move"
#define CMD_INIT        "init"
#define CMD_PRESS       "press"
#define CMD_RELEASE     "release"
#define CMD_PUT         "put"
#define CMD_DELETE      "delete"
#define CMD_STATS       "stats"
#define CMD_HIGHLIGHT   "highlight"
#define CMD_UNHIGHLIGHT "unhighlight"
#define CMD_UNKNOWN     "unknown"
#define CMD_ID          "id"
#define CMD_SEQUENCE    "seq"
#define CMD_X           "x"
#define CMD_Y           "y"
#define CMD_Z           "z"
#define CMD_WIDTH       "width"
#define CMD_HEIGHT      "height"
#define CMD_WHOAMI      "whoami"
#define CMD_GAMES       "games"
#define CMD_LIVES       "lives"
#define CMD_SCORE       "score"

// Key strings used in the protocol with the server
#define KEY_LEFT   "left"
#define KEY_RIGHT  "right"
#define KEY_SHOOT  "shoot"

// Char constants
#define NEW_LINE        '\n'
#define PATH_SEPARATOR  '/'

// Integer constant that by convention indicates an invalid file descriptor
#define FD_INVALID -1

// Related to operations with paths and sprite globbing (auto-detection)
#define GLOB_FLAGS            (GLOB_ERR | GLOB_NOSORT | GLOB_NOESCAPE)
#define SPRITE_PATH_GLOB      "assets/sprites/*/\?\?-*.png"
#define SPRITE_PATH_PATTERN   "/%d-"
#define F_OPEN_MODE_APPEND    "a+"

// Command line options
#define CMDLINE_HELP                "help"
#define CMDLINE_OPT_HELP            'h'
#define CMDLINE_VERSION             "version"
#define CMDLINE_OPT_VERSION         'v'
#define CMDLINE_FULLSCREEN          "fullscreen"
#define CMDLINE_OPT_FULLSCREEN      'f'
#define CMDLINE_FULLSCREEN_FAKE     "fullscreen-fake"
#define CMDLINE_OPT_FULLSCREEN_FAKE 'F'
#define CMDLINE_ALL_SHORTS          "hvfF"

// Empirical constant parameters for vectors
#define DEFAULT_VEC_CAPACITY 4
#define VEC_CAPACITY_FACTOR  2

// Constant parameters for hash maps
#define DEFAULT_MAP_ORDER       8
#define HASH_MAP_CELLS_PER_ITEM 2

#endif