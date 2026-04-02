package com.example.randomdogandandroidintroproject

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import java.util.Locale

data class DogItem(
    val index: Int, // Stable ID for the "slot"
    val name: String? = null,
    val imageUrl: String? = null,
    val breedDisplay: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    @Named("numeric") private val numericRepository: NameRepository,
    @Named("roman") private val romanRepository: NameRepository,
    private val dogApiService: DogApiService,
    private val nameApiService: NameApiService
) : ViewModel() {

    var shouldShowOnboarding by mutableStateOf(true)
        private set

    var isRomanMode by mutableStateOf(false)
        private set

    var dogItems by mutableStateOf<List<DogItem>>(emptyList())
        private set

    data class DogCacheData(
        val name: String? = null,
        val imageUrl: String? = null,
        val breedDisplay: String? = null
    )

    // Persistent cache for items tied to the index
    private val dogCache = mutableMapOf<Int, DogCacheData>()

    init {
        loadNames()
    }

    fun onContinueClicked() {
        shouldShowOnboarding = false
    }

    private fun loadNames() {
        viewModelScope.launch {
            val amount = 1000
            val names = if (isRomanMode) {
                romanRepository.getNames(amount = amount)
            } else {
                numericRepository.getNames(amount = amount)
            }
            
            // Reconstruct the list while preserving cached data
            dogItems = names.mapIndexed { index, name ->
                val cachedData = dogCache[index]
                DogItem(
                    index = index,
                    name = cachedData?.name ?: name,
                    imageUrl = cachedData?.imageUrl,
                    breedDisplay = cachedData?.breedDisplay
                )
            }
        }
    }

    fun fetchImageForItem(item: DogItem) {
        // If the item already has an image, don't fetch it again
        if (item.imageUrl != null) return

        viewModelScope.launch {
            try {
                val response = dogApiService.getRandomDogImage()
                val url = response.message
                val breedDisplay = extractBreedFromUrl(url)
                
                Log.d("MainViewModel", "Fetched dog: $breedDisplay for index ${item.index}, with url: $url")
                
                // 1. Save to persistent cache
                val currentDogCache = dogCache[item.index] ?: DogCacheData()
                dogCache[item.index] = currentDogCache.copy(imageUrl = url, breedDisplay = breedDisplay)

                // 2. Update the active UI list
                dogItems = dogItems.map {
                    if (it.index == item.index) {
                        it.copy(imageUrl = url, breedDisplay = breedDisplay)
                    } else {
                        it
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching image for index ${item.index}", e)
            }
        }
    }

    fun fetchNameForItem(item: DogItem) {
        // Check cache first to avoid redundant fetches if the UI list doesn't have it yet
        if (dogCache[item.index]?.name != null) return

        viewModelScope.launch {
            try {
                val response = nameApiService.getRandomName()
                val fetchedName = response.results.first().name.first

                Log.d("MainViewModel", "Fetched name: $fetchedName for index ${item.index}")

                val currentDogCache = dogCache[item.index] ?: DogCacheData()
                dogCache[item.index] = currentDogCache.copy(name = fetchedName)


                // 2. Update the active UI list
                dogItems = dogItems.map {
                    if (it.index == item.index) {
                        it.copy(name = fetchedName)
                    } else {
                        it
                    }
                }

            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching name for index ${item.index}", e)
            }
        }
    }

    private fun extractBreedFromUrl(url: String): String {
        // Example: "https://images.dog.ceo/breeds/terrier-tibetan/n02097474_494.jpg"
        // 1. Get the part after "/breeds/"
        val breedPart = url.substringAfter("/breeds/").substringBefore("/")
        
        // 2. Split by '-'
        val parts = breedPart.split("-")
        
        return if (parts.size >= 2) {
            // "terrier-tibetan" -> parts[1]="tibetan", parts[0]="terrier" -> "Tibetan Terrier"
            val breed = parts[1].replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            val group = parts[0].replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            "$breed $group"
        } else {
            // "shiba" -> "Shiba"
            breedPart.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }

    fun toggleNamesMode() {
        isRomanMode = !isRomanMode
        loadNames()
    }
}
