package com.example.calladapterfactory

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.GlobalScope
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.concurrent.Executor

class CallAdapterFactory(private val executor: Executor) : CallAdapter.Factory() {

    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
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

        /**
         * костыль, не смог кастануть response к шаблоному типу, единому для всех сущностей
         */
        private val errors: ArrayList<String> = arrayListOf(
            "code=1",
            "code=2",
            "code=3",
            "code=4",
            "code=5",
            "code=6"
        )

        override fun clone(): Call<T> {
            return ExecutorCallbackCall(delegateCall.clone(), executor, retrofit)
        }

        override fun enqueue(callback: Callback<T>) {
            delegateCall.enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    executor.execute {

                        if (response.isSuccessful) {

                            val resp = response.body().toString()

                            if (resp.contains("code=0")) {
                                callback.onResponse(this@ExecutorCallbackCall, response)
                            } else {
                                errors.forEach {
                                    if (resp.contains(it)) {
                                        callback.onFailure(this@ExecutorCallbackCall, Exception(it))
                                    }
                                }
                            }
                        } else {
                            //TODO обработка неудачного запроса
                            //callback.onFailure(this@ExecutorCallbackCall, Exception())
                        }
                    }
                }

                override fun onFailure(call: Call<T>, throwable: Throwable) {
                    executor.execute {
                        when (throwable.message) {
                            "code=1" -> {
                                /**
                                 * здесь мы проверяем эксепшнеы, стоил создать sealed классы, для удобства, на каждый эксепшн
                                 */
                            }
                            /**
                            is java.lang.Exception -> callback.onFailure(this@ExecutorCallbackCall, java.lang.Exception("error test"))
                            else -> callback.onFailure(this@ExecutorCallbackCall, "UnexpectedException")
                            здесь можно проверять эксепшены
                             */
                        }
                    }
                }
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