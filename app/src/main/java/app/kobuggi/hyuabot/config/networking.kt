package app.kobuggi.hyuabot.config

import app.kobuggi.hyuabot.BuildConfig

fun getServerURL(): String {
    return BuildConfig.server_url
}