package com.example.randomdogandandroidintroproject

import retrofit2.http.GET


// 1. The Root Object
data class NameResponse(val results: List<UserResult>
)

// 2. The objects inside the "results" array
data class UserResult(
    val name: UserName
)

// 3. The "name" object containing the actual string
data class UserName(
    val first: String
)

interface NameApiService {
    // Hilts creates an implementation of this in NetworkModule (also adding the base URL and retrofit engine)
    @GET("api/")
    suspend fun getRandomName(): NameResponse
}
