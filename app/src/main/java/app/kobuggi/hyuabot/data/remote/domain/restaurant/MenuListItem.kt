package app.kobuggi.hyuabot.data.remote.domain.restaurant

import app.kobuggi.hyuabot.model.MenuItem

data class MenuListItem(
    val menuItem: MenuItem,
    val key: String,
    val visible: Boolean
)