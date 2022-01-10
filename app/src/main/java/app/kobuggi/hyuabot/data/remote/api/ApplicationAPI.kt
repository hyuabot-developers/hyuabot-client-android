package app.kobuggi.hyuabot.data.remote.api

import app.kobuggi.hyuabot.data.remote.domain.restaurant.RestaurantItem
import app.kobuggi.hyuabot.data.remote.response.bus.BusDepartureByRouteResponse
import app.kobuggi.hyuabot.data.remote.response.bus.BusDepartureResponse
import app.kobuggi.hyuabot.data.remote.response.readingRoom.ReadingRoomResponse
import app.kobuggi.hyuabot.data.remote.response.shuttle.ShuttleDepartureByStopResponse
import app.kobuggi.hyuabot.data.remote.response.shuttle.ShuttleDepartureResponse
import app.kobuggi.hyuabot.data.remote.response.shuttle.ShuttleStopInfoResponse
import app.kobuggi.hyuabot.data.remote.response.subway.SubwayERICAResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface ApplicationAPI {
    @GET("/app/shuttle")
    fun getShuttleArrivalInfo(): Single<ShuttleDepartureResponse>

    @GET("/app/shuttle")
    fun getShuttleArrivalInfoByStop(@QueryMap params: MutableMap<String, String>): Single<ShuttleDepartureByStopResponse>

    @GET("/app/shuttle/by-stop")
    fun getShuttleStopInfo(@QueryMap params: MutableMap<String, String>): Single<ShuttleStopInfoResponse>

    @GET("/app/subway")
    fun getSubwayArrivalInfo(@QueryMap params: MutableMap<String, String>): Single<SubwayERICAResponse>

    @GET("/app/bus")
    fun getBusArrivalInfo(@QueryMap params: MutableMap<String, String>): Single<BusDepartureResponse>

    @GET("/app/bus")
    fun getBusArrivalInfoByRoute(@QueryMap params: MutableMap<String, String>): Single<BusDepartureByRouteResponse>

    @GET("/app/bus/timetable")
    fun getBusTimetableByRoute(@QueryMap params: MutableMap<String, String>): Single<BusDepartureByRouteResponse>

    @GET("/app/library")
    fun getReadingRoomInfo(@QueryMap params: MutableMap<String, String>): Single<ReadingRoomResponse>

    @GET("/app/food")
    fun getRestaurantMenu(@QueryMap params: MutableMap<String, String>): Single<ArrayList<RestaurantItem>>


}