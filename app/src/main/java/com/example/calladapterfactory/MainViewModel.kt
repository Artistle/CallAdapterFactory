package com.example.calladapterfactory

import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainViewModel: BaseViewModel() {

    var retrofit: ApiClass = Retrofit.Builder()
        .baseUrl("https://6b03918d-e381-4042-972b-95f9c8527666.mock.pstmn.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(CallAdapterFactory.create())
        .build()
        .create(ApiClass::class.java)

    fun load() = launch {
        var r = retrofit.getTest()
        var e = ""
    }

}