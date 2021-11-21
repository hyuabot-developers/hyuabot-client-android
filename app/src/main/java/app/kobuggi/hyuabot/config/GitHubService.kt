package app.kobuggi.hyuabot.config

import app.kobuggi.hyuabot.model.*
import io.reactivex.Observable
import retrofit2.http.GET

interface GitHubService {
    @GET("/jil8885/API-for-ERICA/light/calendar/master.json")
    fun getEvent() : Observable<EventsJson>
}