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

/**
 * UI model representing a single dog entry in the list.
 *
 * @property index The stable index in the list, used as a key for lazy layouts.
 * @property id The display identifier (e.g., "1" or "I").
 * @property name The name of the dog, fetched asynchronously.
 * @property imageUrl The URL of the dog's image, fetched asynchronously.
 * @property breedDisplay The formatted string for breed and group.
 * @property breed The specific breed name.
 * @property group The breed group (e.g., "Hound").
 * @property isLiked Whether the user has liked this dog.
 * @property hasBeenOpened Whether the card has ever been expanded/called.
 * @property isExpanded Whether the card is currently in its detailed view state.
 */
data class DogItem(
    val index: Int,
    val id: String,
    val name: String? = null,
    val imageUrl: String? = null,
    val breedDisplay: String? = null,
    val breed: String? = null,
    val group: String? = null,
    val isLiked: Boolean = false,
    val hasBeenOpened: Boolean = false,
    val isExpanded: Boolean = false
)

/**
 * ViewModel responsible for managing the state and logic of the Dog Discovery application.
 * 
 * It coordinates data fetching between the ID repositories and the Dog repository,
 * maintains the UI state for filtering and stats, and handles user interactions.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    @Named("numeric") private val numericRepository: IdRepository,
    @Named("roman") private val romanRepository: IdRepository,
    private val dogRepository: DogRepository
) : ViewModel() {

    /**
     * Controls whether the onboarding welcome screen is visible.
     */
    var shouldShowOnboarding by mutableStateOf(true)
        private set

    /**
     * Determines if IDs should be displayed in Roman numerals (true) or Numeric (false).
     */
    var isRomanMode by mutableStateOf(false)
        private set

    /**
     * If true, only dogs marked as [DogItem.isLiked] are shown in the list.
     */
    var showOnlyLiked by mutableStateOf(false)
        private set

    /**
     * If true, the statistics screen is shown instead of the dog list.
     */
    var showLikedStats by mutableStateOf(false)
        private set

    /**
     * The full list of dog items available for discovery.
     */
    var dogItems by mutableStateOf<List<DogItem>>(emptyList())
        private set

    /**
     * A filtered subset of [dogItems] containing only liked dogs.
     */
    var likedDogItems by mutableStateOf<List<DogItem>>(emptyList())
        private set

    init {
        loadDogItems()
    }

    /**
     * Dismisses the onboarding screen.
     */
    fun onContinueClicked() {
        shouldShowOnboarding = false
    }

    /**
     * Loads the initial list of dog items based on the current [isRomanMode].
     * Preserves cached data from the [dogRepository] for each item slot.
     */
    private fun loadDogItems() {
        viewModelScope.launch {
            val amount = 1000
            val ids = if (isRomanMode) {
                romanRepository.getIds(amount = amount)
            } else {
                numericRepository.getIds(amount = amount)
            }
            
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
                    isExpanded = false
                )
            }
            updateLikedDogsList()
        }
    }

    /**
     * Toggles the expansion state of a [DogItem].
     * When expanded for the first time, it marks the dog as opened and triggers data fetching.
     */
    fun toggleExpanded(item: DogItem) {
        val newExpandedState = !item.isExpanded
        if (newExpandedState) {
            val updatedDetails = dogRepository.markAsOpened(item.index)
            updateItemInList(item.index, updatedDetails, forceExpanded = true)
            loadDetailsForItem(item)
        } else {
            updateItemInList(item.index, details = null, forceExpanded = false)
        }
    }

    /**
     * Asynchronously fetches the image and name for a specific dog item.
     */
    private fun loadDetailsForItem(item: DogItem) {
        viewModelScope.launch {
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

    /**
     * Toggles the "liked" status of a dog and updates both the main and liked lists.
     */
    fun onLikeClicked(item: DogItem) {
        val updatedDetails = dogRepository.toggleLike(item.index)
        updateItemInList(item.index, updatedDetails)
    }

    /**
     * Helper to update a specific item in the [dogItems] list and refresh state.
     */
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

    /**
     * Re-filters [dogItems] to update the [likedDogItems] list.
     */
    private fun updateLikedDogsList() {
        likedDogItems = dogItems.filter { it.isLiked }
    }

    /**
     * Switches between Numeric and Roman numeral ID modes and reloads the list.
     */
    fun toggleNamesMode() {
        isRomanMode = !isRomanMode
        loadDogItems()
    }

    /**
     * Toggles the filter to show only liked dogs. Disables stats view if enabled.
     */
    fun toggleShowOnlyLiked() {
        showOnlyLiked = !showOnlyLiked
        if (showOnlyLiked) showLikedStats = false
    }

    /**
     * Toggles the statistics screen. Disables "liked only" filter if enabled.
     */
    fun toggleShowLikedStats() {
        showLikedStats = !showLikedStats
        if (showLikedStats) showOnlyLiked = false
    }
}
