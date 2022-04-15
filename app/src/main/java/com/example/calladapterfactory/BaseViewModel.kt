package com.example.calladapterfactory

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

open class BaseViewModel: ViewModel(), CoroutineScope {

    private val scopeJob: Job = SupervisorJob()

    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        /**
         * здесь мы отлавливаем эксепшн, создаём метод handleThrowable и обрабатываем эксепшены как нам угодно
         */
        var error = throwable.message
    }

    override val coroutineContext: CoroutineContext = scopeJob + Dispatchers.IO + errorHandler
}