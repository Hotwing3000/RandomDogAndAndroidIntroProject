package com.example.randomdogandandroidintroproject

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DogRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NameRetrofit


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {


    @DogRetrofit
    @Provides
    @Singleton
    fun provideDogRetrofit(): Retrofit {
        return Retrofit.Builder()
        .baseUrl("https://dog.ceo/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    }

    @NameRetrofit
    @Provides
    @Singleton
    fun provideNameRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://randomuser.me/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideDogApiService(@DogRetrofit retrofit: Retrofit): DogApiService {
        // tell hilt how to create an instance of DogApiService
        return retrofit.create(DogApiService::class.java) // injects automatically the Retrofit instance from the provideRetrofit function above
    }

    @Provides
    @Singleton
    fun provideNameApiService(@NameRetrofit retrofit: Retrofit): NameApiService {
        // tell hilt how to create an instance of DogApiService
        return retrofit.create(NameApiService::class.java) // injects automatically the Retrofit instance from the provideRetrofit function above
    }
}
