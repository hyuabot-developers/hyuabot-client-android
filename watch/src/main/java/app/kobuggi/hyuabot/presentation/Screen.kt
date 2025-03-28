package app.kobuggi.hyuabot.presentation

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object Detail : Screen("detail")
}
