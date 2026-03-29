package com.example.randomdogandandroidintroproject

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface NameRepository {
    suspend fun getNames(amount: Int): List<String>
}

class NameRepositoryImpl @Inject constructor() : NameRepository {
    override suspend fun getNames(amount: Int): List<String> = withContext(Dispatchers.Default) {
        List(amount) { "${it + 1}" }
    }
}

class RomanNumeralNameRepository @Inject constructor() : NameRepository {
    override suspend fun getNames(amount: Int): List<String> = withContext(Dispatchers.Default) {
        List(amount) { toRoman(it + 1) }
    }

    /**
     * Converts an integer to a Roman numeral string.
     *
     * This implementation supports numbers up to 3,999. Numbers larger than 3,999
     * require vinculum symbols (bars over letters to indicate multiplication by 1,000),
     * which are not implemented here.
     *
     * @param num The integer to convert.
     * @return The Roman numeral representation, or the original number as a string
     * if it is outside the supported range (1-3,999).
     */
    private fun toRoman(num: Int): String {
        if (num <= 0 || num > 3999) return num.toString()

        val values = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
        val symbols = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")

        var remaining = num
        val result = StringBuilder()

        for (i in values.indices) {
            while (remaining >= values[i]) {
                remaining -= values[i]
                result.append(symbols[i])
            }
        }
        return result.toString()
    }
}
