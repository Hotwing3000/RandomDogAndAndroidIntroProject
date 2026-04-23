package com.example.randomdogandandroidintroproject

import android.util.Log
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data model representing the detailed information of a dog.
 *
 * @property name The assigned name of the dog.
 * @property imageUrl The URL pointing to an image of the dog.
 * @property breedDisplay A formatted string combining breed and group (e.g., "Tibetan Terrier").
 * @property breed The specific breed of the dog.
 * @property group The group the dog belongs to (e.g., "Terrier"), if available.
 * @property isLiked Indicates if the user has liked this specific dog entry.
 * @property hasBeenOpened Indicates if the user has expanded the details for this dog.
 */
data class DogDetails(
    val name: String? = null,
    val imageUrl: String? = null,
    val breedDisplay: String? = null,
    val breed: String? = null,
    val group: String? = null,
    val isLiked: Boolean = false,
    val hasBeenOpened: Boolean = false
)

/**
 * Repository responsible for managing dog data, including fetching from network APIs
 * and maintaining an in-memory cache.
 *
 * This repository coordinates calls to [DogApiService] for images and [NameApiService]
 * for names, while ensuring that data is persisted for the duration of the app session
 * via a simple index-based cache.
 */
@Singleton
class DogRepository @Inject constructor(
    private val dogApiService: DogApiService,
    private val nameApiService: NameApiService
) {
    /**
     * In-memory cache for dog details, keyed by their list index.
     */
    private val dogCache = mutableMapOf<Int, DogDetails>()
    
    /**
     * Internal list of indices for dogs that have been liked.
     */
    private val _likedIndices = mutableListOf<Int>()

    /**
     * An immutable list of indices representing all dogs currently liked by the user.
     */
    val likedIndices: List<Int> get() = _likedIndices.toList()

    /**
     * Retrieves the cached details for a dog at a specific index, if they exist.
     */
    fun getCachedDetails(index: Int): DogDetails? {
        return dogCache[index]
    }

    /**
     * Fetches a random dog image from the [DogApiService] for the given index.
     * Extracts breed information from the returned URL and updates the cache.
     *
     * @param index The position in the list to associate this image with.
     * @return The updated [DogDetails] or null if the network request fails.
     */
    suspend fun fetchImage(index: Int): DogDetails? {
        // If the item already has an image in cache, return it
        val cached = dogCache[index]
        if (cached?.imageUrl != null) return cached

        return try {
            val response = dogApiService.getRandomDogImage()
            val url = response.message
            val (breed, group, breedDisplay) = parseBreedInfo(url)

            Log.d("DogRepository", "Fetched dog: $breedDisplay for index $index, with url: $url")

            val updatedDetails = (dogCache[index] ?: DogDetails()).copy(
                imageUrl = url,
                breedDisplay = breedDisplay,
                breed = breed,
                group = group
            )
            dogCache[index] = updatedDetails
            updatedDetails
        } catch (e: Exception) {
            Log.e("DogRepository", "Error fetching image for index $index", e)
            null
        }
    }

    /**
     * Fetches a random name from the [NameApiService] for the given index.
     *
     * @param index The position in the list to associate this name with.
     * @return The updated [DogDetails] or null if the network request fails.
     */
    suspend fun fetchName(index: Int): DogDetails? {
        // If the item already has a name in cache, return it
        val cached = dogCache[index]
        if (cached?.name != null) return cached

        return try {
            val response = nameApiService.getRandomName()
            val fetchedName = response.results.first().name.first

            Log.d("DogRepository", "Fetched name: $fetchedName for index $index")

            val updatedDetails = (dogCache[index] ?: DogDetails()).copy(
                name = fetchedName
            )
            dogCache[index] = updatedDetails
            updatedDetails
        } catch (e: Exception) {
            Log.e("DogRepository", "Error fetching name for index $index", e)
            null
        }
    }

    /**
     * Toggles the [DogDetails.isLiked] status for the dog at the specified index.
     * Also manages the internal list of liked indices to preserve selection order.
     *
     * @param index The position of the dog to toggle.
     * @return The updated [DogDetails].
     */
    fun toggleLike(index: Int): DogDetails {
        val currentDetails = dogCache[index] ?: DogDetails()
        val isNowLiked = !currentDetails.isLiked
        val updatedDetails = currentDetails.copy(isLiked = isNowLiked)
        dogCache[index] = updatedDetails
        
        if (isNowLiked) {
            if (index !in _likedIndices) {
                _likedIndices.add(index)
            }
        } else {
            _likedIndices.remove(index)
        }
        
        return updatedDetails
    }

    /**
     * Marks a dog as "opened" (expanded) in the cache.
     *
     * @param index The position of the dog.
     * @return The updated [DogDetails].
     */
    fun markAsOpened(index: Int): DogDetails {
        val currentDetails = dogCache[index] ?: DogDetails()
        val updatedDetails = currentDetails.copy(hasBeenOpened = true)
        dogCache[index] = updatedDetails
        return updatedDetails
    }

    /**
     * Returns a list of all liked dogs along with their original indices.
     */
    fun getLikedDogs(): List<Pair<Int, DogDetails>> {
        return _likedIndices.mapNotNull { index ->
            dogCache[index]?.let { index to it }
        }
    }

    /**
     * Internal data class to hold parsed breed components.
     */
    private data class BreedInfo(val breed: String, val group: String?, val display: String)

    /**
     * Parses the Dog API URL to extract the breed and sub-breed (group).
     * Example URL: .../breeds/terrier-tibetan/n02097474_494.jpg
     * Result: breed="Tibetan", group="Terrier", display="Tibetan Terrier"
     */
    private fun parseBreedInfo(url: String): BreedInfo {
        val breedPart = url.substringAfter("/breeds/").substringBefore("/")
        val parts = breedPart.split("-")
        return if (parts.size >= 2) {
            val group = parts[0].replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            val breed = parts[1].replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            BreedInfo(breed, group, "$breed $group")
        } else {
            val breed = breedPart.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            BreedInfo(breed, null, breed)
        }
    }

    /**
     * Extracts only the display name of the breed from a Dog API URL.
     */
    private fun extractBreedFromUrl(url: String): String {
        return parseBreedInfo(url).display
    }
}
