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


data class DogItem(
    val index: Int, // Stable ID for the "slot"
    val name: String,
    val imageUrl: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    @Named("numeric") private val numericRepository: NameRepository,
    @Named("roman") private val romanRepository: NameRepository,
    private val dogApiService: DogApiService
) : ViewModel() {

    var shouldShowOnboarding by mutableStateOf(true)
        private set

    var isRomanMode by mutableStateOf(false)
        private set

    var dogItems by mutableStateOf<List<DogItem>>(emptyList())
        private set

    // Persistent cache for URLs tied to the index
    private val imageCache = mutableMapOf<Int, String>()

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
            
            // Reconstruct the list while preserving cached images
            dogItems = names.mapIndexed { index, name ->
                DogItem(
                    index = index,
                    name = name,
                    imageUrl = imageCache[index] // Restore from cache

                )
            }
            Log.d("MainViewModel", "${imageCache.size}")
        }
    }

    fun fetchImageForItem(item: DogItem) {
        // If the item already has an image, don't fetch it again
        if (item.imageUrl != null) return

        viewModelScope.launch {
            try {
                val response = dogApiService.getRandomDogImage()
                Log.d("MainViewModel", "Fetched dog image URL for index ${item.index}: ${response.message}")
                
                // 1. Save to persistent cache
                imageCache[item.index] = response.message
                
                // 2. Update the active UI list
                dogItems = dogItems.map {
                    if (it.index == item.index) {
                        it.copy(imageUrl = response.message)
                    } else {
                        it
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching image for index ${item.index}", e)
            }
        }
    }

    fun toggleNamesMode() {
        isRomanMode = !isRomanMode
        loadNames()
    }
}
