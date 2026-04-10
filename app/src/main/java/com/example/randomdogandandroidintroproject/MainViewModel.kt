package com.example.randomdogandandroidintroproject

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
    val id: String, // The numeric or roman identifier
    val name: String? = null,
    val imageUrl: String? = null,
    val breedDisplay: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    @Named("numeric") private val numericRepository: IdRepository,
    @Named("roman") private val romanRepository: IdRepository,
    private val dogRepository: DogRepository
) : ViewModel() {

    var shouldShowOnboarding by mutableStateOf(true)
        private set

    var isRomanMode by mutableStateOf(false)
        private set

    var dogItems by mutableStateOf<List<DogItem>>(emptyList())
        private set

    init {
        loadDogItems()
    }

    fun onContinueClicked() {
        shouldShowOnboarding = false
    }

    private fun loadDogItems() {
        viewModelScope.launch {
            val amount = 1000
            val ids = if (isRomanMode) {
                romanRepository.getIds(amount = amount)
            } else {
                numericRepository.getIds(amount = amount)
            }
            
            // Reconstruct the list while preserving cached data from repository
            dogItems = ids.mapIndexed { index, id ->
                val cached = dogRepository.getCachedDetails(index)
                DogItem(
                    index = index,
                    id = id,
                    name = cached?.name,
                    imageUrl = cached?.imageUrl,
                    breedDisplay = cached?.breedDisplay
                )
            }
        }
    }

    fun loadDetailsForItem(item: DogItem) {
        viewModelScope.launch {
            // Fetch both in parallel
            launch {
                dogRepository.fetchImage(item.index)?.let { details ->
                    updateItemInList(item.index, details)
                }
            }
            launch {
                dogRepository.fetchName(item.index)?.let { details ->
                    updateItemInList(item.index, details)
                }
            }
        }
    }

    private fun updateItemInList(index: Int, details: DogDetails) {
        dogItems = dogItems.map {
            if (it.index == index) {
                it.copy(
                    name = details.name ?: it.name,
                    imageUrl = details.imageUrl ?: it.imageUrl,
                    breedDisplay = details.breedDisplay ?: it.breedDisplay
                )
            } else {
                it
            }
        }
    }

    fun toggleNamesMode() {
        isRomanMode = !isRomanMode
        loadDogItems()
    }
}
