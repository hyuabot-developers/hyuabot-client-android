package app.kobuggi.hyuabot.ui.readingRoom

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** 혼잡도. 글자·태그 색과 이름이 모두 이 값에서 정해진다. */
enum class SeatLevel { Good, Mid, Busy, Full }

/** 열람실 화면의 순수 계산 (iOS ReadingRoomDisplayLogic과 같은 규칙). */
object ReadingRoomLogic {
    val ZONE: ZoneId = ZoneId.of("Asia/Seoul")
    const val DEFAULT_CAMPUS = 2
    const val AUTO_REFRESH_MS = 15_000L
    /** 이용 종료 몇 분 전에 알릴지. */
    const val ALARM_LEAD_MINUTES = 10L

    private val CLOCK = DateTimeFormatter.ofPattern("HH:mm")
    val STORED_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /** 잔여 ≤ 0 또는 전체 ≤ 0이면 만석, 잔여/전체 < 10% 혼잡, < 30% 보통, 나머지 여유. */
    fun level(available: Int, active: Int): SeatLevel {
        if (available <= 0 || active <= 0) return SeatLevel.Full
        val ratio = available.toDouble() / active
        return when {
            ratio < 0.1 -> SeatLevel.Busy
            ratio < 0.3 -> SeatLevel.Mid
            else -> SeatLevel.Good
        }
    }

    /** 표시하는 잔여 숫자는 음수가 되지 않는다. */
    fun shownAvailable(available: Int): Int = maxOf(available, 0)

    /** 사용률 (전체 − 잔여) / 전체를 0–1로 자른다. */
    fun usage(available: Int, active: Int): Float {
        if (active <= 0) return 0f
        return ((active - shownAvailable(available)).toFloat() / active).coerceIn(0f, 1f)
    }

    /** `제1열람실[2층]` → (제1열람실, 2층). `[`가 없으면 층은 비어 있다. */
    fun splitName(label: String): Pair<String, String> {
        val open = label.indexOf('[')
        if (open < 0) return label to ""
        return label.substring(0, open) to label.substring(open + 1).removeSuffix("]")
    }

    /** 거른 목록의 updatedAt 중 가장 늦은 시각 (없으면 null → 호출하는 쪽이 지금을 쓴다). */
    fun latestUpdate(times: List<ZonedDateTime>): ZonedDateTime? = times.maxOrNull()

    fun clock(time: ZonedDateTime): String = time.withZoneSameInstant(ZONE).format(CLOCK)

    fun clock(time: LocalDateTime): String = time.format(CLOCK)

    /** 지금부터 h × 60 − 10분 뒤 한 번 울린다. */
    fun alarmAt(now: LocalDateTime, hours: Int): LocalDateTime =
        now.plusHours(hours.toLong()).minusMinutes(ALARM_LEAD_MINUTES)

    /** 예약한 알림 시각에서 되짚은 이용 종료 시각. */
    fun endAt(alarmAt: LocalDateTime): LocalDateTime = alarmAt.plusMinutes(ALARM_LEAD_MINUTES)

    /** 저장된 알림 시각 문자열 → 시각. 비었거나 이미 지났거나 형식이 틀리면 null (그 경우 저장값을 지운다). */
    fun restoreAlarm(stored: String?, now: LocalDateTime): LocalDateTime? {
        if (stored.isNullOrBlank()) return null
        val at = runCatching { LocalDateTime.parse(stored, STORED_TIME) }.getOrNull() ?: return null
        return at.takeIf { it.isAfter(now) }
    }
}
