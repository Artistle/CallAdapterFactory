package com.example.calladapterfactory

import androidx.lifecycle.ViewModel
import com.example.calladapterfactory.exceptions.NetworkException
import com.example.calladapterfactory.exceptions.UnexpectedException
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

open class BaseViewModel: ViewModel(), CoroutineScope {

    private val scopeJob: Job = SupervisorJob()

    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        handleThrowable(throwable)
    }

    override val coroutineContext: CoroutineContext = scopeJob + Dispatchers.IO + errorHandler

    private fun handleThrowable(ioe : Throwable) {

        when(ioe) {
            is NetworkException.UnAuthorize -> {
                var message = ioe.message
            }
            is NetworkException.TokenUnAvailable -> {
                var message = ioe.message
            }
            is NetworkException.NotSubscribe -> {
                var message = ioe.message
            }
            is NetworkException.AuthorizeFailed -> {
                var message = ioe.message
            }
            is UnexpectedException -> {
                var message = ioe.message
            }
        }
    }
}