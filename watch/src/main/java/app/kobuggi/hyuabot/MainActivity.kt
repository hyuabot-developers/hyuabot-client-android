package app.kobuggi.hyuabot

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import app.kobuggi.hyuabot.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.sqrt

@AndroidEntryPoint
class MainActivity: FragmentActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel: MainViewModel by viewModels()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val location = locationResult.lastLocation
            subscribeData(location)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        checkLocationPermission()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        viewModel.fetchData()
        viewModel.start()
    }

    private fun checkLocationPermission() {
        if (
            ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "가까운 정류장을 찾기 위해 위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION), 1)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION
                ), 1)
            }
        }
    }

    private fun subscribeData(location: Location?) {
        val now = LocalTime.now()
        if (location == null) {
            viewModel.result.observe(this) { timetable ->
                val shuttle = timetable.filter { it.stop == "shuttlecock_o" }
                val shuttleForStation = shuttle.filter { it.tag == "DH" || it.tag == "DJ" || it.tag == "C" }
                val shuttleForTerminal = shuttle.filter { it.tag == "DY" || it.tag == "C" }
                val shuttleForJungangStation = shuttle.filter { it.tag == "DJ" }

                binding.apply {
                    shuttleCurrentStop.apply {
                        text = getString(R.string.shuttle_tab_shuttlecock_out)
                        textSize = 25F
                    }
                    shuttleDestinationFirst.text = getString(R.string.shuttle_header_bound_for_station)
                    shuttleDestinationSecond.text = getString(R.string.shuttle_header_bound_for_terminal)
                    shuttleDestinationThird.text = getString(R.string.shuttle_header_bound_for_jungang_station)
                    shuttleRemainingTimeFirst.text = if (shuttleForStation.isNotEmpty()) {
                        val time = LocalTime.parse(shuttleForStation[0].time)
                        val remainingTime = time.minusHours(now.hour.toLong()).minusMinutes(now.minute.toLong())
                        getString(R.string.shuttle_remaining_format, (remainingTime.hour * 60 + remainingTime.minute).toString())
                    } else "-"
                    shuttleRemainingTimeSecond.text = if (shuttleForTerminal.isNotEmpty()) {
                        val time = LocalTime.parse(shuttleForTerminal[0].time)
                        val remainingTime = time.minusHours(now.hour.toLong()).minusMinutes(now.minute.toLong())
                        getString(R.string.shuttle_remaining_format, (remainingTime.hour * 60 + remainingTime.minute).toString())
                    } else "-"
                    shuttleRemainingTimeThird.text = if (shuttleForJungangStation.isNotEmpty()) {
                        val time = LocalTime.parse(shuttleForJungangStation[0].time)
                        val remainingTime = time.minusHours(now.hour.toLong()).minusMinutes(now.minute.toLong())
                        getString(R.string.shuttle_remaining_format, (remainingTime.hour * 60 + remainingTime.minute).toString())
                    } else "-"
                }
            }
            return
        }
        viewModel.stopInfo.observe(this) {stops ->
            if (stops.isNotEmpty()) {
                val nearestStop = stops.mapIndexed { index, stopItem ->
                    Pair(stopItem, calculateDistance(stopItem, location))
                }.minByOrNull { it.second }?.first
                when(nearestStop?.name) {
                    "dormitory_o" -> {
                        viewModel.result.observe(this) { timetable ->
                            val shuttle = timetable.filter { it.stop == "dormitory_o" }
                            val shuttleForStation = shuttle.filter { it.tag == "DH" || it.tag == "DJ" || it.tag == "C" }
                            val shuttleForTerminal = shuttle.filter { it.tag == "DY" || it.tag == "C" }
                            val shuttleForJungangStation = shuttle.filter { it.tag == "DJ" }

                            binding.apply {
                                shuttleCurrentStop.apply {
                                    text = getString(R.string.shuttle_tab_dormitory_out)
                                    textSize = 25F
                                }
                                shuttleDestinationFirst.text = getString(R.string.shuttle_header_bound_for_station)
                                shuttleDestinationSecond.text = getString(R.string.shuttle_header_bound_for_terminal)
                                shuttleDestinationThird.text = getString(R.string.shuttle_header_bound_for_jungang_station)
                                shuttleRemainingTimeFirst.text = if (shuttleForStation.isNotEmpty()) {
                                    val time = LocalTime.parse(shuttleForStation[0].time)
                                    val remainingTime = time.minusHours(now.hour.toLong()).minusMinutes(now.minute.toLong())
                                    getString(R.string.shuttle_remaining_format, (remainingTime.hour * 60 + remainingTime.minute).toString())
                                } else "-"
                                shuttleRemainingTimeSecond.text = if (shuttleForTerminal.isNotEmpty()) {
                                    val time = LocalTime.parse(shuttleForTerminal[0].time)
                                    val remainingTime = time.minusHours(now.hour.toLong()).minusMinutes(now.minute.toLong())
                                    getString(R.string.shuttle_remaining_format, (remainingTime.hour * 60 + remainingTime.minute).toString())
                                } else "-"
                                shuttleRemainingTimeThird.text = if (shuttleForJungangStation.isNotEmpty()) {
                                    val time = LocalTime.parse(shuttleForJungangStation[0].time)
                                    val remainingTime = time.minusHours(now.hour.toLong()).minusMinutes(now.minute.toLong())
                                    getString(R.string.shuttle_remaining_format, (remainingTime.hour * 60 + remainingTime.minute).toString())
                                } else "-"
                            }
                        }
                    }
                    "shuttlecock_o" -> {
                        viewModel.result.observe(this) { timetable ->
                            val shuttle = timetable.filter { it.stop == "shuttlecock_o" }
                            val shuttleForStation = shuttle.filter { it.tag == "DH" || it.tag == "DJ" || it.tag == "C" }
                            val shuttleForTerminal = shuttle.filter { it.tag == "DY" || it.tag == "C" }
                            val shuttleForJungangStation = shuttle.filter { it.tag == "DJ" }

                            binding.apply {
                                shuttleCurrentStop.apply {
                                    text = getString(R.string.shuttle_tab_shuttlecock_out)
                                    textSize = 25F
                                }
                                shuttleDestinationFirst.text = getString(R.string.shuttle_header_bound_for_station)
                                shuttleDestinationSecond.text = getString(R.string.shuttle_header_bound_for_terminal)
                                shuttleDestinationThird.text = getString(R.string.shuttle_header_bound_for_jungang_station)
                                shuttleRemainingTimeFirst.text = if (shuttleForStation.isNotEmpty()) {
                                    val time = LocalTime.parse(shuttleForStation[0].time)
                                    val remainingTime = time.minusHours(now.hour.toLong()).minusMinutes(now.minute.toLong())
                                    getString(R.string.shuttle_remaining_format, (remainingTime.hour * 60 + remainingTime.minute).toString())
                                } else "-"
                                shuttleRemainingTimeSecond.text = if (shuttleForTerminal.isNotEmpty()) {
                                    val time = LocalTime.parse(shuttleForTerminal[0].time)
                                    val remainingTime = time.minusHours(now.hour.toLong()).minusMinutes(now.minute.toLong())
                                    getString(R.string.shuttle_remaining_format, (remainingTime.hour * 60 + remainingTime.minute).toString())
                                } else "-"
                                shuttleRemainingTimeThird.text = if (shuttleForJungangStation.isNotEmpty()) {
                                    val time = LocalTime.parse(shuttleForJungangStation[0].time)
                                    val remainingTime = time.minusHours(now.hour.toLong()).minusMinutes(now.minute.toLong())
                                    getString(R.string.shuttle_remaining_format, (remainingTime.hour * 60 + remainingTime.minute).toString())
                                } else "-"
                            }
                        }
                    }
                    "station" -> {
                        viewModel.result.observe(this) { timetable ->
                            val shuttle = timetable.filter { it.stop == "station" }
                            val shuttleForTerminal = shuttle.filter { it.tag == "C" }
                            val shuttleForJungangStation = shuttle.filter { it.tag == "DJ" }

                            binding.apply {
                                shuttleCurrentStop.apply {
                                    text = getString(R.string.shuttle_tab_station)
                                    textSize = 25F
                                }
                                shuttleDestinationFirst.text = getString(R.string.shuttle_header_bound_for_dormitory)
                                shuttleDestinationSecond.text = getString(R.string.shuttle_header_bound_for_terminal)
                                shuttleDestinationThird.text = getString(R.string.shuttle_header_bound_for_jungang_station)
                                shuttleRemainingTimeFirst.text = if (shuttle.isNotEmpty()) {
                                    val time = LocalTime.parse(shuttle[0].time)
                                    val remainingTime = time.minusHours(now.hour.toLong()).minusMinutes(now.minute.toLong())
                                    getString(R.string.shuttle_remaining_format, (remainingTime.hour * 60 + remainingTime.minute).toString())
                                } else "-"
                                shuttleRemainingTimeSecond.text = if (shuttleForTerminal.isNotEmpty()) {
                                    val time = LocalTime.parse(shuttleForTerminal[0].time)
                                    val remainingTime = time.minusHours(now.hour.toLong()).minusMinutes(now.minute.toLong())
                                    getString(R.string.shuttle_remaining_format, (remainingTime.hour * 60 + remainingTime.minute).toString())
                                } else "-"
                                shuttleRemainingTimeThird.text = if (shuttleForJungangStation.isNotEmpty()) {
                                    val time = LocalTime.parse(shuttleForJungangStation[0].time)
                                    val remainingTime = time.minusHours(now.hour.toLong()).minusMinutes(now.minute.toLong())
                                    getString(R.string.shuttle_remaining_format, (remainingTime.hour * 60 + remainingTime.minute).toString())
                                } else "-"
                            }
                        }
                    }
                    "terminal" -> {
                        viewModel.result.observe(this) { timetable ->
                            val shuttle = timetable.filter { it.stop == "terminal" }

                            binding.apply {
                                shuttleCurrentStop.apply {
                                    text = getString(R.string.shuttle_tab_terminal)
                                    textSize = 25F
                                }
                                shuttleDestinationFirst.text = getString(R.string.shuttle_header_bound_for_dormitory)
                                shuttleDestinationSecond.text = "-"
                                shuttleDestinationThird.text = "-"
                                shuttleRemainingTimeSecond.text = "-"
                                shuttleRemainingTimeThird.text = "-"
                                shuttleRemainingTimeFirst.text = if (shuttle.isNotEmpty()) {
                                    val time = LocalTime.parse(shuttle[0].time)
                                    val remainingTime = time.minusHours(now.hour.toLong()).minusMinutes(now.minute.toLong())
                                    getString(R.string.shuttle_remaining_format, (remainingTime.hour * 60 + remainingTime.minute).toString())
                                } else "-"
                            }
                        }
                    }
                    "jungang_stn" -> {
                        viewModel.result.observe(this) { timetable ->
                            val shuttle = timetable.filter { it.stop == "jungang_stn" }

                            binding.apply {
                                shuttleCurrentStop.apply {
                                    text = getString(R.string.shuttle_tab_jungang_station)
                                    textSize = 25F
                                }
                                shuttleDestinationFirst.text = getString(R.string.shuttle_header_bound_for_dormitory)
                                shuttleDestinationSecond.text = "-"
                                shuttleDestinationThird.text = "-"
                                shuttleRemainingTimeSecond.text = "-"
                                shuttleRemainingTimeThird.text = "-"
                                shuttleRemainingTimeFirst.text = if (shuttle.isNotEmpty()) {
                                    val time = LocalTime.parse(shuttle[0].time)
                                    val remainingTime = time.minusHours(now.hour.toLong()).minusMinutes(now.minute.toLong())
                                    getString(R.string.shuttle_remaining_format, (remainingTime.hour * 60 + remainingTime.minute).toString())
                                } else "-"
                            }
                        }
                    }
                    "shuttlecock_i" -> {
                        viewModel.result.observe(this) { timetable ->
                            val shuttle = timetable.filter { it.stop == "shuttlecock_i" }

                            binding.apply {
                                shuttleCurrentStop.apply {
                                    text = getString(R.string.shuttle_tab_shuttlecock_in)
                                    textSize = 16F
                                }
                                shuttleDestinationFirst.text = getString(R.string.shuttle_header_bound_for_dormitory)
                                shuttleDestinationSecond.text = "-"
                                shuttleDestinationThird.text = "-"
                                shuttleRemainingTimeSecond.text = "-"
                                shuttleRemainingTimeThird.text = "-"
                                shuttleRemainingTimeFirst.text = if (shuttle.isNotEmpty()) {
                                    val time = LocalTime.parse(shuttle[0].time)
                                    val remainingTime = time.minusHours(now.hour.toLong()).minusMinutes(now.minute.toLong())
                                    getString(R.string.shuttle_remaining_format, (remainingTime.hour * 60 + remainingTime.minute).toString())
                                } else "-"
                            }
                        }
                    }
                    else -> {
                        binding.apply {
                            shuttleCurrentStop.apply {
                                getString(R.string.shuttle_tab_shuttlecock_out)
                                textSize = 25F
                            }
                            shuttleDestinationFirst.text = getString(R.string.shuttle_header_bound_for_station)
                            shuttleDestinationSecond.text = getString(R.string.shuttle_header_bound_for_terminal)
                            shuttleDestinationThird.text = getString(R.string.shuttle_header_bound_for_jungang_station)
                        }
                    }
                }
            }
        }
    }

    private fun calculateDistance(stopItem: ShuttleRealtimePageQuery.Stop, location: Location): Double {
        val distance = sqrt(
            (stopItem.latitude - location.latitude) * (stopItem.latitude - location.latitude) +
                (stopItem.longitude - location.longitude) * (stopItem.longitude - location.longitude)
        )
        return distance
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).apply {
                setMinUpdateDistanceMeters(0F)
                setWaitForAccurateLocation(true)
            }.build(),
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}
