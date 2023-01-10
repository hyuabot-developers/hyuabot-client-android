package app.kobuggi.hyuabot.model.cafeteria

import com.google.gson.annotations.SerializedName

data class RestaurantTimeItem(
    @SerializedName("time") val timeType: String,
    @SerializedName("menu") val menu: List<RestaurantMenuItem>,
)
