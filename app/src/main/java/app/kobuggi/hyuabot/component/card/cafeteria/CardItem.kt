package app.kobuggi.hyuabot.component.card.cafeteria

import app.kobuggi.hyuabot.model.cafeteria.RestaurantItemResponse

data class CardItem (
    val timetypeID: Int,
    var restaurantList: List<RestaurantItemResponse>
)