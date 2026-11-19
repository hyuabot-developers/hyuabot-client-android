package app.kobuggi.hyuabot.ui.commuter

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.math.cos
import kotlin.math.sqrt

/** 통학버스 화면의 순수 로직. iOS CommuterBusLogic과 같은 규칙을 따른다. */
enum class CommuterDirection { ToCampus, FromCampus }

data class CommuterStop(
    /** HH:mm. 00:00(정해지지 않음)은 null. */
    val time: String?,
    val name: String,
    val detail: String,
    val latitude: Double,
    val longitude: Double,
) {
    val hasCoordinate: Boolean get() = latitude != 0.0
    val isCampus: Boolean get() = name.startsWith("ERICA")
}

data class CommuterRoute(val name: String, val summary: String, val stops: List<CommuterStop>) {
    val direction: CommuterDirection
        get() = if (stops.lastOrNull()?.isCampus == true) CommuterDirection.ToCampus else CommuterDirection.FromCampus
}

data class GeoPoint(val latitude: Double, val longitude: Double)

data class CommuterServiceContext(
    val date: LocalDate,
    val nowMinutes: Int,
    val periodType: String? = null,
    val periodEndDay: LocalDate? = null,
    val holidayType: String? = null,
    val nextRunDay: LocalDate? = null,
    val userLocation: GeoPoint? = null,
)

/** direction/routeName이 null이면 자동(가까운 정류장). */
data class CommuterSelection(
    val direction: CommuterDirection? = null,
    val routeName: String? = null,
    val expanded: Boolean = false,
)

enum class CommuterPhase { Off, BeforeRun, Finished, Running }
enum class CommuterOffReason { Halt, VacationSession, Vacation, Weekend }
data class CommuterOffNotice(val reason: CommuterOffReason, val nextRunDay: LocalDate)

sealed interface CommuterSubtitle {
    data class Semester(val endDay: LocalDate) : CommuterSubtitle
    data object WeekdaysOnly : CommuterSubtitle
}

data class CommuterRouteItem(val name: String, val summary: String, val isSelected: Boolean)

data class CommuterRouteSummary(
    val routeName: String,
    val direction: CommuterDirection,
    val firstTime: String,
    val lastTime: String?,
    val stopCount: Int,
    val durationMinutes: Int?,
    val isPartial: Boolean,
    val phase: CommuterPhase,
)

enum class CommuterRail { Hidden, Normal, Traveled }
enum class CommuterDot { Traveled, Filled, Hollow }
enum class CommuterTag { Departure, Arrival }

data class CommuterStopRow(
    val index: Int,
    val stop: CommuterStop,
    val isNear: Boolean,
    val isPassed: Boolean,
    val tag: CommuterTag?,
    val dot: CommuterDot,
    val lineTop: CommuterRail,
    val lineBottom: CommuterRail,
    /** 현재 구간의 버스가 이 정류장과 다음 정류장 경계에 있다. */
    val hasBus: Boolean,
)

sealed interface CommuterTimelineRow {
    data class Fold(val hiddenCount: Int) : CommuterTimelineRow
    data class Stop(val row: CommuterStopRow) : CommuterTimelineRow
    data class Gap(val minutes: Int, val traveled: Boolean) : CommuterTimelineRow
}

data class CommuterScreenModel(
    val subtitle: CommuterSubtitle,
    val directions: List<Pair<CommuterDirection, Boolean>>,
    val routes: List<CommuterRouteItem>,
    val showsResetToNearest: Boolean,
    val autoDirection: CommuterDirection,
    val autoRouteName: String?,
    val selectedDirection: CommuterDirection,
    val selectedRouteName: String?,
    val summary: CommuterRouteSummary?,
    val offNotice: CommuterOffNotice?,
    val rows: List<CommuterTimelineRow>,
)

object CommuterLogic {
    val ZONE: ZoneId = ZoneId.of("Asia/Seoul")
    const val NOON_MINUTES = 720
    const val GAP_MINUTES = 20

    fun minutes(hhmm: String): Int? {
        val parts = hhmm.split(":")
        if (parts.size != 2 && parts.size != 3) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        if (parts.size == 3 && parts[2].toIntOrNull()?.let { it in 0..59 } != true) return null
        return h * 60 + m
    }

    fun meters(from: GeoPoint, to: GeoPoint): Double {
        val r = Math.PI / 180
        val x = (to.longitude - from.longitude) * r * cos((from.latitude + to.latitude) / 2 * r)
        val y = (to.latitude - from.latitude) * r
        return sqrt(x * x + y * y) * 6_371_000
    }

    private fun isWeekend(date: LocalDate) = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

