package app.kobuggi.hyuabot.ui.readingRoom
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ReadingRoomPageQuery
import app.kobuggi.hyuabot.databinding.FragmentReadingRoomBinding
import app.kobuggi.hyuabot.service.alarm.AlarmFunction
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class ReadingRoomFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentReadingRoomBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<ReadingRoomViewModel>()
    private lateinit var roomListAdapter: ReadingRoomListAdapter
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Toast.makeText(requireContext(), R.string.reading_room_noti_allowed, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), R.string.reading_room_noti_denied, Toast.LENGTH_SHORT).show()
        }
    }
    private lateinit var alarmFunction: AlarmFunction

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        alarmFunction = AlarmFunction(requireContext())
        roomListAdapter = ReadingRoomListAdapter(requireContext(), ::onClickReadingRoom, emptyList(), emptySet())
        binding.apply {
            readingRoomSwipeRefreshLayout.setOnRefreshListener {
                viewModel.campusID.observe(viewLifecycleOwner) {
                    viewModel.fetchRooms(it)
                }
            }
            readingRoomRecyclerView.apply {
                setHasFixedSize(true)
                adapter = roomListAdapter
                layoutManager = LinearLayoutManager(context)
            }
            readingRoomAlarm3Hour.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.READING_ROOM_ALARM_TOGGLE, type = AnalyticsContentType.TOGGLE, name = "3hour")
                callAlarm(R.string.reading_room_alarm_3_hour, 3)
            }
            readingRoomAlarm4Hour.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.READING_ROOM_ALARM_TOGGLE, type = AnalyticsContentType.TOGGLE, name = "4hour")
                callAlarm(R.string.reading_room_alarm_4_hour, 4)
            }
            readingRoomAlarmCancel.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.READING_ROOM_ALARM_TOGGLE, type = AnalyticsContentType.TOGGLE, name = "cancel")
                alarmFunction.cancelAlarm(R.string.reading_room_alarm_3_hour)
                alarmFunction.cancelAlarm(R.string.reading_room_alarm_4_hour)
                viewModel.setExtendNotificationTime(null)
                readingRoomAlarmCancel.visibility = View.GONE
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        askNotificationPermission()
        viewModel.apply {
            campusID.observe(viewLifecycleOwner) {
                fetchRooms(it)
            }
            rooms.observe(viewLifecycleOwner) {
                binding.readingRoomSwipeRefreshLayout.isRefreshing = false
                roomListAdapter.updateData(it)
                if (it.isNotEmpty()) {
                    // The updatedAt field is in UTC format and converted to LocalDateTime
                    binding.readingRoomUpdateTime.text = getString(
                        R.string.reading_room_update_time,
                        it[0].updatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    )
                } else {
                    binding.readingRoomUpdateTime.text = getString(R.string.reading_room_update_time, LocalDateTime.now().toString())
                }
            }
            notificationList.observe(viewLifecycleOwner) {
                Log.d("ReadingRoomFragment", "Notification list updated: $it")
                roomListAdapter.updateNotifications(it.mapNotNull { item -> item.split("_").getOrNull(2)?.toIntOrNull() }.toSet())
            }
            extendNotificationTime.observe(viewLifecycleOwner) {
                if (!it.isNullOrEmpty()) {
                    binding.readingRoomRunningAlarm.apply {
                        text = getString(R.string.reading_room_alarm_format, it)
                        visibility = View.VISIBLE
                    }
                    binding.readingRoomAlarmCancel.visibility = View.VISIBLE
                } else {
                    binding.readingRoomRunningAlarm.visibility = View.GONE
                    binding.readingRoomAlarmCancel.visibility = View.GONE
                }
            }
            queryError.observe(viewLifecycleOwner) {
                it?.let { Toast.makeText(requireContext(), getString(R.string.reading_room_error), Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(POST_NOTIFICATIONS)) {
                Snackbar.make(binding.root, R.string.reading_room_noti_rationale, Snackbar.LENGTH_LONG)
                    .setAction(R.string.reading_room_noti_settings) {
                        requestPermissionLauncher.launch(POST_NOTIFICATIONS)
                    }
                    .show()
            } else {
                requestPermissionLauncher.launch(POST_NOTIFICATIONS)
            }
        }
    }

    private fun onClickReadingRoom(room: ReadingRoomPageQuery.ReadingRoom, subscribe: Boolean) {
        viewModel.viewModelScope.launch {
            viewModel.toggleReadingRoomNotification(requireContext(), room.seq, subscribe)
        }
    }

    private fun callAlarm(alarmResId: Int, hours: Long) {
        val alarmManager = ContextCompat.getSystemService(requireContext(), AlarmManager::class.java)
        alarmFunction.cancelAlarm(R.string.reading_room_alarm_3_hour)
        alarmFunction.cancelAlarm(R.string.reading_room_alarm_4_hour)
        if (alarmManager == null) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            return
        }

        val alarmTime = LocalDateTime.now().plusHours(hours).minusMinutes(10).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        alarmFunction.callAlarm(alarmTime, alarmResId, getString(R.string.reading_room_alarm_extend))
        viewModel.setExtendNotificationTime(alarmTime)
    }
}
