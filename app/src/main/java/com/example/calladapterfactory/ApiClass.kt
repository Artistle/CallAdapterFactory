package com.example.calladapterfactory

import retrofit2.http.GET

interface ApiClass {

    @GET("/test_request")
    suspend fun getTest(): TestEntity
}