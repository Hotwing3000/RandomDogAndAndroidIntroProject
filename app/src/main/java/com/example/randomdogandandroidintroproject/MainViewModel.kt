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

    init {
        loadNames()
    }

    fun onContinueClicked() {
        shouldShowOnboarding = false
    }

    private fun loadNames() {
        viewModelScope.launch {
            val names = if (isRomanMode) {
                romanRepository.getNames(amount = 3)
            } else {
                numericRepository.getNames(amount = 3)
            }
            // Initialize items without images first
            dogItems = names.map { DogItem(it) }
            // Then fetch images for all of them
            fetchRandomDogs()
        }
    }

    fun fetchRandomDogs() {
        viewModelScope.launch {
            // Fetch an image for each item in the list
            dogItems = dogItems.map { item ->
                try {
                    val response = dogApiService.getRandomDogImage()
                    item.copy(imageUrl = response.message)
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Error fetching image for ${item.name}", e)
                    item // keep old item if fetch fails
                }
            }
        }
    }

    fun toggleNamesMode() {
        isRomanMode = !isRomanMode
        loadNames()
    }
}
