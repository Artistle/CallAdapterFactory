package com.example.calladapterfactory.exceptions

sealed class UnexpectedException(message: String) : Exception(message) {

    object BaseUnexpectedException : UnexpectedException(message = "Oops :(")
}
