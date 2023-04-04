cmake_minimum_required(VERSION 3.25)
project(client C)

set(CMAKE_C_STANDARD 11)

add_executable(client main.c game.c loop.c net.c init.c quit.c hash_map.c vec.c game.c)

target_link_libraries(client json-c SDL2 SDL2_ttf SDL2_image X11)