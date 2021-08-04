package app.kobuggi.hyuabot.config

import app.kobuggi.hyuabot.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class NetworkClient{
    companion object{
        private const val BASE_URL = BuildConfig.server_url
        fun getServer() : NetworkService = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NetworkService::class.java)
    }
}