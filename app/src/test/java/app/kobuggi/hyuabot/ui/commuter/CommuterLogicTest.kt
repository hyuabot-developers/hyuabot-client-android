package app.kobuggi.hyuabot.ui.commuter

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CommuterLogicTest {
    private fun stop(time: String?, name: String, lat: Double = 37.5, lng: Double = 127.0) = CommuterStop(time, name, "", lat, lng)

    private val to4 = CommuterRoute(
        "4", "천호",
        listOf(
            stop("06:35", "A", 37.5378, 127.1229), stop("06:39", "B"), stop("06:42", "C"), stop("06:46", "D", 37.5149, 127.0985),
            stop("06:49", "E"), stop("06:51", "F"), stop("07:12", "G"), stop("08:30", "ERICA 민주광장", 37.2991, 126.8348),
        ),
    )
    private val to5 = CommuterRoute("5", "정자", listOf(stop("07:15", "P", 37.3664, 127.1079), stop("08:30", "ERICA 민주광장", 37.2991, 126.8348)))
    private val from = CommuterRoute(
        "D", "수지",
        listOf(stop("17:40", "ERICA 본관 앞", 37.2984, 126.8336), stop(null, "상현", 0.0, 0.0), stop(null, "모란", 0.0, 0.0)),
    )
    private val routes = listOf(to4, to5, from)
    private val monday = LocalDate.of(2026, 11, 9)

    private fun ctx(now: Int, loc: GeoPoint? = null, period: String = "semester", holiday: String? = null, date: LocalDate = monday) =
        CommuterServiceContext(date, now, period, LocalDate.of(2026, 12, 21), holiday, null, loc)

    private fun rowsOf(m: CommuterScreenModel) = m.rows.filterIsInstance<CommuterTimelineRow.Stop>().map { it.row }

    @Test
    fun directionAndRouteFollowTheClock() {
        assertEquals(CommuterDirection.ToCampus, CommuterLogic.autoDirection(719))
        assertEquals(CommuterDirection.FromCampus, CommuterLogic.autoDirection(720))
        assertEquals(CommuterDirection.FromCampus, from.direction)
        assertEquals(CommuterDirection.ToCampus, to4.direction)
    }

    @Test
    fun nearestBoardingStopPicksRoute() {
        // 5번 노선 정류장 가까이
        val m = CommuterLogic.makeModel(routes, ctx(6 * 60, GeoPoint(37.3664, 127.1079)), CommuterSelection())
        assertEquals("5", m.selectedRouteName)
        assertFalse(m.showsResetToNearest)
        // 위치 없음: 방향 첫 노선, 근처 강조 없음
        val none = CommuterLogic.makeModel(routes, ctx(6 * 60), CommuterSelection())
        assertEquals("4", none.selectedRouteName)
        assertTrue(rowsOf(none).none { it.isNear })
        // 학교 정류장은 승차 정류장이 아니다 (등교)
        val campus = CommuterLogic.makeModel(routes, ctx(6 * 60, GeoPoint(37.2991, 126.8348)), CommuterSelection())
        assertTrue(rowsOf(campus).none { it.stop.isCampus && it.isNear })
    }

    @Test
    fun manualSelectionShowsResetOnlyWhenDifferent() {
        val loc = GeoPoint(37.3664, 127.1079)
        val same = CommuterLogic.makeModel(routes, ctx(360, loc), CommuterSelection(CommuterDirection.ToCampus, "5"))
        assertFalse(same.showsResetToNearest)
        val diff = CommuterLogic.makeModel(routes, ctx(360, loc), CommuterSelection(CommuterDirection.ToCampus, "4"))
        assertTrue(diff.showsResetToNearest)
        assertEquals("4", diff.selectedRouteName)
    }

    @Test
    fun foldAnchorUsesMinOfBusAndNearAndNeedsMoreThanTwo() {
        // 06:44: 구간 2(06:42~06:46), 가까운 곳 D(index 3) → anchor = min(3,3) = 3 → foldTo 2
        val m = CommuterLogic.makeModel(routes, ctx(6 * 60 + 44, GeoPoint(37.5149, 127.0985)), CommuterSelection())
        assertEquals(CommuterTimelineRow.Fold(2), m.rows.first())
        assertEquals(2, rowsOf(m).first().index)
        // 펼치면 접지 않는다
        val open = CommuterLogic.makeModel(routes, ctx(6 * 60 + 44, GeoPoint(37.5149, 127.0985)), CommuterSelection(expanded = true))
        assertEquals(0, rowsOf(open).first().index)
        // anchor <= 2 이면 접지 않는다 (06:36: 구간 0)
        val early = CommuterLogic.makeModel(routes, ctx(6 * 60 + 36), CommuterSelection())
        assertEquals(0, rowsOf(early).first().index)
    }

    @Test
    fun busSitsOnCurrentSegmentAndRailsFollow() {
        val m = CommuterLogic.makeModel(routes, ctx(6 * 60 + 40), CommuterSelection())
        val rows = rowsOf(m)
        assertEquals(1, rows.single { it.hasBus }.index)
        assertEquals(CommuterRail.Traveled, rows[1].lineBottom) // 버스가 있는 정류장 아래는 지나간 레일
        assertEquals(CommuterRail.Traveled, rows[1].lineTop)
        assertEquals(CommuterRail.Normal, rows[2].lineTop)
        assertEquals(CommuterDot.Traveled, rows[0].dot)
        assertEquals(CommuterDot.Hollow, rows[2].dot)
        assertEquals(CommuterRail.Hidden, rows[0].lineTop)
        assertEquals(CommuterTag.Departure, rows[0].tag)
        assertEquals(CommuterTag.Arrival, rows.last().tag)
        assertEquals(CommuterDot.Filled, rows.last().dot)
        assertEquals(CommuterPhase.Running, m.summary?.phase)
    }

    @Test
    fun gapOf20MinutesOrMoreHidesRailAndAddsGapRow() {
        val m = CommuterLogic.makeModel(routes, ctx(6 * 60), CommuterSelection())
        val gap = m.rows.filterIsInstance<CommuterTimelineRow.Gap>().first()
        assertEquals(21, gap.minutes) // F 06:51 → G 07:12
        val idx = m.rows.indexOf(gap)
        val before = (m.rows[idx - 1] as CommuterTimelineRow.Stop).row
        assertEquals("F", before.stop.name)
        assertEquals(CommuterRail.Hidden, before.lineBottom)
        // 07:00 이후엔 버스가 그 구간에 있어 gap 대신 버스가 그려진다
        val live = CommuterLogic.makeModel(routes, ctx(7 * 60), CommuterSelection(expanded = true))
        assertEquals(1, live.rows.count { it is CommuterTimelineRow.Gap }) // G → 학교 78분만 남는다
        assertEquals("F", rowsOf(live).single { it.hasBus }.stop.name)
    }

    @Test
    fun phasesBeforeFinishedAndOff() {
        assertEquals(CommuterPhase.BeforeRun, CommuterLogic.makeModel(routes, ctx(6 * 60), CommuterSelection()).summary?.phase)
        val done = CommuterLogic.makeModel(routes, ctx(11 * 60), CommuterSelection())
        assertEquals(CommuterPhase.Finished, done.summary?.phase)
        assertTrue(rowsOf(done).all { it.isPassed })
        val weekend = CommuterLogic.makeModel(routes, ctx(8 * 60, date = LocalDate.of(2026, 11, 14)), CommuterSelection())
        assertEquals(CommuterPhase.Off, weekend.summary?.phase)
        assertEquals(CommuterOffReason.Weekend, weekend.offNotice?.reason)
        assertEquals(LocalDate.of(2026, 11, 16), weekend.offNotice?.nextRunDay)
    }

    @Test
    fun offReasonsFollowHolidayAndPeriod() {
        val halt = CommuterLogic.makeModel(routes, ctx(480, holiday = "halt"), CommuterSelection())
        assertEquals(CommuterOffReason.Halt, halt.offNotice?.reason)
        val vac = CommuterLogic.makeModel(routes, ctx(480, period = "vacation"), CommuterSelection())
        assertEquals(CommuterOffReason.Vacation, vac.offNotice?.reason)
        val session = CommuterLogic.makeModel(routes, ctx(480, period = "vacation_session"), CommuterSelection())
        assertEquals(CommuterOffReason.VacationSession, session.offNotice?.reason)
        assertNull(CommuterLogic.makeModel(routes, ctx(480), CommuterSelection()).offNotice)
        assertEquals(CommuterSubtitle.WeekdaysOnly, vac.subtitle)
    }

    @Test
    fun partialTimetableHasNoBusOrDuration() {
        val m = CommuterLogic.makeModel(routes, ctx(18 * 60), CommuterSelection())
        assertEquals(CommuterDirection.FromCampus, m.selectedDirection)
        val s = m.summary!!
        assertTrue(s.isPartial)
        assertNull(s.durationMinutes)
        assertEquals(CommuterPhase.Running, s.phase)
        assertTrue(rowsOf(m).none { it.hasBus })
        assertEquals(CommuterTag.Departure, rowsOf(m).first().tag)
    }

    @Test
    fun midnightTimeIsUnknown() {
        val route = CommuterLogic.routes(
            listOf(
                CommuterLogic.CommuteInput(
                    "D", "수지", "Suji",
                    listOf(
                        CommuterLogic.EntryInput("b", 2, "00:00:00", null, null, null, null),
                        CommuterLogic.EntryInput("a", 1, "17:40:00", "ERICA 본관", "", 37.29, 126.83),
                    ),
                ),
            ),
            korean = true,
        ).single()
        assertEquals(listOf("ERICA 본관", "b"), route.stops.map { it.name })
        assertEquals(listOf<String?>("17:40", null), route.stops.map { it.time })
        assertFalse(route.stops[1].hasCoordinate)
    }

    @Test
    fun weekdayHelpersSkipWeekends() {
        assertEquals(LocalDate.of(2026, 11, 16), CommuterLogic.nextWeekday(LocalDate.of(2026, 11, 13)))
        assertEquals(LocalDate.of(2026, 11, 16), CommuterLogic.firstWeekday(LocalDate.of(2026, 11, 14)))
        assertEquals(LocalDate.of(2026, 11, 13), CommuterLogic.firstWeekday(LocalDate.of(2026, 11, 13)))
        assertEquals(360, CommuterLogic.minutes("06:00:30"))
        assertNull(CommuterLogic.minutes("24:00"))
    }
}
