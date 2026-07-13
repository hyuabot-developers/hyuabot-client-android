package app.kobuggi.hyuabot.ui.menu

data class MenuItem(
    val iconResource: Int,
    val titleResource: Int,
    val analyticsName: String,
    val sectionTitleResource: Int? = null,
)
