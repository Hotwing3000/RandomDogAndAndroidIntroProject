package com.example.randomdogandandroidintroproject

interface NameRepository {
    fun getNames(): List<String>
}

class NameRepositoryImpl : NameRepository {
    override fun getNames(): List<String> {
        return List(1000) { "$it" }
    }
}

