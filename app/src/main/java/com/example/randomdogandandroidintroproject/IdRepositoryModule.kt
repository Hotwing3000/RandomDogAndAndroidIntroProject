package com.example.randomdogandandroidintroproject

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt module for providing implementations of [IdRepository].
 *
 * This module uses Dagger's [Binds] to link the [IdRepository] interface to its
 * concrete implementations. It uses [Named] qualifiers to distinguish between
 * numeric and Roman numeral versions.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class IdRepositoryModule {

    /**
     * Binds the numeric implementation of [IdRepository].
     *
     * @param impl The concrete [IdRepositoryImpl] instance.
     * @return An [IdRepository] that provides numeric identifiers.
     */
    @Binds
    @Singleton
    @Named("numeric")
    abstract fun bindNumericRepository(
        impl: IdRepositoryImpl
    ): IdRepository

    /**
     * Binds the Roman numeral implementation of [IdRepository].
     *
     * @param impl The concrete [RomanNumeralIdRepository] instance.
     * @return An [IdRepository] that provides Roman numeral identifiers.
     */
    @Binds
    @Singleton
    @Named("roman")
    abstract fun bindRomanRepository(
        impl: RomanNumeralIdRepository
    ): IdRepository
}
