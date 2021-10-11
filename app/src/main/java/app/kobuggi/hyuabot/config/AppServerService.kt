package app.kobuggi.hyuabot.config

import app.kobuggi.hyuabot.model.*
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AppServerService {
    @GET("/app/shuttle")
    fun getShuttleAll() : Call<Shuttle>

    @GET("/app/food")
    fun getFoodAll() : Call<RestaurantList>

    @POST("/app/subway")
    fun getSubwayERICA(@Body body : CampusRequest) : Observable<SubwayERICA>

    @GET("/app/bus")
    fun getBus() : Observable<Bus>

    @POST("/app/library")
    fun getReadingRoom(@Body body : CampusRequest) : Observable<ReadingRoomList>
}