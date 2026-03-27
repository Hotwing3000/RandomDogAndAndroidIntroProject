package com.example.randomdogandandroidintroproject

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NameRepositoryModule {

    @Binds
    @Singleton
    @Named("numeric")
    abstract fun bindNumericRepository(
        impl: NameRepositoryImpl
    ): NameRepository

    @Binds
    @Singleton
    @Named("roman")
    abstract fun bindRomanRepository(
        impl: RomanNumeralNameRepository
    ): NameRepository
}
