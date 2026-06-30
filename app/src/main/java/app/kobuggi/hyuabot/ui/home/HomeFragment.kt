package app.kobuggi.hyuabot.ui.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.HomePageQuery
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentHomeBinding
import app.kobuggi.hyuabot.databinding.ItemHomeRowBinding
import app.kobuggi.hyuabot.util.setSkeletonLoading
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import kotlin.math.ceil

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    private val viewModel: HomeViewModel by viewModels()
    private var selectedDeparture = HomeDeparture.DORMITORY
    private var selectedDestination = HomeDestination.STATION

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding.dateText.text = DateFormat.getDateInstance(DateFormat.FULL).format(Date())
        setupDestinationButtons()
        binding.movementDetail.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_shuttleRealtimeFragment)
        }
        binding.legacyShuttleButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_shuttleRealtimeFragment)
        }
        binding.mealDetail.setOnClickListener {
            val args = Bundle().apply {
                putString("tab", activeMealPeriod().tab)
            }
            findNavController().navigate(
                R.id.action_homeFragment_to_cafeteriaFragment,
                args,
            )
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.movementLoading.setSkeletonLoading(isLoading)
            binding.mealLoading.setSkeletonLoading(isLoading)
            binding.movementLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.mealLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        viewModel.data.observe(viewLifecycleOwner) { render(it) }
        viewModel.queryError.observe(viewLifecycleOwner) {
            it?.let { Toast.makeText(requireContext(), getString(R.string.shuttle_no_realtime_data), Toast.LENGTH_SHORT).show() }
        }
        viewModel.fetchData()
        return binding.root
    }

    private fun setupDestinationButtons() {
        binding.destinationGroup.removeAllViews()
        selectedDeparture.destinations.forEach { destination ->
            val button = MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
                id = View.generateViewId()
                text = getString(destination.titleRes)
                minHeight = resources.getDimensionPixelSize(R.dimen.home_destination_button_min_height)
                insetTop = 0
                insetBottom = 0
                isCheckable = true
                isAllCaps = false
                setTextColor(ContextCompat.getColor(requireContext(), R.color.hanyang_blue))
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                tag = destination
            }
            binding.destinationGroup.addView(button)
            if (destination == selectedDestination) binding.destinationGroup.check(button.id)
        }
        binding.destinationGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val destination = group.findViewById<View>(checkedId)?.tag as? HomeDestination ?: return@addOnButtonCheckedListener
            selectedDestination = destination
            render(viewModel.data.value)
        }
    }

    private fun render(data: HomePageQuery.Data?) {
        binding.routeText.text = getString(
            R.string.home_route_format,
            getString(selectedDeparture.titleRes),
            getString(selectedDestination.titleRes),
        )
        binding.mealTitle.text = activeMealPeriod().title(requireContext())
        renderMovement(data)
        renderMeals(data)
    }

    private fun renderMovement(data: HomePageQuery.Data?) {
        binding.movementContainer.removeAllViews()
        if (data == null) return

        val shuttleRows = shuttleEntries(data)
            .take(3)
            .map { entry ->
                HomeRow(
                    badge = routeBadge(entry.route.tag, entry.route.name),
                    title = getString(R.string.home_departure_format, compactTime(entry.time)),
                    subtitle = routeSubtitle(entry),
                    trailing = minutesUntil(entry.time)?.let { getString(R.string.home_minutes, it) } ?: getString(R.string.home_check),
                    tint = routeColor(entry.route.tag, entry.route.name),
                )
            }
        val busRows = busAlternatives(data).take(2)
        val rows = shuttleRows + busRows

        if (rows.isEmpty()) {
            addEmptyRow(binding.movementContainer, R.string.home_no_data_title, R.string.home_no_data_message)
        } else {
            rows.forEach { addHomeRow(binding.movementContainer, it) }
        }
    }

    private fun shuttleEntries(data: HomePageQuery.Data): List<HomePageQuery.Entry> {
        val route = selectedDeparture.routeTo(selectedDestination)
        val entries = data.shuttle.stops
            .firstOrNull { it.name == route.stop }
            ?.timetable
            ?.destination
            ?.firstOrNull { it.destination == route.destination }
            ?.entries
            .orEmpty()
        return route.filter?.let { entries.filter(it) } ?: entries
    }

    private fun busAlternatives(data: HomePageQuery.Data): List<HomeRow> {
        val routeIds = when (selectedDestination) {
            HomeDestination.STATION -> setOf(216000068)
            HomeDestination.TERMINAL -> setOf(216000016, 216000082, 216000102)
            HomeDestination.JUNGANG -> setOf(216000016, 216000082, 216000102)
            HomeDestination.DORMITORY -> setOf(216000068)
        }
        return data.bus
            .filter { it.route.seq in routeIds }
            .mapNotNull { item ->
                val minutes = item.arrival.firstOrNull()?.minutes ?: return@mapNotNull null
                HomeRow(
                    badge = item.route.name,
                    title = getString(R.string.home_bus_title_format, item.route.name),
                    subtitle = getString(R.string.home_bus_subtitle_format, item.stop.name),
                    trailing = getString(R.string.home_minutes, minutes),
                    tint = ContextCompat.getColor(requireContext(), R.color.blue_bus),
                )
            }
            .sortedBy { row -> row.trailing.filter(Char::isDigit).toIntOrNull() ?: Int.MAX_VALUE }
    }

    private fun renderMeals(data: HomePageQuery.Data?) {
        binding.mealContainer.removeAllViews()
        if (data == null) return
        val period = activeMealPeriod()
        val rows = data.cafeteria
            .sortedBy { it.seq }
            .flatMap { cafeteria ->
                cafeteria.menus
                    .filter { it.type.contains(period.marker) }
                    .take(2)
                    .map { menu ->
                        HomeRow(
                            badge = cafeteriaName(cafeteria.seq),
                            title = representativeMenu(menu.food),
                            subtitle = runningTime(cafeteria, period).orEmpty(),
                            trailing = menu.price,
                            tint = ContextCompat.getColor(requireContext(), R.color.hanyang_blue),
                        )
                    }
            }
            .take(5)

        if (rows.isEmpty()) {
            addEmptyRow(binding.mealContainer, R.string.home_meal_empty_title, R.string.home_meal_empty_message)
        } else {
            rows.forEach { addHomeRow(binding.mealContainer, it) }
        }
    }

    private fun addHomeRow(container: LinearLayout, row: HomeRow) {
        val rowBinding = ItemHomeRowBinding.inflate(layoutInflater, container, false)
        rowBinding.badge.text = row.badge
        rowBinding.badge.backgroundTintList = ColorStateList.valueOf(row.tint)
        rowBinding.title.text = row.title
        rowBinding.subtitle.text = row.subtitle
        rowBinding.trailing.text = row.trailing
        rowBinding.trailing.setTextColor(row.tint)
        container.addView(rowBinding.root, rowLayoutParams(container.childCount))
    }

    private fun addEmptyRow(container: LinearLayout, titleRes: Int, messageRes: Int) {
        val view = layoutInflater.inflate(R.layout.item_home_row, container, false)
        view.findViewById<TextView>(R.id.badge).visibility = View.GONE
        view.findViewById<TextView>(R.id.title).text = getString(titleRes)
        view.findViewById<TextView>(R.id.subtitle).text = getString(messageRes)
        view.findViewById<TextView>(R.id.trailing).visibility = View.GONE
        container.addView(view, rowLayoutParams(container.childCount))
    }

    private fun rowLayoutParams(index: Int): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply {
            if (index > 0) topMargin = resources.getDimensionPixelSize(R.dimen.home_row_gap)
        }
    }

    private fun routeSubtitle(entry: HomePageQuery.Entry): String {
        val stopNames = entry.stops.map { localizedStopName(it.stop) }.filter { it.isNotBlank() }
        return if (stopNames.isEmpty()) entry.route.name else stopNames.joinToString(" · ")
    }

    private fun localizedStopName(stop: String): String = when (stop) {
        "dormitory_o" -> getString(R.string.shuttle_tab_dormitory_out)
        "shuttlecock_o" -> getString(R.string.shuttle_tab_shuttlecock_out)
        "station" -> getString(R.string.shuttle_tab_station)
        "terminal" -> getString(R.string.shuttle_tab_terminal)
        "jungang_stn" -> getString(R.string.shuttle_tab_jungang_station)
        "shuttlecock_i" -> getString(R.string.shuttle_tab_shuttlecock_in)
        "dormitory_i" -> getString(R.string.shuttle_bound_for_dormitory)
        else -> stop
    }

    private fun routeBadge(tag: String, name: String): String = when {
        tag == "DH" || tag == "DY" -> getString(R.string.shuttle_type_direct)
        tag == "DJ" -> getString(R.string.shuttle_type_jungang)
        tag == "C" -> getString(R.string.shuttle_type_circular)
        name.endsWith("S") -> getString(R.string.shuttle_type_shuttlecock)
        name.endsWith("D") -> getString(R.string.shuttle_type_dormitory)
        else -> name
    }

    private fun routeColor(tag: String, name: String): Int = ContextCompat.getColor(
        requireContext(),
        when {
            tag == "DH" || tag == "DY" || name.endsWith("S") -> R.color.red_bus
            tag == "DJ" -> R.color.green_bus
            else -> R.color.blue_bus
        },
    )

    private fun compactTime(time: LocalTime): String = "%02d:%02d".format(time.hour, time.minute)

    private fun minutesUntil(time: LocalTime): Int? {
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        var target = now.withHour(time.hour).withMinute(time.minute).withSecond(0).withNano(0)
        if (target.isBefore(now)) target = target.plusDays(1)
        return ceil((target.toEpochSecond() - now.toEpochSecond()) / 60.0).toInt().coerceAtLeast(0)
    }

    private fun activeMealPeriod(): HomeMealPeriod {
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        return when {
            now.hour < 10 -> HomeMealPeriod("조식", R.string.home_meal_breakfast, "breakfast")
            now.hour < 15 -> HomeMealPeriod("중식", R.string.home_meal_lunch, "lunch")
            now.hour < 20 -> HomeMealPeriod("석식", R.string.home_meal_dinner, "dinner")
            else -> HomeMealPeriod("조식", R.string.home_meal_tomorrow_breakfast, "breakfast")
        }
    }

    private fun runningTime(cafeteria: HomePageQuery.Cafeterium, period: HomeMealPeriod): String? = when (period.marker) {
        "조식" -> cafeteria.runningTime.breakfast
        "중식" -> cafeteria.runningTime.lunch
        else -> cafeteria.runningTime.dinner
    }

    private fun cafeteriaName(seq: Int): String {
        val resId = when (seq) {
            1 -> R.string.cafeteria_1
            2 -> R.string.cafeteria_2
            4 -> R.string.cafeteria_4
            6 -> R.string.cafeteria_6
            7 -> R.string.cafeteria_7
            8 -> R.string.cafeteria_8
            11 -> R.string.cafeteria_11
            12 -> R.string.cafeteria_12
            13 -> R.string.cafeteria_13
            14 -> R.string.cafeteria_14
            15 -> R.string.cafeteria_15
            else -> R.string.cafeteria_1
        }
        return getString(resId)
    }

    private fun representativeMenu(food: String): String {
        return food
            .replace("\"", "")
            .split(Regex("\\s+"))
            .firstOrNull { it.isNotBlank() }
            ?: food
    }
}

