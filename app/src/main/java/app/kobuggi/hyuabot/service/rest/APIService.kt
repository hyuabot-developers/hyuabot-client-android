package app.kobuggi.hyuabot.service.rest

import app.kobuggi.hyuabot.model.bus.BusStopItem
import app.kobuggi.hyuabot.model.bus.BusTimetableResponse
import app.kobuggi.hyuabot.model.shuttle.ArrivalListResponse
import app.kobuggi.hyuabot.model.shuttle.StopItemResponse
import app.kobuggi.hyuabot.model.shuttle.TimetableResponse
import app.kobuggi.hyuabot.model.subway.SubwayStationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

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
    suspend fun subwayStationItem(@Path("stationID") stationID: String) : Response<SubwayStationResponse>

    @Headers("Content-Type: application/json")
    @GET("bus/stop/{stopID}")
    suspend fun busStopItem(@Path("stopID") stopID: Int) : Response<BusStopItem>

    @Headers("Content-Type: application/json")
    @GET("bus/route/{routeID}/timetable/{stopID}")
    suspend fun busTimetableItem(@Path("routeID") routeID: Int, @Path("stopID") stopID: Int) : Response<BusTimetableResponse>
}