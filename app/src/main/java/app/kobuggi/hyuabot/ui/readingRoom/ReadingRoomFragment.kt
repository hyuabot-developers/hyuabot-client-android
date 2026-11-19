package app.kobuggi.hyuabot.ui.readingRoom

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.ui.common.coachmark.CoachmarkStep
import app.kobuggi.hyuabot.ui.common.coachmark.Coachmarks
import app.kobuggi.hyuabot.ui.common.coachmark.placeAt
import app.kobuggi.hyuabot.ui.common.coachmark.showCoachmarkOnce
import app.kobuggi.hyuabot.ui.common.compose.HyuabotTheme
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** 열람실: 좌석 현황(15초마다 자동 갱신), 빈자리 알림(FCM 토픽), 이용 종료 알림(알람). 알림 권한은 필요한 순간에 안내한다. */
@AndroidEntryPoint
class ReadingRoomFragment @Inject constructor() : Fragment() {
    private val viewModel by viewModels<ReadingRoomViewModel>()

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    /** 코치마크가 가리킬, Compose 요소 위에 겹친 보이지 않는 뷰. */
    private val anchors = mutableMapOf<ReadingRoomAnchor, View>()

    /** 정확한 알람 설정 화면에 다녀오는 중이면 true. 돌아오면(ON_RESUME) 보류한 예약을 이어서 한다. */
    private var awaitingExactAlarm = false

    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val job = viewModel.state.value.job
        if (granted && job != null) {
            proceed(job)
        } else {
            viewModel.setJob(null)
            if (!granted) viewModel.showMessage(ReadingRoomMessage.PermissionDenied)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val context = requireContext()
        val compose = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { HyuabotTheme { ReadingRoomContent() } }
        }
        val root = FrameLayout(context).apply {
            addView(compose, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
        ReadingRoomAnchor.entries.forEach { anchor ->
            val view = View(context).apply { importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO }
            anchors[anchor] = view
            root.addView(view, FrameLayout.LayoutParams(0, 0))
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fixtureQuery = arguments?.getString("readingRoomFixture")?.takeIf { BuildConfig.DEBUG }
        viewModel.start(fixtureQuery, BuildConfig.DEBUG)
        // 화면이 보이는 동안 15초마다 조용히 다시 불러온다. 다시 앞으로 오면(STARTED 진입) 즉시 한 번 부르고 다시 시작한다.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.refresh(manual = false)
                while (true) {
                    delay(ReadingRoomLogic.AUTO_REFRESH_MS)
                    viewModel.refresh(manual = false)
                }
            }
        }
        if (!viewModel.isFixture) scheduleCoachmark()
    }

    override fun onDestroyView() {
        anchors.clear()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        if (!awaitingExactAlarm) return
        awaitingExactAlarm = false
        val job = viewModel.state.value.job as? ReadingRoomJob.Alarm
        if (job != null && canScheduleExactAlarms()) viewModel.scheduleAlarm(job.hours) else viewModel.setJob(null)
    }

