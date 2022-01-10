package app.kobuggi.hyuabot.di

import app.kobuggi.hyuabot.data.remote.api.ApplicationAPI
import org.koin.dsl.module
import retrofit2.Retrofit

val apiModule = module{
    single(createdAtStart = false){get<Retrofit>().create(ApplicationAPI::class.java)}
}