package com.example.randomdogandandroidintroproject

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MainViewModel @Inject constructor(
    @Named("numeric") private val numericRepository: NameRepository,
    @Named("roman") private val romanRepository: NameRepository
) : ViewModel() {

    var shouldShowOnboarding by mutableStateOf(true)
        private set

    var isRomanMode by mutableStateOf(false)
        private set

    var names by mutableStateOf(numericRepository.getNames(amount = 100))
        private set

    fun onContinueClicked() {
        shouldShowOnboarding = false
    }

    fun toggleNamesMode() {
        isRomanMode = !isRomanMode
        names = if (isRomanMode) {
            romanRepository.getNames(amount = 100)
        } else {
            numericRepository.getNames(amount = 100)
        }
    }
}
