package app.kobuggi.hyuabot.config

import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.model.RestaurantList
import app.kobuggi.hyuabot.model.Shuttle
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET

interface NetworkService {
    companion object{
        const val SERVER_URL = BuildConfig.server_url
    }
    @GET("/app/shuttle")
    fun getShuttleAll() : Observable<Shuttle>

    @GET("/app/food")
    fun getFoodAll() : Observable<RestaurantList>
}