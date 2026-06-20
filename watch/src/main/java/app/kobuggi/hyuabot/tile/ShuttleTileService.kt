package app.kobuggi.hyuabot.tile

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ActionBuilders.LaunchAction
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
import app.kobuggi.hyuabot.R
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService

@OptIn(ExperimentalHorologistApi::class)
class ShuttleTileService: SuspendingTileService() {
    private val resourceVersion = "0"

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
            ShuttleStop("dormitory", R.string.stop_dormitory),
            ShuttleStop("shuttlecock", R.string.stop_shuttlecock),
            ShuttleStop("station", R.string.stop_station),
            ShuttleStop("terminal", R.string.stop_terminal),
            ShuttleStop("jungang", R.string.stop_jungang),
        )

        private fun getClickable(action: LaunchAction) = ModifiersBuilders.Clickable.Builder()
            .setOnClick(action)
            .build()

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

        private fun getShuttleButtonLayout(context: Context, stop: ShuttleStop): Button {
            val name = context.getString(stop.labelRes)
            val launchAction = LaunchAction.Builder()
                .setAndroidActivity(
                    ActionBuilders.AndroidActivity.Builder()
                        .setPackageName(context.packageName)
                        .setClassName("app.kobuggi.hyuabot.presentation.MainActivity")
                        .addKeyToExtraMapping("stopID", ActionBuilders.stringExtra(stop.id))
                        .build()
                ).build()
            return Button.Builder(context, getClickable(launchAction))
                .setContentDescription(name)
                .setTextContent(name, Typography.TYPOGRAPHY_CAPTION1)
                .build()
        }

        private data class ShuttleStop(val id: String, val labelRes: Int)
    }
}
