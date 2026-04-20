package com.example.randomdogandandroidintroproject

import android.util.Log
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class DogDetails(
    val name: String? = null,
    val imageUrl: String? = null,
    val breedDisplay: String? = null,
    val breed: String? = null,
    val group: String? = null,
    val isLiked: Boolean = false,
    val hasBeenOpened: Boolean = false
)

@Singleton
class DogRepository @Inject constructor(
    private val dogApiService: DogApiService,
    private val nameApiService: NameApiService
) {
    // Persistent cache for items tied to the index
    private val dogCache = mutableMapOf<Int, DogDetails>()
    
    // List of indices for dogs that are liked, preserving the order they were liked in
    private val _likedIndices = mutableListOf<Int>()
    val likedIndices: List<Int> get() = _likedIndices.toList()

    fun getCachedDetails(index: Int): DogDetails? {
        return dogCache[index]
    }

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

    fun markAsOpened(index: Int): DogDetails {
        val currentDetails = dogCache[index] ?: DogDetails()
        val updatedDetails = currentDetails.copy(hasBeenOpened = true)
        dogCache[index] = updatedDetails
        return updatedDetails
    }

    fun getLikedDogs(): List<Pair<Int, DogDetails>> {
        return _likedIndices.mapNotNull { index ->
            dogCache[index]?.let { index to it }
        }
    }

    private data class BreedInfo(val breed: String, val group: String?, val display: String)

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

    private fun extractBreedFromUrl(url: String): String {
        return parseBreedInfo(url).display
    }
}
