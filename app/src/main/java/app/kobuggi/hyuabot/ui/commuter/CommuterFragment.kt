package app.kobuggi.hyuabot.ui.commuter

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.ui.common.compose.HyuabotTheme
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay

/** 통학버스: 방향·노선별 시간표와 버스 위치(시간표 보간), 내 근처 정류장 강조, 지도 앱으로 길찾기. */
@AndroidEntryPoint
class CommuterFragment @Inject constructor() : Fragment() {
    private val viewModel by viewModels<CommuterViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { HyuabotTheme { CommuterContent() } }
        }

    @Composable
    private fun CommuterContent() {
        val context = LocalContext.current
        val state by viewModel.state.collectAsStateWithLifecycle()
        val lifecycleOwner = LocalLifecycleOwner.current
        val fixtureQuery = arguments?.getString("commuterFixture")?.takeIf { BuildConfig.DEBUG }

        LaunchedEffect(Unit) { viewModel.start(fixtureQuery, BuildConfig.DEBUG) }

        // 화면이 보이는 동안 60초마다, 다시 앞으로 올 때(STARTED 진입)도 같은 처리를 한다.
        LaunchedEffect(lifecycleOwner) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tick()
                while (true) {
                    delay(CLOCK_INTERVAL_MS)
                    viewModel.tick()
                }
            }
        }

        // 위치 권한은 처음 들어올 때 한 번만 요청한다. 허용하면 위치만 다시 받는다. 거부해도 안내 문구는 없다.
        val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.any { it }) viewModel.refreshLocation()
        }
        LaunchedEffect(Unit) {
            // 디버그 픽스처는 위치를 쓰지 않으므로 권한도 묻지 않는다. 앱을 통틀어 한 번만 묻는다(iOS .notDetermined와 같음).
            val prefs = context.getSharedPreferences("commuter", Context.MODE_PRIVATE)
            if (!prefs.getBoolean("location_asked", false) && fixtureQuery == null) {
                prefs.edit().putBoolean("location_asked", true).apply()
                if (!hasLocationPermission(context)) {
                    permission.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
                }
            }
        }

        val installed = remember(context) { installedMapApps(context) }
        val actions = remember(viewModel) {
            CommuterActions(
                onBack = { findNavController().popBackStack() },
                onDirection = {
                    AnalyticsManager.logSelect(AnalyticsItem.COMMUTER_SELECT_DIRECTION, name = it.name)
                    viewModel.selectDirection(it)
                },
                onRoute = {
                    AnalyticsManager.logSelect(AnalyticsItem.COMMUTER_SELECT_ROUTE, name = it)
                    viewModel.selectRoute(it)
                },
                onReset = {
                    AnalyticsManager.logSelect(AnalyticsItem.COMMUTER_RESET_NEAREST)
                    viewModel.resetToNearest()
                },
                onExpand = {
                    AnalyticsManager.logSelect(AnalyticsItem.COMMUTER_EXPAND_STOPS)
                    viewModel.expand()
                },
                onRetry = { viewModel.load() },
                onRefresh = { viewModel.load(refresh = true) },
                onOpenMap = viewModel::openSheet,
                onCloseSheet = viewModel::closeSheet,
                onPickMap = { app, stop ->
                    AnalyticsManager.logSelect(AnalyticsItem.COMMUTER_SELECT_MAP_APP, type = AnalyticsContentType.LIST_ITEM, name = app.name)
                    // 앱을 고르면 시트를 닫은 뒤 연다.
                    viewModel.closeSheet()
                    openMap(context, app, stop)
                },
            )
        }
        CommuterScreen(state, installed, actions)
    }

    private fun hasLocationPermission(context: Context) =
        listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION).any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    private fun installedMapApps(context: Context): Set<CommuterMapApp> = buildSet {
        val pm = context.packageManager
        if (pm.getLaunchIntentForPackage(NAVER_PACKAGE) != null) add(CommuterMapApp.Naver)
        if (pm.getLaunchIntentForPackage(KAKAO_PACKAGE) != null) add(CommuterMapApp.Kakao)
    }

    private fun openMap(context: Context, app: CommuterMapApp, stop: CommuterStop) {
        val coordinate = "${stop.latitude},${stop.longitude}"
        val google = "https://www.google.com/maps/dir/?api=1&destination=$coordinate&travelmode=transit".toUri()
        val uri: Uri = when (app) {
            CommuterMapApp.Google -> google
            CommuterMapApp.Naver -> Uri.Builder().scheme("nmap").authority("route").appendPath("public")
                .appendQueryParameter("dlat", stop.latitude.toString())
                .appendQueryParameter("dlng", stop.longitude.toString())
                .appendQueryParameter("dname", stop.name)
                .appendQueryParameter("appname", context.packageName)
                .build()
            CommuterMapApp.Kakao -> "kakaomap://route?ep=$coordinate&by=PUBLICTRANSIT".toUri()
        }
        // 앱 열기에 실패하면 Google 지도 URL(브라우저로도 열린다)로 대신한다.
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }.onFailure {
            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, google)) }
        }
    }

    private companion object {
        const val CLOCK_INTERVAL_MS = 60_000L
        const val NAVER_PACKAGE = "com.nhn.android.nmap"
        const val KAKAO_PACKAGE = "net.daum.android.map"
    }
}
