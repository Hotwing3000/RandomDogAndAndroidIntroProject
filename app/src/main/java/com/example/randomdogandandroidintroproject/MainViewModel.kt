package com.example.randomdogandandroidintroproject

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var shouldShowOnboarding by mutableStateOf(true)
        private set

    fun onContinueClicked() {
        shouldShowOnboarding = false
    }
}
