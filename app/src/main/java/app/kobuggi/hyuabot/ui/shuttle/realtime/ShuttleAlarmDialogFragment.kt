package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.DialogShuttleAlarmBinding
import app.kobuggi.hyuabot.service.alarm.ShuttleAlarmService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ShuttleAlarmDialogFragment : BottomSheetDialogFragment() {

    private val binding by lazy { DialogShuttleAlarmBinding.inflate(layoutInflater) }
    private var pendingAlarmStart: (() -> Unit)? = null
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                pendingAlarmStart?.invoke()
            } else {
                Toast.makeText(requireContext(), R.string.shuttle_alarm_no_permission, Toast.LENGTH_SHORT).show()
            }
            pendingAlarmStart = null
        }

    companion object {
        private const val SHUTTLE_SHARE_SCHEME = "https"
        private const val SHUTTLE_SHARE_HOST = "hyuabot.app"
        private const val SHUTTLE_SHARE_PATH = "shuttle"
        private const val ARG_BOARDING_ID = "boarding_id"
        private const val ARG_BOARDING_NAME = "boarding_name"
        private const val ARG_BOARDING_LAT = "boarding_lat"
        private const val ARG_BOARDING_LNG = "boarding_lng"
        private const val ARG_MINUTES = "minutes"
        private const val ARG_DEPARTURE_TIME_MILLIS = "departure_time_millis"
        private const val ARG_ALARM_KEY = "alarm_key"
        private const val ARG_CHECKPOINT_NAMES = "checkpoint_names"
        private const val ARG_CHECKPOINT_TIMES = "checkpoint_times"
        private const val ARG_DEST_NAMES = "dest_names"
        private const val ARG_DEST_IDS = "dest_ids"
        private const val ARG_DEST_LATS = "dest_lats"
        private const val ARG_DEST_LNGS = "dest_lngs"
        private const val ARG_DEST_TIMES = "dest_times"

        fun newInstance(
            boardingStopId: String,
            boardingStopName: String,
            boardingStopLat: Double,
            boardingStopLng: Double,
            minutes: Int,
            departureTimeMillis: Long,
            alarmKey: String,
            checkpointNames: Array<String>,
            checkpointTimes: LongArray,
            destTimes: LongArray,
            destStops: List<ShuttleAlarmDestinationStop>
        ): ShuttleAlarmDialogFragment {
            return ShuttleAlarmDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_BOARDING_ID, boardingStopId)
                    putString(ARG_BOARDING_NAME, boardingStopName)
                    putDouble(ARG_BOARDING_LAT, boardingStopLat)
                    putDouble(ARG_BOARDING_LNG, boardingStopLng)
                    putInt(ARG_MINUTES, minutes)
                    putLong(ARG_DEPARTURE_TIME_MILLIS, departureTimeMillis)
                    putString(ARG_ALARM_KEY, alarmKey)
                    putStringArray(ARG_CHECKPOINT_NAMES, checkpointNames)
                    putLongArray(ARG_CHECKPOINT_TIMES, checkpointTimes)
                    putStringArray(ARG_DEST_NAMES, destStops.map { it.name }.toTypedArray())
                    putStringArray(ARG_DEST_IDS, destStops.map { it.id }.toTypedArray())
                    putDoubleArray(ARG_DEST_LATS, destStops.map { it.latitude }.toDoubleArray())
                    putDoubleArray(ARG_DEST_LNGS, destStops.map { it.longitude }.toDoubleArray())
                    putLongArray(ARG_DEST_TIMES, destTimes)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val args = requireArguments()
        val boardingId = args.getString(ARG_BOARDING_ID, "")
        val boardingName = args.getString(ARG_BOARDING_NAME, "")
        val boardingLat = args.getDouble(ARG_BOARDING_LAT)
        val boardingLng = args.getDouble(ARG_BOARDING_LNG)
        val minutes = args.getInt(ARG_MINUTES)
        val departureTimeMillis = args.getLong(ARG_DEPARTURE_TIME_MILLIS)
        val alarmKey = args.getString(ARG_ALARM_KEY, "")
        val checkpointNames = args.getStringArray(ARG_CHECKPOINT_NAMES) ?: emptyArray()
        val checkpointTimes = args.getLongArray(ARG_CHECKPOINT_TIMES) ?: LongArray(0)
        val destNames = args.getStringArray(ARG_DEST_NAMES) ?: emptyArray()
        val destIds = args.getStringArray(ARG_DEST_IDS) ?: emptyArray()
        val destLats = args.getDoubleArray(ARG_DEST_LATS) ?: DoubleArray(0)
        val destLngs = args.getDoubleArray(ARG_DEST_LNGS) ?: DoubleArray(0)
        val destTimes = args.getLongArray(ARG_DEST_TIMES) ?: LongArray(0)

        binding.boardingStopName.text = getString(R.string.shuttle_alarm_boarding_initial, minutes)
            .let { "$boardingName · $it" }

        if (ShuttleAlarmService.isBoardingAlarmActive(alarmKey)) {
            binding.boardingStartButton.text = getString(R.string.shuttle_alarm_cancel)
            binding.boardingStartButton.setOnClickListener {
                cancelAlarm(alarmKey)
                dismiss()
            }
        } else {
            binding.boardingStartButton.text = getString(R.string.shuttle_alarm_start)
            binding.boardingStartButton.setOnClickListener {
                startWithNotificationPermission {
                    startBoardingAlarm(alarmKey, boardingName, boardingLat, boardingLng, minutes, departureTimeMillis, checkpointNames, checkpointTimes)
                    dismiss()
                }
            }
        }

        if (destNames.isNotEmpty() || ShuttleAlarmService.isAlightingAlarmActive(alarmKey)) {
            binding.alightingCard.visibility = View.VISIBLE
            destNames.forEachIndexed { index, name ->
                val radio = RadioButton(requireContext()).apply {
                    id = View.generateViewId()
                    text = name
                    typeface = ResourcesCompat.getFont(requireContext(), R.font.godo)
                }
                binding.destinationRadioGroup.addView(radio)
                if (index == 0) binding.destinationRadioGroup.check(radio.id)
            }
            if (ShuttleAlarmService.isAlightingAlarmActive(alarmKey)) {
                binding.alightingStartButton.text = getString(R.string.shuttle_alarm_cancel)
                binding.alightingStartButton.setOnClickListener {
                    cancelAlarm(alarmKey)
                    dismiss()
                }
            } else {
                binding.alightingStartButton.text = getString(R.string.shuttle_alarm_start)
                binding.alightingStartButton.setOnClickListener {
                    startWithNotificationPermission {
                        val selectedId = binding.destinationRadioGroup.checkedRadioButtonId
                        val selectedIndex = (0 until binding.destinationRadioGroup.childCount).firstOrNull {
                            binding.destinationRadioGroup.getChildAt(it).id == selectedId
                        } ?: return@startWithNotificationPermission
                        if (selectedIndex < destLats.size && selectedIndex < destTimes.size) {
                            val alightingCheckpointNames = arrayOf(boardingName) + destNames.take(selectedIndex + 1)
                            val alightingCheckpointTimes = longArrayOf(departureTimeMillis) + destTimes.take(selectedIndex + 1)
                            startAlightingAlarm(
                                alarmKey,
                                destNames[selectedIndex],
                                destLats[selectedIndex],
                                destLngs[selectedIndex],
                                destTimes[selectedIndex],
                                minutes,
                                alightingCheckpointNames,
                                alightingCheckpointTimes
                            )
                            dismiss()
                        }
                    }
                }
            }
            binding.shareJourneyButton.setOnClickListener {
                val selectedIndex = selectedDestinationIndex() ?: return@setOnClickListener
                if (selectedIndex < destNames.size && selectedIndex < destTimes.size) {
                    shareJourney(
                        boardingId,
                        boardingName,
                        departureTimeMillis,
                        destIds.getOrNull(selectedIndex).orEmpty(),
                        destNames[selectedIndex],
                        destTimes[selectedIndex],
                    )
                }
            }
        }

        return binding.root
    }

    private fun selectedDestinationIndex(): Int? {
        val selectedId = binding.destinationRadioGroup.checkedRadioButtonId
        return (0 until binding.destinationRadioGroup.childCount).firstOrNull {
            binding.destinationRadioGroup.getChildAt(it).id == selectedId
        }
    }

    private fun shareJourney(
        boardingId: String,
        boardingName: String,
        departureTimeMillis: Long,
        destinationId: String,
        destinationName: String,
        arrivalTimeMillis: Long,
    ) {
        val shareUrl = Uri.Builder()
            .scheme(SHUTTLE_SHARE_SCHEME)
            .authority(SHUTTLE_SHARE_HOST)
            .appendPath(SHUTTLE_SHARE_PATH)
            .appendQueryParameter("stop", boardingId)
            .appendQueryParameter("to", destinationId)
            .build()
            .toString()
        val formatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("Asia/Seoul"))
        val text = getString(
            R.string.shuttle_share_journey_format,
            boardingName,
            formatter.format(Instant.ofEpochMilli(departureTimeMillis)),
            destinationName,
            formatter.format(Instant.ofEpochMilli(arrivalTimeMillis)),
            shareUrl,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.shuttle_share_journey_title)))
    }

    private fun startWithNotificationPermission(action: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(requireContext(), POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            action()
            return
        }

        pendingAlarmStart = action
        notificationPermissionLauncher.launch(POST_NOTIFICATIONS)
    }

    private fun startBoardingAlarm(
        alarmKey: String,
        stopName: String,
        stopLat: Double,
        stopLng: Double,
        minutes: Int,
        departureTimeMillis: Long,
        checkpointNames: Array<String>,
        checkpointTimes: LongArray
    ) {
        val intent = Intent(requireContext(), ShuttleAlarmService::class.java).apply {
            action = ShuttleAlarmService.ACTION_START_BOARDING
            putExtra(ShuttleAlarmService.EXTRA_STOP_NAME, stopName)
            putExtra(ShuttleAlarmService.EXTRA_STOP_LAT, stopLat)
            putExtra(ShuttleAlarmService.EXTRA_STOP_LNG, stopLng)
            putExtra(ShuttleAlarmService.EXTRA_MINUTES, minutes)
            putExtra(ShuttleAlarmService.EXTRA_DEPARTURE_TIME_MILLIS, departureTimeMillis)
            putExtra(ShuttleAlarmService.EXTRA_ALARM_KEY, alarmKey)
            putExtra(ShuttleAlarmService.EXTRA_CHECKPOINT_NAMES, checkpointNames)
            putExtra(ShuttleAlarmService.EXTRA_CHECKPOINT_TIMES_MILLIS, checkpointTimes)
        }
        ContextCompat.startForegroundService(requireContext(), intent)
    }

    private fun startAlightingAlarm(
        alarmKey: String,
        destName: String,
        destLat: Double,
        destLng: Double,
        arrivalTimeMillis: Long,
        minutes: Int,
        checkpointNames: Array<String>,
        checkpointTimes: LongArray
    ) {
        val intent = Intent(requireContext(), ShuttleAlarmService::class.java).apply {
            action = ShuttleAlarmService.ACTION_START_ALIGHTING
            putExtra(ShuttleAlarmService.EXTRA_DEST_STOP_NAME, destName)
            putExtra(ShuttleAlarmService.EXTRA_DEST_STOP_LAT, destLat)
            putExtra(ShuttleAlarmService.EXTRA_DEST_STOP_LNG, destLng)
            putExtra(ShuttleAlarmService.EXTRA_DEST_ARRIVAL_TIME_MILLIS, arrivalTimeMillis)
            putExtra(ShuttleAlarmService.EXTRA_MINUTES, minutes)
            putExtra(ShuttleAlarmService.EXTRA_ALARM_KEY, alarmKey)
            putExtra(ShuttleAlarmService.EXTRA_CHECKPOINT_NAMES, checkpointNames)
            putExtra(ShuttleAlarmService.EXTRA_CHECKPOINT_TIMES_MILLIS, checkpointTimes)
        }
        ContextCompat.startForegroundService(requireContext(), intent)
    }

    private fun cancelAlarm(alarmKey: String) {
        val intent = Intent(requireContext(), ShuttleAlarmService::class.java).apply {
            action = ShuttleAlarmService.ACTION_CANCEL
            putExtra(ShuttleAlarmService.EXTRA_ALARM_KEY, alarmKey)
        }
        ContextCompat.startForegroundService(requireContext(), intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {}
}
