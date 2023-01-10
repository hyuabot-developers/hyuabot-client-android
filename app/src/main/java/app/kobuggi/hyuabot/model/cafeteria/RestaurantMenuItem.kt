package app.kobuggi.hyuabot.model.cafeteria

import com.google.gson.annotations.SerializedName

data class RestaurantMenuItem(
    @SerializedName("food") val food: String,
    @SerializedName("price") val price: String,
)