package app.kobuggi.hyuabot.model.cafeteria

import com.google.gson.annotations.SerializedName

data class RestaurantListResponse(
    @SerializedName("restaurant") val restaurants: List<RestaurantItemResponse>,
)
