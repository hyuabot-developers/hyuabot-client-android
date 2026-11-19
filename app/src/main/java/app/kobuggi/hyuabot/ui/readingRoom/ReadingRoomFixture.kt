package app.kobuggi.hyuabot.ui.readingRoom

import java.net.URLDecoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime

/**
 * 디버그 전용 픽스처 (iOS 디버그 픽스처와 같은 역할). 디자인 보드의 상태를 그대로 재현한다.
 * `hyuabot://debug/reading-room?campus=2&now=21:14&loading=first|refresh&error=1&alarm=3&subs=131,132&sheet=perm|denied|exact`
 */
object ReadingRoomFixture {
    private val SAMPLE = listOf(
        2 to ReadingRoomItem(61, "제1열람실[2층]", 312, 128),
        2 to ReadingRoomItem(63, "제2열람실[4층]", 220, 37),
        2 to ReadingRoomItem(131, "집중열람실[4층]", 96, 6),
        2 to ReadingRoomItem(132, "노상일 HOLMZ[4층]", 64, 0),
        1 to ReadingRoomItem(53, "제1열람실[지하1층]", 238, 54),
        1 to ReadingRoomItem(54, "제2열람실[지하1층]", 206, 0),
        1 to ReadingRoomItem(55, "제3열람실[3층]", 180, 14),
        1 to ReadingRoomItem(56, "제4열람실[3층]", 180, 96),
    )

    fun parse(query: String): ReadingRoomUiState {
        val params = query.split("&").filter { it.contains("=") }
            .associate { it.substringBefore("=") to URLDecoder.decode(it.substringAfter("="), "UTF-8") }
        val campus = if (params["campus"] == "1") 1 else 2
        val time = params["now"]?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: LocalTime.of(21, 14)
        val now = LocalDateTime.of(LocalDate.of(2026, 11, 9), time)
        val rooms = SAMPLE.filter { it.first == campus }.map { it.second }
        val alarmHours = params["alarm"]?.toIntOrNull()?.takeIf { it > 0 }
        val sheet = when (params["sheet"]) {
            "perm" -> ReadingRoomSheet.Perm
            "denied" -> ReadingRoomSheet.Denied
            "exact" -> ReadingRoomSheet.Exact
            else -> null
        }
        val job: ReadingRoomJob? = when (sheet) {
            null -> null
            ReadingRoomSheet.Exact -> ReadingRoomJob.Alarm(3)
            else -> rooms.firstOrNull()?.let { ReadingRoomJob.Bell(it.seq) }
        }
        val load = when (params["loading"]) {
            "first" -> ReadingRoomLoad.First
            "refresh" -> ReadingRoomLoad.Refresh
            else -> ReadingRoomLoad.Idle
        }
        val first = load == ReadingRoomLoad.First
        return ReadingRoomUiState(
            load = load,
            failed = params["error"] == "1",
            campus = campus,
            rooms = if (first) emptyList() else rooms,
            updatedAt = if (first) null else ZonedDateTime.of(now, ReadingRoomLogic.ZONE),
            subscribed = params["subs"].orEmpty().split(",").mapNotNull { it.trim().toIntOrNull() }.toSet(),
            alarm = alarmHours?.let { ReadingRoomAlarm(ReadingRoomLogic.alarmAt(now, it), it) },
            sheet = sheet,
            job = job,
            clockOverride = now,
        )
    }
}
