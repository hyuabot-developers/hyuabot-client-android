package app.kobuggi.hyuabot.service.rest

import app.kobuggi.hyuabot.model.bus.BusStopItem
import app.kobuggi.hyuabot.model.bus.BusTimetableResponse
import app.kobuggi.hyuabot.model.cafeteria.RestaurantListResponse
import app.kobuggi.hyuabot.model.library.ReadingRoomListResponse
import app.kobuggi.hyuabot.model.shuttle.ArrivalListResponse
import app.kobuggi.hyuabot.model.shuttle.StopItemResponse
import app.kobuggi.hyuabot.model.shuttle.TimetableResponse
import app.kobuggi.hyuabot.model.subway.SubwayStationResponse
import app.kobuggi.hyuabot.model.subway.SubwayTimetableResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface APIService {
    @Headers("Content-Type: application/json")
    @GET("shuttle/arrival")
    suspend fun entireShuttleArrivalList() : Response<ArrivalListResponse>

    @Headers("Content-Type: application/json")
    @GET("shuttle/timetable")
    suspend fun entireShuttleTimetable() : Response<TimetableResponse>

    @Headers("Content-Type: application/json")
    @GET("shuttle/stop/{stopID}")
    suspend fun shuttleStopItem(@Path("stopID") stopID: String) : Response<StopItemResponse>

    @Headers("Content-Type: application/json")
    @GET("subway/station/{stationID}")
    suspend fun subwayStationItem(@Path("stationID") stationID: String, @Query("all") entireTimetable: Boolean = false) : Response<SubwayStationResponse>

    @Headers("Content-Type: application/json")
    @GET("subway/station/{stationID}/timetable")
    suspend fun subwayStationTimetable(@Path("stationID") stationID: String) : Response<SubwayTimetableResponse>

    @Headers("Content-Type: application/json")
    @GET("bus/stop/{stopID}")
    suspend fun busStopItem(@Path("stopID") stopID: Int) : Response<BusStopItem>

    @Headers("Content-Type: application/json")
    @GET("bus/route/{routeID}/timetable/{stopID}")
    suspend fun busTimetableItem(@Path("routeID") routeID: Int, @Path("stopID") stopID: Int) : Response<BusTimetableResponse>

    @Headers("Content-Type: application/json")
    @GET("cafeteria/campus/{campusID}/restaurant")
    suspend fun cafeteriaItem(@Path("campusID") campusID: Int, @Query("date") date: String, @Query("time") timeType: String) : Response<RestaurantListResponse>

    @Headers("Content-Type: application/json")
    @GET("library/campus/{campusID}")
    suspend fun readingRoomList(@Path("campusID") campusID: Int) : Response<ReadingRoomListResponse>
}