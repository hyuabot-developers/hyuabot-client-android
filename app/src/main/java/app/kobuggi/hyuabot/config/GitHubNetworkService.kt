package app.kobuggi.hyuabot.config

import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.model.*
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface GitHubNetworkService {
    companion object{
        const val SERVER_URL = "https://raw.githubusercontent.com"
    }
    @GET("/jil8885/API-for-ERICA/light/calendar/master.json")
    fun getEvent() : Observable<EventsJson>
}