private enum class HomeDeparture(val titleRes: Int, val destinations: List<HomeDestination>) {
    DORMITORY(R.string.shuttle_tab_dormitory_out, listOf(HomeDestination.STATION, HomeDestination.TERMINAL, HomeDestination.JUNGANG)),
    SHUTTLECOCK(R.string.shuttle_tab_shuttlecock_out, listOf(HomeDestination.STATION, HomeDestination.TERMINAL, HomeDestination.JUNGANG, HomeDestination.DORMITORY)),
    STATION(R.string.shuttle_tab_station, listOf(HomeDestination.DORMITORY, HomeDestination.TERMINAL, HomeDestination.JUNGANG)),
    TERMINAL(R.string.shuttle_tab_terminal, listOf(HomeDestination.DORMITORY)),
    JUNGANG(R.string.shuttle_tab_jungang_station, listOf(HomeDestination.DORMITORY));

    fun routeTo(destination: HomeDestination): HomeShuttleRoute = when (this to destination) {
        DORMITORY to HomeDestination.STATION -> HomeShuttleRoute("dormitory_o", "STATION")
        DORMITORY to HomeDestination.TERMINAL -> HomeShuttleRoute("dormitory_o", "TERMINAL")
        DORMITORY to HomeDestination.JUNGANG -> HomeShuttleRoute("dormitory_o", "JUNGANG")
        SHUTTLECOCK to HomeDestination.STATION -> HomeShuttleRoute("shuttlecock_o", "STATION")
        SHUTTLECOCK to HomeDestination.TERMINAL -> HomeShuttleRoute("shuttlecock_o", "TERMINAL")
        SHUTTLECOCK to HomeDestination.JUNGANG -> HomeShuttleRoute("shuttlecock_o", "JUNGANG")
        SHUTTLECOCK to HomeDestination.DORMITORY -> HomeShuttleRoute("shuttlecock_i", "CAMPUS") { it.route.name.endsWith("D") }
        STATION to HomeDestination.DORMITORY -> HomeShuttleRoute("station", "CAMPUS") { it.route.name.endsWith("D") }
        STATION to HomeDestination.TERMINAL -> HomeShuttleRoute("station", "TERMINAL")
        STATION to HomeDestination.JUNGANG -> HomeShuttleRoute("station", "JUNGANG")
        TERMINAL to HomeDestination.DORMITORY -> HomeShuttleRoute("terminal", "CAMPUS") { it.route.name.endsWith("D") }
        JUNGANG to HomeDestination.DORMITORY -> HomeShuttleRoute("jungang_stn", "CAMPUS") { it.route.name.endsWith("D") }
        else -> HomeShuttleRoute("dormitory_o", "STATION")
    }
}

private enum class HomeDestination(val titleRes: Int) {
    STATION(R.string.home_destination_station),
    TERMINAL(R.string.home_destination_terminal),
    JUNGANG(R.string.home_destination_jungang),
    DORMITORY(R.string.home_destination_dormitory),
}

private data class HomeShuttleRoute(
    val stop: String,
    val destination: String,
    val filter: ((HomePageQuery.Entry) -> Boolean)? = null,
)

private data class HomeMealPeriod(
    val marker: String,
    val titleRes: Int,
    val tab: String,
) {
    fun title(context: android.content.Context): String = context.getString(titleRes)
}

private data class HomeRow(
    val badge: String,
    val title: String,
    val subtitle: String,
    val trailing: String,
    val tint: Int,
)
