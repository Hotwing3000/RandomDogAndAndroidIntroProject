package com.example.randomdogandandroidintroproject

import android.util.Log
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class DogDetails(
    val name: String? = null,
    val imageUrl: String? = null,
    val breedDisplay: String? = null
)

@Singleton
class DogRepository @Inject constructor(
    private val dogApiService: DogApiService,
    private val nameApiService: NameApiService
) {
    // Persistent cache for items tied to the index
    private val dogCache = mutableMapOf<Int, DogDetails>()

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
            val breedDisplay = extractBreedFromUrl(url)

            Log.d("DogRepository", "Fetched dog: $breedDisplay for index $index, with url: $url")

            val updatedDetails = (dogCache[index] ?: DogDetails()).copy(
                imageUrl = url,
                breedDisplay = breedDisplay
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

    private fun extractBreedFromUrl(url: String): String {
        val breedPart = url.substringAfter("/breeds/").substringBefore("/")
        val parts = breedPart.split("-")
        return if (parts.size >= 2) {
            val breed = parts[1].replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            val group = parts[0].replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            "$breed $group"
        } else {
            breedPart.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }
}
