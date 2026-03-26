package com.example.randomdogandandroidintroproject

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel(private val repository: NameRepository = NameRepositoryImpl()) : ViewModel() {
    var shouldShowOnboarding by mutableStateOf(true)
        private set

    val names: List<String> = repository.getNames()

    fun onContinueClicked() {
        shouldShowOnboarding = false
    }
}
