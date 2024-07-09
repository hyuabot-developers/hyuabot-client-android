package app.kobuggi.hyuabot.ui.readingRoom

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import java.lang.Integer.parseInt
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class ReadingRoomFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentReadingRoomBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<ReadingRoomViewModel>()
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Snackbar.make(binding.root, R.string.reading_room_noti_allowed, Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(binding.root, R.string.reading_room_noti_denied, Snackbar.LENGTH_SHORT).show()
        }
    }
    private lateinit var alaramFunction: AlarmFunction

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        askNotificationPermission()
        val roomListAdapter = ReadingRoomListAdapter(requireContext(), ::onClickReadingRoom, emptyList(), emptySet())
        alaramFunction = AlarmFunction(requireContext())
        viewModel.apply {
            fetchRooms()
            rooms.observe(viewLifecycleOwner) {
                binding.readingRoomSwipeRefreshLayout.isRefreshing = false
                roomListAdapter.updateData(it)
                if (it.isNotEmpty()) {
                    // The updatedAt field is in UTC format and converted to LocalDateTime
                    val utcUpdatedAt = LocalDateTime.parse(it[0].updatedAt.split(".")[0]).atOffset(ZoneOffset.UTC)
                    val localUpdatedAt = utcUpdatedAt.atZoneSameInstant(ZoneOffset.systemDefault()).toLocalDateTime()
                    binding.readingRoomUpdateTime.text = getString(
                        R.string.reading_room_update_time,
                        localUpdatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    )
                } else {
                    binding.readingRoomUpdateTime.text = getString(R.string.reading_room_update_time, LocalDateTime.now().toString())
                }
            }
            notificationList.observe(viewLifecycleOwner) {
                Log.d("ReadingRoomFragment", "Notification list updated: $it")
                roomListAdapter.updateNotifications(it.map { item -> parseInt(item.split("_")[2]) }.toSet())
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
        }

        binding.apply {
            readingRoomSwipeRefreshLayout.setOnRefreshListener {
                viewModel.fetchRooms()
            }
            readingRoomRecyclerView.apply {
                setHasFixedSize(true)
                adapter = roomListAdapter
                layoutManager = LinearLayoutManager(context)
            }
            readingRoomAlarm3Hour.setOnClickListener {
                alaramFunction.cancelAlarm(R.string.reading_room_alarm_4_hour)
                val alarmTime = LocalDateTime.now().plusHours(3).minusMinutes(10).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                alaramFunction.callAlarm(alarmTime, R.string.reading_room_alarm_3_hour, getString(R.string.reading_room_alarm_extend))
                viewModel.setExtendNotificationTime(alarmTime)
            }
            readingRoomAlarm4Hour.setOnClickListener {
                alaramFunction.cancelAlarm(R.string.reading_room_alarm_3_hour)
                val alarmTime = LocalDateTime.now().plusHours(4).minusMinutes(10).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                alaramFunction.callAlarm(alarmTime, R.string.reading_room_alarm_4_hour, getString(R.string.reading_room_alarm_extend))
                viewModel.setExtendNotificationTime(alarmTime)
            }
            readingRoomAlarmCancel.setOnClickListener {
                alaramFunction.cancelAlarm(R.string.reading_room_alarm_3_hour)
                alaramFunction.cancelAlarm(R.string.reading_room_alarm_4_hour)
                viewModel.setExtendNotificationTime(null)
                readingRoomAlarmCancel.visibility = View.GONE
            }
        }

        return binding.root
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
            viewModel.toggleReadingRoomNotification(binding, room.id, subscribe)
        }
    }
}
