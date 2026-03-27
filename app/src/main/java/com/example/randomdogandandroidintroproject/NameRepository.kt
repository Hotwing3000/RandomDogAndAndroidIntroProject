package com.example.randomdogandandroidintroproject

import javax.inject.Inject

interface NameRepository {
    fun getNames(amount: Int): List<String>
}

class NameRepositoryImpl @Inject constructor() : NameRepository {
    override fun getNames(amount: Int): List<String> {
        return List(amount) { "${it + 1}" }
    }
}

class RomanNumeralNameRepository @Inject constructor() : NameRepository {
    override fun getNames(amount: Int): List<String> {
        return List(amount) { toRoman(it + 1) }
    }

    private fun toRoman(num: Int): String {
        if (num <= 0) return num.toString()

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