    fun nextWeekday(after: LocalDate): LocalDate {
        var next = after
        do next = next.plusDays(1) while (isWeekend(next))
        return next
    }

    fun firstWeekday(onOrAfter: LocalDate): LocalDate {
        var next = onOrAfter
        while (isWeekend(next)) next = next.plusDays(1)
        return next
    }

    /** 서버의 기간 경계(DateTime)는 UTC 기준 날짜로 읽는다. */
    fun periodBoundaryDay(instant: ZonedDateTime): LocalDate = instant.withZoneSameInstant(ZoneOffset.UTC).toLocalDate()

    fun routes(
        commute: List<CommuteInput>,
        korean: Boolean,
    ): List<CommuterRoute> = commute.mapNotNull { route ->
        val stops = route.timetable.sortedBy { it.order }.map { entry ->
            val raw = entry.time.take(5)
            CommuterStop(
                time = if (raw == "00:00") null else raw,
                name = entry.stopName ?: entry.name,
                detail = entry.stopDescription.orEmpty(),
                latitude = entry.latitude ?: 0.0,
                longitude = entry.longitude ?: 0.0,
            )
        }
        if (stops.isEmpty()) null else CommuterRoute(route.name, if (korean) route.korean else route.english, stops)
    }

    /** 쿼리 결과에서 필요한 값만 옮긴 입력. 생성된 Apollo 타입과 로직을 분리한다. */
    data class CommuteInput(val name: String, val korean: String, val english: String, val timetable: List<EntryInput>)
    data class EntryInput(
        val name: String,
        val order: Int,
        val time: String,
        val stopName: String?,
        val stopDescription: String?,
        val latitude: Double?,
        val longitude: Double?,
    )

    private data class Nearest(val routeName: String, val index: Int, val meters: Double)

    private fun nearestStop(routes: List<CommuterRoute>, context: CommuterServiceContext, direction: CommuterDirection): Nearest? {
        val location = context.userLocation ?: return null
        var near: Nearest? = null
        for (route in routes.filter { it.direction == direction }) {
            route.stops.forEachIndexed { index, stop ->
                val boarding = if (direction == CommuterDirection.ToCampus) !stop.isCampus else stop.isCampus
                if (boarding && stop.hasCoordinate) {
                    val d = meters(location, GeoPoint(stop.latitude, stop.longitude))
                    if (near == null || d < near!!.meters) near = Nearest(route.name, index, d)
                }
            }
        }
        return near
    }

    private fun serviceNotice(context: CommuterServiceContext, running: Boolean, semester: Boolean, weekend: Boolean): CommuterOffNotice? {
        if (running) return null
        val next = context.nextRunDay ?: nextWeekday(context.date)
        val reason = when {
            context.holidayType == "halt" -> CommuterOffReason.Halt
            !semester -> if (context.periodType == "vacation_session") CommuterOffReason.VacationSession else CommuterOffReason.Vacation
            weekend -> CommuterOffReason.Weekend
            else -> null
        }
        return reason?.let { CommuterOffNotice(it, next) }
    }

    fun autoDirection(nowMinutes: Int) = if (nowMinutes < NOON_MINUTES) CommuterDirection.ToCampus else CommuterDirection.FromCampus

    fun makeModel(routes: List<CommuterRoute>, context: CommuterServiceContext, selection: CommuterSelection): CommuterScreenModel {
        val weekend = isWeekend(context.date) || context.holidayType == "weekends"
        val semester = context.periodType == "semester"
        val running = semester && !weekend && context.holidayType != "halt"
        val subtitle = if (semester && context.periodEndDay != null) CommuterSubtitle.Semester(context.periodEndDay) else CommuterSubtitle.WeekdaysOnly
        val autoDirection = autoDirection(context.nowMinutes)
        val near = nearestStop(routes, context, autoDirection)
        val autoRoute = near?.routeName ?: routes.firstOrNull { it.direction == autoDirection }?.name
        val reset = (selection.direction != null || selection.routeName != null) &&
            (selection.direction != autoDirection || selection.routeName != autoRoute)
        val selectedDirection = selection.direction ?: autoDirection
        val list = routes.filter { it.direction == selectedDirection }
        val want = selection.routeName ?: autoRoute
        val current = list.firstOrNull { it.name == want } ?: list.firstOrNull()
        val nearHere = near?.takeIf { it.routeName == current?.name }
        val directions = CommuterDirection.entries.map { it to (it == selectedDirection) }
        val items = list.map { CommuterRouteItem(it.name, it.summary, it.name == current?.name) }
        val notice = serviceNotice(context, running, semester, weekend)
        if (current == null || current.stops.isEmpty()) {
            return CommuterScreenModel(subtitle, directions, items, reset, autoDirection, autoRoute, selectedDirection, null, null, notice, emptyList())
        }
        val (summary, rows) = timeline(current, context, selection, running, nearHere)
        return CommuterScreenModel(subtitle, directions, items, reset, autoDirection, autoRoute, selectedDirection, current.name, summary, notice, rows)
    }

