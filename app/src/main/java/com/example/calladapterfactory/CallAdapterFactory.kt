package com.example.calladapterfactory

import android.os.Handler
import android.os.Looper
import com.example.calladapterfactory.exceptions.NetworkException
import com.example.calladapterfactory.exceptions.UnexpectedException
import retrofit2.*
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.concurrent.Executor

class CallAdapterFactory(private val executor: Executor) : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) {
            return null
        }
        val responseType = getCallResponseType(returnType)
        return object : CallAdapter<Any?, Call<*>> {
            override fun responseType(): Type {
                return responseType
            }

            override fun adapt(call: Call<Any?>): Call<Any?> {
                return ExecutorCallbackCall(call, executor, retrofit)
            }
        }
    }

    private fun getCallResponseType(returnType: Type): Type {
        require(returnType is ParameterizedType) { "Call return type must be parameterized" }
        return getParameterUpperBound(0, returnType)
    }

    class ExecutorCallbackCall<T>(
        private val delegateCall: Call<T>,
        private val executor: Executor,
        private val retrofit: Retrofit
    ) : Call<T> by delegateCall {

        private val successfulCode = "code=0"

        override fun clone(): Call<T> {
            return ExecutorCallbackCall(delegateCall.clone(), executor, retrofit)
        }

        override fun enqueue(callback: Callback<T>) {
            delegateCall.enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    executor.execute {

                        if (response.isSuccessful) {

                            val responseString = response.body().toString()

                            if (responseString.contains(successfulCode)) callback.onResponse(this@ExecutorCallbackCall, response)
                            else{
                                val exception = NetworkException.toList().filter { responseString.contains(it.key) }
                                if (exception.isNullOrEmpty()) {
                                    executeCallback(UnexpectedException.BaseUnexpectedException)
                                } else {
                                    executeCallback(exception.values.elementAt(0))
                                }
                            }
                        } else {
                            //TODO обработка неудачного запроса
                            executeCallback(UnexpectedException.BaseUnexpectedException)
                        }
                    }
                }

                override fun onFailure(call: Call<T>, throwable: Throwable) {
                    executor.execute {
                        when (throwable) {
                            is IOException -> executeCallback(throwable)
                            else -> executeCallback(UnexpectedException.BaseUnexpectedException)
                        }
                    }
                }

                fun executeCallback(throwable: Throwable) = callback.onFailure(this@ExecutorCallbackCall, throwable)
            })
        }
    }

    class MainThreadExecutor : Executor {

        private val handler = Handler(Looper.getMainLooper())

        override fun execute(r: Runnable) {
            handler.post(r)
        }
    }

    companion object {

        fun create(): CallAdapter.Factory {
            return CallAdapterFactory(MainThreadExecutor())
        }
    }
}