#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include "util.h"
#include "constants.h"

/**
 * Create and initialize a new vector
 * @param element_size memory size of each element
 * @return struct vec Initialized vector that stores vectors of size element_size
 */
struct vec vec_new(size_t element_size)
{
	struct vec empty =
	{
		.data         = NULL,
		.length       = 0,
		.capacity     = 0,
		.element_size = element_size,
	};

	return empty;
}

/**
 * Removes all elements from a vector
 * @param vec pointer to the array whose elements are to be removed
 */
void vec_clear(struct vec *vec)
{
	free(vec->data);

	vec->data = NULL;
	vec->length = vec->capacity = 0;
}

/**
 * Gets the element located at a given index of a vector
 * @param vec pointer to the vector that the element is in
 * @param index element index
 * @return void* pointer to the vector from which you want to get the element
 */
void *vec_get(struct vec *vec, size_t index)
{
	return (char*)vec->data + vec->element_size * index;
}

/**
 * Expands the maximum capacity of a vector
 * @param vec vector whose capacity you want to increase
 * @param required Capacity to which the vector is required to expand
 */
static void vec_require_capacity(struct vec *vec, size_t required)
{
	if(required > vec->capacity)
	{
		do
		{
			// Doubles the capacity
			vec->capacity = vec->capacity > 0 ? VEC_CAPACITY_FACTOR * vec->capacity : DEFAULT_VEC_CAPACITY;
		} while(required > vec->capacity);

		vec->data = realloc(vec->data, vec->element_size * vec->capacity);
		if(!vec->data)
		{
			perror("realloc");
			abort();
		}
	}
}

/**
 * Adds a space to a vector and returns a pointer to the new element
 * @param vec vector in which you want to place an emplace
 * @return void* pointer to the new space added to the vector
 */
void *vec_emplace(struct vec *vec)
{
	// Se extiende y luego se obtiene un puntero al último
	vec_require_capacity(vec, vec->length + 1);
	return (char*)vec->data + vec->length++ * vec->element_size;
}

/**
 * Removes an element at the given index from a vector
 * @param vec vector from which you want to remove the element
 * @param index element index
 */
void vec_delete(struct vec *vec, size_t index)
{
	assert(index < vec->length); //verifica que el índice sea válido

	void *target = (char*)vec->data + vec->element_size * index;
	void *source = (char*)target + vec->element_size;

	// Se corren los elementos que quedaron por encima del eliminado
	memmove(target, source, (vec->length-- - index - 1) * vec->element_size);
}

/**
 * Resizes a vector to a new given size
 * @param vec vector to resize
 * @param new_size new vector size
 */
void vec_resize(struct vec *vec, size_t new_size)
{
	vec_require_capacity(vec, new_size);
	if(new_size > vec->length)
	{
		memset(vec_get(vec, vec->length), 0, vec->element_size * (new_size - vec->length));
	}

	vec->length = new_size;
}