package com.example.randomdogandandroidintroproject

import retrofit2.http.GET

/**
 * Data model representing the response from the Dog API.
 *
 * @property message The URL of the dog image if successful, or an error message.
 * @property status The status of the API call (e.g., "success").
 */
data class DogResponse(
    val message: String,
    val status: String
)

/**
 * Retrofit service interface for interacting with the Dog API.
 */
interface DogApiService {
    /**
     * Fetches a random dog image from the API.
     * 
     * @return A [DogResponse] containing the image URL and status.
     */
    @GET("api/breeds/image/random")
    suspend fun getRandomDogImage(): DogResponse
}
