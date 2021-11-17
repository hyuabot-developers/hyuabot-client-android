package app.kobuggi.hyuabot.model

class RestaurantList : ArrayList<Restaurant>()

data class Restaurant(
    val MenuList: Map<String, List<MenuItem>>,
    val Name: String,
    val Time: String
)

data class MenuListItem(
    val menuItem: MenuItem,
    val key: String,
    val visible: Boolean
)

data class MenuItem(
    val Menu: String,
    val Price: String
)