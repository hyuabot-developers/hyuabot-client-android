package app.kobuggi.hyuabot.config

import app.kobuggi.hyuabot.model.*
import io.reactivex.Observable
import retrofit2.http.GET

interface GitHubNetworkService {
    @GET("/jil8885/API-for-ERICA/light/calendar/master.json")
    fun getEvent() : Observable<EventsJson>
}