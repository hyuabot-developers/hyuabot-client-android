package app.kobuggi.hyuabot.model.cafeteria

import com.google.gson.annotations.SerializedName

data class RestaurantItemResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("menu") val menu: List<RestaurantTimeItem>,
)
