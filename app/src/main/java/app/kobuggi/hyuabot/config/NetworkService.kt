package app.kobuggi.hyuabot.config

import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.model.CampusRequest
import app.kobuggi.hyuabot.model.RestaurantList
import app.kobuggi.hyuabot.model.Shuttle
import app.kobuggi.hyuabot.model.SubwayERICA
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface NetworkService {
    companion object{
        const val SERVER_URL = BuildConfig.server_url
    }
    @GET("/app/shuttle")
    fun getShuttleAll() : Observable<Shuttle>

    @GET("/app/food")
    fun getFoodAll() : Observable<RestaurantList>

    @POST("/app/subway")
    fun getSubwayERICA(@Body body : CampusRequest) : Observable<SubwayERICA>
}