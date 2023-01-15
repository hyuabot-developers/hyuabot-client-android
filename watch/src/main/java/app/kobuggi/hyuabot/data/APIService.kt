package app.kobuggi.hyuabot.data

import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.data.model.ArrivalResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface APIService {
    @GET("/rest/shuttle/arrival")
    suspend fun getArrivalList(): ArrivalResponse

    companion object {
        var apiService: APIService? = null

        fun getInstance(): APIService {
            if (apiService == null) {
                apiService = Retrofit.Builder()
                    .baseUrl(BuildConfig.API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(APIService::class.java)
            }
            return apiService!!
        }
    }
}