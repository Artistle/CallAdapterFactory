package com.example.calladapterfactory.exceptions

sealed class NetworkException(message: String) : Exception(message) {

    object AuthorizeFailed : NetworkException(message = "authorization failed")

    object UnAuthorize : NetworkException(message = "un authorize")

    object TokenUnAvailable : NetworkException(message = "token unavailable")

    object NotSubscribe : NetworkException(message = "not subscribe")

    companion object AuthExceptionMap {

        fun toList(): Map<String, Exception> = mapOf(
            "code=1" to AuthorizeFailed,
            "code=2" to UnAuthorize,
            "code=3" to TokenUnAvailable,
            "code=4" to NotSubscribe
        )
    }
}