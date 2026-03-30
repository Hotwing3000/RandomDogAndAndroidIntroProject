package com.example.randomdogandandroidintroproject

import retrofit2.http.GET

data class DogResponse(
    val message: String,
    val status: String
)

interface DogApiService {
    // Hilts creates an implementation of this in NetworkModule (also adding the base URL and retrofit engine)
    @GET("breeds/image/random")
    suspend fun getRandomDogImage(): DogResponse
}
