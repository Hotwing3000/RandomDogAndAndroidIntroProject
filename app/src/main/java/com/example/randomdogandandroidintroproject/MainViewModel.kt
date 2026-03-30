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

    var names by mutableStateOf<List<String>>(emptyList())
        private set

    var dogImageUrl by mutableStateOf<String?>(null)
        private set

    init {
        loadNames()
        fetchRandomDog()
    }

    fun onContinueClicked() {
        shouldShowOnboarding = false
    }

    private fun loadNames() {
        viewModelScope.launch {
            val amount = 3
            names = if (isRomanMode) {
                romanRepository.getNames(amount = amount)
            } else {
                numericRepository.getNames(amount = amount)
            }
        }
    }

    fun fetchRandomDog() {
        viewModelScope.launch {
            try {
                val response = dogApiService.getRandomDogImage()
                dogImageUrl = response.message
                Log.d("MainViewModel", "Dog image URL: ${response.message}")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching dog image", e)
            }
        }
    }

    fun toggleNamesMode() {
        isRomanMode = !isRomanMode
        loadNames()
    }
}
