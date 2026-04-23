package com.example.randomdogandandroidintroproject

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * The base [Application] class for the Random Dog app.
 * 
 * Annotated with [@HiltAndroidApp] to trigger Hilt's code generation, 
 * including a base class for the application that serves as the 
 * application-level dependency container.
 */
@HiltAndroidApp
class RandomDogApplication : Application()
