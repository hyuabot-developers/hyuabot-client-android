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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        askNotificationPermission()
        val roomListAdapter = ReadingRoomListAdapter(requireContext(), ::onClickReadingRoom, emptyList(), emptySet())
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

    private fun onClickReadingRoom(room: ReadingRoomPageQuery.ReadingRoom) {
        viewModel.viewModelScope.launch {
            viewModel.toggleReadingRoomNotification(room.id)
        }
    }
}
