package app.kobuggi.hyuabot.data.remote.domain.restaurant

data class RestaurantItem(
    val MenuList: Map<String, List<MenuItem>>,
    val Name: String,
    val Time: String
)