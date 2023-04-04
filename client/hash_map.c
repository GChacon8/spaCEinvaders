#include <stdlib.h>
#include <assert.h>
#include <string.h>

#include "util.h"
#include "constants.h"

/**
 * Search for a key-value pair in a given bucket
 * @param bucket pointer to the hashmap bucket in which you want to search for the pair
 * @param lookup key that identifies the pair sought
 * @return void* pointer to the pair if it is found in the bucket, NULL otherwise
 */
static void *bucket_get_pair(struct vec *bucket, int lookup)
{
	if(bucket)
	{
		// is searched linearly
		for(size_t i = 0; i < bucket->length; ++i)
		{
			char *pair = vec_get(bucket, i); // Get element at index i of bucket vector
			int *key = (int*)pair;

			if(lookup == *key)
			{
				return pair;
			}
		}
	}

	return NULL;
}

/**
 * Create a new hashmap and return the same
 * @param order hashmap order
 * @param value_size size of an entry in the hashmap
 * @return struct hash_map hashmap created
 */
struct hash_map hash_map_new(unsigned order, size_t value_size)
{
	assert(order > 0); // order cannot be null

	struct hash_map empty =
	{
		.buckets    = vec_new(sizeof(struct vec)),
		.order      = order,
		.value_size = value_size
	};

	return empty;
}

/**
 * Remove all elements from a given hash map
 * @param map pointer to the hashmap whose elements you want to remove
 */
void hash_map_clear(struct hash_map *map)
{
	for(size_t i = 0; i < map->buckets.length; ++i)
	{
		vec_clear(vec_get(&map->buckets, i));
	}

	vec_clear(&map->buckets);
}

/**
 * Gets the cell size of a hashmap element
 * @param map map whose cell size you want to get
 */
static size_t hash_map_cell_size(struct hash_map *map)
{
	// Maximum between key size (sizeof(int)) and value size, to guarantee alignment
	return map->value_size > sizeof(int) ? map->value_size : sizeof(int);
}

/**
 * Gets the bucket that contains the record with the given key
 * @param map hashmap in which the bucket is located
 * @param key registry key to search
 * @return struct vec* bucket containing the record identified by the key, or NULL if the record does not exist
 */
static struct vec *hash_map_bucket_for(struct hash_map *map, int key)
{
	// No memory reserved
	if(!map->buckets.data)
	{
		return NULL;
	}

	/* `((1 << order) - 1)` is a power of two minus use: `2 ** order - 1`
	 * So this takes the bits indicated by the order.
	 */
	return vec_get(&map->buckets, (unsigned)key & ((1u << map->order) - 1));
}

/**
 * Gets a key-value pair from a hashmap
 * @param map hashmap from which you want to get the key-value pair
 * @param lookup key that identifies the pair
 * @return void* pointer to key-value pair
 */
void *hash_map_get(struct hash_map *map, int lookup)
{
	struct vec *bucket = hash_map_bucket_for(map, lookup);
	char *pair = bucket_get_pair(bucket, lookup);

	return pair ? pair + hash_map_cell_size(map) : NULL;
}

/**
 * Add a key-value pair to a hashmap
 * @param map hashmap to which you want to add the record
 * @param key key that identifies the record
 * @param value registry value
 * @return void* pointer to inserted value
 */
void *hash_map_put(struct hash_map *map, int key, const void *value)
{
	// Buckets are initialized if they have not already
	if(!map->buckets.data)
	{
		vec_resize(&map->buckets, 1lu << map->order);

		struct vec empty_bucket = vec_new(HASH_MAP_CELLS_PER_ITEM * hash_map_cell_size(map));
		for(size_t i = 0; i < 1lu << map->order; ++i)
		{
			*((struct vec*)vec_get(&map->buckets, i)) = empty_bucket;
		}
	}

	struct vec *bucket = hash_map_bucket_for(map, key);
	char *pair = bucket_get_pair(bucket, key);

	// If the element was not already there, it is inserted
	if(!pair)
	{
		pair = vec_emplace(bucket);
	}

	int *stored_key = (int*)pair;
	void *stored_value = pair + hash_map_cell_size(map);

	// It is overwritten, whether it was there before or not
	*stored_key = key;
	return memcpy(stored_value, value, map->value_size);
}

/**
 * Removes a record identified by a key from a hash map
 * @param map hashmap from which you want to remove the record
 * @param key key that identifies the record
 */
void hash_map_delete(struct hash_map *map, int key)
{
	struct vec *bucket = hash_map_bucket_for(map, key);
	char *pair = bucket_get_pair(bucket, key);

	if(pair)
	{
		// The second argument is an index
		vec_delete(bucket, (pair - (char*)bucket->data) / bucket->element_size);
	}
}

/**
 * Begins an iteration over a map
 * @param map the map to iterate over
 * @return struct hash_map_iter iterator over the hashmap
 */
struct hash_map_iter hash_map_iter(struct hash_map *map)
{
	struct hash_map_iter iter =
	{
		.map            = map,
		.cell           = NULL,
		.current_bucket = 0,
		.next_index     = 0
	};

	hash_map_iter_next(&iter);
	return iter;
}

/**
 * Advance the iterator. It is only valid to call this procedure if `iter->cell` is not a null pointer
 * @param iter iterator to advance
 */
void hash_map_iter_next(struct hash_map_iter *iter)
{
	for(; iter->current_bucket < iter->map->buckets.length; ++iter->current_bucket)
	{
		struct vec *bucket = vec_get(&iter->map->buckets, iter->current_bucket);
		if(iter->next_index < bucket->length)
		{
            // Found a next cell in the iteration
			iter->cell = vec_get(bucket, iter->next_index++);
			return;
		}

        // The current bucket has no more items to iterate over
		iter->next_index = 0;
	}

    // If not found, iteration is over
	iter->cell = NULL;
}

/**
 * Gets the key of the current element in the iterator
 * @return int key of the current element
 */
int hash_map_iter_key(struct hash_map_iter *iter)
{
	return *(int*)iter->cell;
}

/**
 * Gets a pointer to the current value of the iterator
 * @return int value of the current element
 */
void *hash_map_iter_value(struct hash_map_iter *iter)
{
	return (char*)iter->cell + hash_map_cell_size(iter->map);
}