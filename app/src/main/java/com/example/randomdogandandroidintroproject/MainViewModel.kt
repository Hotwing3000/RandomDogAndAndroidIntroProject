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
    val breedDisplay: String? = null,
    val breed: String? = null,
    val group: String? = null,
    val isLiked: Boolean = false,
    val hasBeenOpened: Boolean = false,
    val isExpanded: Boolean = false
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

    var showOnlyLiked by mutableStateOf(false)
        private set

    var showLikedStats by mutableStateOf(false)
        private set

    var dogItems by mutableStateOf<List<DogItem>>(emptyList())
        private set

    var likedDogItems by mutableStateOf<List<DogItem>>(emptyList())
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
                    breedDisplay = cached?.breedDisplay,
                    breed = cached?.breed,
                    group = cached?.group,
                    isLiked = cached?.isLiked ?: false,
                    hasBeenOpened = cached?.hasBeenOpened ?: false,
                    isExpanded = false // Reset expanded on mode toggle for simplicity, or preserve if needed
                )
            }
            updateLikedDogsList()
        }
    }

    fun toggleExpanded(item: DogItem) {
        val newExpandedState = !item.isExpanded
        if (newExpandedState) {
            // Mark as opened in repository when expanding
            val updatedDetails = dogRepository.markAsOpened(item.index)
            updateItemInList(item.index, updatedDetails, forceExpanded = true)
            
            // Trigger data load
            loadDetailsForItem(item)
        } else {
            updateItemInList(item.index, details = null, forceExpanded = false)
        }
    }

    private fun loadDetailsForItem(item: DogItem) {
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

    fun onLikeClicked(item: DogItem) {
        val updatedDetails = dogRepository.toggleLike(item.index)
        updateItemInList(item.index, updatedDetails)
    }

    private fun updateItemInList(index: Int, details: DogDetails?, forceExpanded: Boolean? = null) {
        dogItems = dogItems.map {
            if (it.index == index) {
                it.copy(
                    name = details?.name ?: it.name,
                    imageUrl = details?.imageUrl ?: it.imageUrl,
                    breedDisplay = details?.breedDisplay ?: it.breedDisplay,
                    breed = details?.breed ?: it.breed,
                    group = details?.group ?: it.group,
                    isLiked = details?.isLiked ?: it.isLiked,
                    hasBeenOpened = details?.hasBeenOpened ?: it.hasBeenOpened,
                    isExpanded = forceExpanded ?: it.isExpanded
                )
            } else {
                it
            }
        }
        updateLikedDogsList()
    }

    private fun updateLikedDogsList() {
        likedDogItems = dogItems.filter { it.isLiked }
    }

    fun toggleNamesMode() {
        isRomanMode = !isRomanMode
        loadDogItems()
    }

    fun toggleShowOnlyLiked() {
        showOnlyLiked = !showOnlyLiked
        if (showOnlyLiked) showLikedStats = false
    }

    fun toggleShowLikedStats() {
        showLikedStats = !showLikedStats
        if (showLikedStats) showOnlyLiked = false
    }
}
