package app.kobuggi.hyuabot.tile

import android.content.Context
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.MultiButtonLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.TileBuilders
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService

@OptIn(ExperimentalHorologistApi::class)
class ShuttleTileService: SuspendingTileService() {
    private val resourceVersion = "0"
    override fun onCreate() {
        super.onCreate()
    }

    override suspend fun tileRequest(requestParams: TileRequest): TileBuilders.Tile {
        return TileBuilders.Tile.Builder()
            .setResourcesVersion(resourceVersion)
            .setTileTimeline(
                TimelineBuilders.Timeline.fromLayoutElement(getShuttleTileLayout(this, requestParams.deviceConfiguration))
            )
            .build()
    }

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion(resourceVersion)
            .build()
    }

    companion object {
        private val stops = listOf(
            "기숙사",
            "셔틀콕",
            "한대앞",
            "예술인",
            "중앙역",
        )

        private val emptyClickable = ModifiersBuilders.Clickable.Builder().build()

        fun getShuttleTileLayout(
            context: Context,
            deviceConfiguration: DeviceParametersBuilders.DeviceParameters
        ) = PrimaryLayout.Builder(deviceConfiguration)
            .setResponsiveContentInsetEnabled(true)
            .setContent(
                MultiButtonLayout.Builder()
                    .apply {
                        stops.forEach { stop -> addButtonContent(getShuttleButtonLayout(context, stop)) }
                    }
                    .build()
            )
            .build()

        private fun getShuttleButtonLayout(context: Context, name: String, clickable: ModifiersBuilders.Clickable = emptyClickable): Button {
            return Button.Builder(context, clickable)
                .setContentDescription(name)
                .setTextContent(name, Typography.TYPOGRAPHY_CAPTION1)
                .build()
        }
    }
}
