package app.kobuggi.hyuabot.config

import app.kobuggi.hyuabot.model.Shuttle
import io.reactivex.Single
import retrofit2.http.GET

interface NetworkService {
    @GET("/app/shuttle")
    fun getShuttleAll() : Single<Shuttle>
}