    /** 목록이 처음 보인 뒤 한 번만: 갱신 줄 → 첫 행 벨 → 이용 종료 알림 카드. */
    private fun scheduleCoachmark() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.first { it.rooms.isNotEmpty() && it.sheet == null }
            delay(COACHMARK_DELAY_MS)
            showCoachmarkOnce(userPreferencesRepository, Coachmarks.READING_ROOM) {
                listOfNotNull(
                    anchors[ReadingRoomAnchor.Status]?.let { v ->
                        CoachmarkStep({ v }, R.string.coachmark_reading_room_status_title, R.string.coachmark_reading_room_status_desc)
                    },
                    anchors[ReadingRoomAnchor.Bell]?.let { v ->
                        CoachmarkStep({ v }, R.string.coachmark_reading_room_bell_title, R.string.coachmark_reading_room_bell_desc)
                    },
                    anchors[ReadingRoomAnchor.Alarm]?.let { v ->
                        CoachmarkStep({ v }, R.string.coachmark_reading_room_alarm_title, R.string.coachmark_reading_room_alarm_desc)
                    },
                )
            }
        }
    }

    @Composable
    private fun ReadingRoomContent() {
        val state by viewModel.state.collectAsStateWithLifecycle()
        val actions = remember(viewModel) {
            ReadingRoomActions(
                onBack = { findNavController().popBackStack() },
                onCampus = { findNavController().navigate(R.id.settingFragment) },
                onRefresh = {
                    AnalyticsManager.logSelect(AnalyticsItem.READING_ROOM_REFRESH)
                    viewModel.refresh(manual = true)
                },
                onRetry = { viewModel.refresh(manual = true) },
                onBell = ::onBell,
                onAlarm = { hours ->
                    AnalyticsManager.logSelect(AnalyticsItem.READING_ROOM_ALARM_TOGGLE, type = AnalyticsContentType.TOGGLE, name = "${hours}hour")
                    request(ReadingRoomJob.Alarm(hours))
                },
                onCancelAlarm = {
                    AnalyticsManager.logSelect(AnalyticsItem.READING_ROOM_ALARM_TOGGLE, type = AnalyticsContentType.TOGGLE, name = "cancel")
                    viewModel.cancelAlarm()
                },
                onSheetPrimary = ::onSheetPrimary,
                onSheetSecondary = { viewModel.dismissSheet() },
                onSheetDismiss = { viewModel.dismissSheet() },
                onMessageShown = viewModel::consumeMessage,
                onAnchor = { anchor, bounds -> anchors[anchor]?.placeAt(bounds) },
            )
        }
        // 디버그 픽스처가 열어 둔 시트도 분석 이벤트를 남기지 않도록 사용자가 연 시트만 기록한다 (request에서 기록).
        ReadingRoomScreen(state, actions)
    }

    private fun onBell(seq: Int) {
        AnalyticsManager.logSelect(AnalyticsItem.READING_ROOM_ALARM_TOGGLE, type = AnalyticsContentType.TOGGLE, name = seq.toString())
        // 이미 켠 알림을 끄는 데는 권한이 필요 없다.
        if (seq in viewModel.state.value.subscribed) viewModel.toggleSubscription(seq) else request(ReadingRoomJob.Bell(seq))
    }

    /** 누른 순간에 권한을 확인한다: 있으면 바로, 처음이면 안내 시트 → 시스템 팝업, 영구 거부면 설정 안내 시트. */
    private fun request(job: ReadingRoomJob) {
        viewModel.setJob(job)
        when (notificationAccess()) {
            NotificationAccess.Granted -> proceed(job)
            NotificationAccess.Ask -> openSheet(ReadingRoomSheet.Perm)
            NotificationAccess.Denied -> openSheet(ReadingRoomSheet.Denied)
        }
    }

    private fun openSheet(sheet: ReadingRoomSheet, job: ReadingRoomJob? = null) {
        AnalyticsManager.logSelect(AnalyticsItem.READING_ROOM_PERMISSION_SHEET, name = sheet.name.lowercase())
        viewModel.openSheet(sheet, job)
    }

    /** 권한이 갖춰진 뒤 이어서 하는 동작. 이용 종료 알림은 정확한 알람 권한도 필요하다. */
    private fun proceed(job: ReadingRoomJob) {
        when (job) {
            is ReadingRoomJob.Bell -> viewModel.toggleSubscription(job.seq)
            is ReadingRoomJob.Alarm ->
                if (canScheduleExactAlarms()) viewModel.scheduleAlarm(job.hours) else openSheet(ReadingRoomSheet.Exact, job)
        }
    }

    /** 설정 화면이 없는 기기에서도 죽지 않게, 열지 못하면 앱 정보 화면으로 돌린다. 열었으면 true. */
    private fun openSettings(intent: Intent): Boolean =
        runCatching { startActivity(intent) }.isSuccess ||
            runCatching {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${requireContext().packageName}")))
            }.isSuccess

    private fun onSheetPrimary() {
        val sheet = viewModel.state.value.sheet ?: return
        val context = requireContext()
        when (sheet) {
            ReadingRoomSheet.Perm -> {
                AnalyticsManager.logSelect(AnalyticsItem.READING_ROOM_PERMISSION_SHEET, name = "perm_allow")
                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ASKED, true).apply()
                // 시트만 닫고 이어서 할 동작은 남겨 둔다. 허용하면 방금 누른 동작을 이어서 한다.
                viewModel.hideSheet()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermission.launch(POST_NOTIFICATIONS)
                } else {
                    viewModel.state.value.job?.let(::proceed)
                }
            }
            ReadingRoomSheet.Denied -> {
                viewModel.dismissSheet()
                openSettings(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
            }
            ReadingRoomSheet.Exact -> {
                viewModel.hideSheet()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    awaitingExactAlarm = openSettings(
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")),
                    )
                }
            }
        }
    }

    private fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val manager = ContextCompat.getSystemService(requireContext(), AlarmManager::class.java) ?: return false
        return manager.canScheduleExactAlarms()
    }

    private enum class NotificationAccess { Granted, Ask, Denied }

    private fun notificationAccess(): NotificationAccess {
        val context = requireContext()
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) return NotificationAccess.Granted
        // 13 미만은 시스템 팝업이 없으므로 꺼져 있으면 설정 안내뿐이다.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return NotificationAccess.Denied
        val asked = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ASKED, false)
        // 영구 거부: 이미 물어봤고 더는 시스템 팝업을 띄울 수 없다.
        return if (asked && !shouldShowRequestPermissionRationale(POST_NOTIFICATIONS)) NotificationAccess.Denied else NotificationAccess.Ask
    }

    private companion object {
        const val PREFS = "reading_room"
        const val KEY_ASKED = "notification_asked"
        const val COACHMARK_DELAY_MS = 400L
    }
}
