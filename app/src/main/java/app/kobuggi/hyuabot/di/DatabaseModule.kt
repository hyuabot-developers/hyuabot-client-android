package app.kobuggi.hyuabot.di

import app.kobuggi.hyuabot.data.database.AppDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {
    single { AppDatabase.getInstance(androidApplication()) }
    single(createdAtStart = false) {get<AppDatabase>().getPhoneNumberDao()}
}