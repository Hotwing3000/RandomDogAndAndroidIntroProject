package com.example.randomdogandandroidintroproject

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        // Builds the Retrofit instance/engine (this lifetime = app lifetime)
        return Retrofit.Builder()
            .baseUrl("https://dog.ceo/api/")
            .addConverterFactory(GsonConverterFactory.create()) // Add Gson converter (Converts JSON to Kotlin objects)
            .build()
    }

    @Provides
    @Singleton
    fun provideDogApiService(retrofit: Retrofit): DogApiService {
        // tell hilt how to create an instance of DogApiService
        return retrofit.create(DogApiService::class.java) // injects automatically the Retrofit instance from the provideRetrofit function above
    }
}
