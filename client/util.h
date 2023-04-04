#ifndef UTIL_H
#define UTIL_H

#include <stddef.h>

/**
 * Implementation of a dynamic array
 */
struct vec
{
	void  *data;
	size_t length;
	size_t capacity;
	size_t element_size;
};

/**
 * Hashmap implementation
 */
struct hash_map
{
	struct vec buckets;
	unsigned   order;
	size_t     value_size;
};

/**
 * Iterator over a hashmap
 */
struct hash_map_iter
{
	struct hash_map *map;
	void            *cell;
	size_t           current_bucket;
	size_t           next_index;
};


/**
 * Create and initialize a new vector
 * @param element_size memory size of each element
 * @return struct vec Initialized vector that stores vectors of size element_size
 */
struct vec vec_new(size_t element_size);

/**
 * Removes all elements from a vector
 * @param vec pointer to the array whose elements are to be removed
 */
void vec_clear(struct vec *vec);

/**
 * Gets the element located at a given index of a vector
 * @param vec pointer to the vector that the element is in
 * @param index element index
 * @return void* pointer to the vector from which you want to get the element
 */
void *vec_get(struct vec *vec, size_t index);

/**
 * Adds a space to a vector and returns a pointer to the new element
 * @param vec vector in which you want to place an emplace
 * @return void* pointer to the new space added to the vector
 */
void *vec_emplace(struct vec *vec);

/**
 * Removes an element at the given index from a vector
 * @param vec vector from which you want to remove the element
 * @param index element index
 */
void vec_delete(struct vec *vec, size_t index);

/**
 * Resizes a vector to a new given size
 * @param vec vector to resize
 * @param new_size new vector size
 */
void vec_resize(struct vec *vec, size_t new_size);

/**
 * Create a new hashmap and return the same
 * @param order hashmap order
 * @param value_size size of an entry in the hashmap
 * @return struct hash_map hashmap created
 */
struct hash_map hash_map_new(unsigned order, size_t value_size);

/**
 * Remove all elements from a given hashmap
 * @param map pointer to the hashmap whose elements you want to remove
 */
void hash_map_clear(struct hash_map *map);

/**
 * Gets a key-value pair from a hashmap
 * @param map hashmap from which you want to get the key-value pair
 * @param lookup key that identifies the pair
 * @return void* pointer to key-value pair
 */
void *hash_map_get(struct hash_map *map, int lookup);

/**
 * Add a key-value pair to a hashmap
 * @param map hashmap to which you want to add the record
 * @param key key that identifies the record
 * @param value registry value
 * @return void* pointer to inserted value
 */
void *hash_map_put(struct hash_map *map, int key, const void *value);

/**
 * Removes a record identified by a key from a hash map
 * @param map hashmap from which you want to remove the record
 * @param key key that identifies the record
 */
void hash_map_delete(struct hash_map *map, int key);

/**
 * Begins an iteration over a map
 * @param map the map to iterate over
 * @return struct hash_map_iter iterator over the hashmap
 */
struct hash_map_iter hash_map_iter(struct hash_map *map);

/**
 * Advance the iterator. It is only valid to call this procedure if `iter->cell` is not a null pointer
 * @param iter iterator to advance
 */
void hash_map_iter_next(struct hash_map_iter *iter);

/**
 * Gets the key of the current element in the iterator
 * @return int key of the current element
 */
int hash_map_iter_key(struct hash_map_iter *iter);

/**
 * Gets a pointer to the current value of the iterator
 * @return int value of the current element
 */
void *hash_map_iter_value(struct hash_map_iter *iter);

#endif