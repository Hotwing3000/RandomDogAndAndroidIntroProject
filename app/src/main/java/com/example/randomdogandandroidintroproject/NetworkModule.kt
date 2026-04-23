package com.example.randomdogandandroidintroproject

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifier for the Retrofit instance configured for the Dog API.
 * Uses [AnnotationRetention.BINARY] retention.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DogRetrofit

/**
 * Qualifier for the Retrofit instance configured for the Random User (Name) API.
 * Uses [AnnotationRetention.BINARY] retention.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NameRetrofit

/**
 * Hilt module that provides network-related dependencies.
 * 
 * It sets up two separate Retrofit instances because the application
 * consumes data from two different base URLs: one for dog images and 
 * another for random names.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides a singleton Retrofit instance pointing to the Dog API.
     * Uses Gson for JSON deserialization.
     */
    @DogRetrofit
    @Provides
    @Singleton
    fun provideDogRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://dog.ceo/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provides a singleton Retrofit instance pointing to the Random User API.
     * Uses Gson for JSON deserialization.
     */
    @NameRetrofit
    @Provides
    @Singleton
    fun provideNameRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://randomuser.me/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provides an implementation of [DogApiService] using the [DogRetrofit] qualified instance.
     * 
     * @param retrofit The Retrofit instance configured for the Dog API.
     */
    @Provides
    @Singleton
    fun provideDogApiService(@DogRetrofit retrofit: Retrofit): DogApiService {
        return retrofit.create(DogApiService::class.java)
    }

    /**
     * Provides an implementation of [NameApiService] using the [NameRetrofit] qualified instance.
     * 
     * @param retrofit The Retrofit instance configured for the Random User API.
     */
    @Provides
    @Singleton
    fun provideNameApiService(@NameRetrofit retrofit: Retrofit): NameApiService {
        return retrofit.create(NameApiService::class.java)
    }
}
