package com.example.calladapterfactory

data class TestEntity(
    val collection: List<Any>,
    val error: Error,
    val responseEntity: ResponseEntity?
)

data class Error(
    val code: Int,
    val message: String
)

data class ResponseEntity(
    val result: Boolean
)