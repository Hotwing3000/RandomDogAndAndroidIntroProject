package com.example.randomdogandandroidintroproject

import retrofit2.http.GET

/**
 * Root object representing the response from the Random User API.
 * 
 * @property results A list of user results matching the query.
 */
data class NameResponse(
    val results: List<UserResult>
)

/**
 * Represents an individual result entry from the API.
 * 
 * @property name The name object containing detailed name components.
 */
data class UserResult(
    val name: UserName
)

/**
 * Details about a user's name.
 * 
 * @property first The user's first name.
 */
data class UserName(
    val first: String
)

/**
 * Retrofit service interface for interacting with the Random User API.
 * Used to assign names to dog entries.
 */
interface NameApiService {
    /**
     * Fetches a random user profile from the API.
     * 
     * @return A [NameResponse] containing a list of generated user data.
     */
    @GET("api/")
    suspend fun getRandomName(): NameResponse
}