    private fun currentSegment(stops: List<CommuterStop>, phase: CommuterPhase, partial: Boolean, now: Int): Int {
        if (phase != CommuterPhase.Running || partial) return -1
        for (i in 0 until stops.lastIndex) {
            val a = minutes(stops[i].time.orEmpty()) ?: 0
            val b = minutes(stops[i + 1].time.orEmpty()) ?: 0
            if (now in a until b) return i
        }
        return -1
    }

    private fun timeline(
        current: CommuterRoute,
        context: CommuterServiceContext,
        selection: CommuterSelection,
        running: Boolean,
        nearHere: Nearest?,
    ): Pair<CommuterRouteSummary, List<CommuterTimelineRow>> {
        val stops = current.stops
        val last = stops.lastIndex
        val partial = stops.any { it.time == null }
        val firstMinutes = minutes(stops[0].time.orEmpty()) ?: 0
        val lastMinutes = minutes(stops[last].time.orEmpty())
        val phase = when {
            !running -> CommuterPhase.Off
            context.nowMinutes < firstMinutes -> CommuterPhase.BeforeRun
            !partial && context.nowMinutes >= (lastMinutes ?: 0) -> CommuterPhase.Finished
            else -> CommuterPhase.Running
        }
        val segment = currentSegment(stops, phase, partial, context.nowMinutes)
        val summary = CommuterRouteSummary(
            routeName = current.name,
            direction = current.direction,
            firstTime = stops[0].time ?: "—",
            lastTime = stops[last].time,
            stopCount = stops.size,
            durationMinutes = if (partial) null else (lastMinutes ?: 0) - firstMinutes,
            isPartial = partial,
            phase = phase,
        )
        return summary to timelineRows(stops, phase, segment, nearHere?.index, selection.expanded, current.direction)
    }

    private fun timelineRows(
        stops: List<CommuterStop>,
        phase: CommuterPhase,
        segment: Int,
        nearIndex: Int?,
        expanded: Boolean,
        direction: CommuterDirection,
    ): List<CommuterTimelineRow> {
        val last = stops.lastIndex
        fun known(i: Int) = stops[i].time != null
        var anchor = nearIndex ?: segment
        if (segment >= 0 && nearIndex != null) anchor = minOf(segment + 1, nearIndex)
        val foldTo = if (!expanded && anchor > 2) anchor - 1 else 0
        val rows = mutableListOf<CommuterTimelineRow>()
        if (foldTo > 0) rows += CommuterTimelineRow.Fold(foldTo)
        for (index in foldTo..last) {
            val stop = stops[index]
            val campus = stop.isCampus
            val passed = phase == CommuterPhase.Finished || (segment >= 0 && index <= segment)
            val isNear = nearIndex == index
            val gap = if (index < last && known(index) && known(index + 1)) {
                (minutes(stops[index + 1].time.orEmpty()) ?: 0) - (minutes(stop.time.orEmpty()) ?: 0)
            } else 0
            val big = gap >= GAP_MINUTES
            val filled = isNear || campus || index == 0
            val dot = when {
                passed && !isNear -> CommuterDot.Traveled
                filled -> CommuterDot.Filled
                else -> CommuterDot.Hollow
            }
            val tag = when {
                campus -> if (direction == CommuterDirection.ToCampus) CommuterTag.Arrival else CommuterTag.Departure
                index == 0 -> CommuterTag.Departure
                else -> null
            }
            val top = when {
                index == 0 -> CommuterRail.Hidden
                passed -> CommuterRail.Traveled
                else -> CommuterRail.Normal
            }
            val traveledBottom = phase == CommuterPhase.Finished || (segment >= 0 && index < segment) || index == segment
            val bottom = when {
                index == last || (big && index != segment) -> CommuterRail.Hidden
                traveledBottom -> CommuterRail.Traveled
                else -> CommuterRail.Normal
            }
            rows += CommuterTimelineRow.Stop(CommuterStopRow(index, stop, isNear, passed, tag, dot, top, bottom, hasBus = index == segment))
            if (big && index != segment) {
                rows += CommuterTimelineRow.Gap(gap, traveled = phase == CommuterPhase.Finished || (segment >= 0 && index < segment))
            }
        }
        return rows
    }
}
