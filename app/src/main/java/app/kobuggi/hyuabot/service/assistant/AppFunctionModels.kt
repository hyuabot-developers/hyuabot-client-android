package app.kobuggi.hyuabot.service.assistant

import androidx.appfunctions.AppFunctionSerializable
import java.time.LocalDate
import java.time.LocalTime

/** 식당 하나의 식단을 AppFunction으로 전달하는 모델입니다. */
@AppFunctionSerializable(isDescribedByKDoc = true)
class CafeteriaMenuEntry(
    /** 식당 이름입니다. */
    val name: String,
    /** 식사 운영 시간이며, 정보가 없으면 null입니다. */
    val runningTime: String?,
    /** 식당에서 제공하는 음식 목록입니다. */
    val foods: List<String>,
)

/** 식당 메뉴 안내와 구조화된 식단 결과입니다. */
@AppFunctionSerializable(isDescribedByKDoc = true)
class CafeteriaMenuResponse(
    /** 사용자에게 그대로 읽어 줄 짧은 안내 문장입니다. */
    val summary: String,
    /** 결과 상태 코드: OK, NO_MENU, UNKNOWN_CAFETERIA, NETWORK_ERROR입니다. */
    val status: String,
    /** 메뉴 날짜이며, 결과가 없으면 null입니다. */
    val date: LocalDate?,
    /** 식사 종류이며, 결과가 없으면 null입니다. */
    val meal: String?,
    /** 식당별 메뉴 목록입니다. */
    val cafeterias: List<CafeteriaMenuEntry>,
)

/** 셔틀 출발 안내와 구조화된 운행 결과입니다. */
@AppFunctionSerializable(isDescribedByKDoc = true)
class NextShuttleResponse(
    /** 사용자에게 그대로 읽어 줄 짧은 안내 문장입니다. */
    val summary: String,
    /** 결과 상태 코드: OK, NO_MORE_SERVICE, UNKNOWN_STOP, UNKNOWN_DESTINATION, NETWORK_ERROR입니다. */
    val status: String,
    /** 출발 정류장 이름이며, 결과가 없으면 null입니다. */
    val stop: String?,
    /** 셔틀 행선지 이름이며, 결과가 없으면 null입니다. */
    val destination: String?,
    /** 출발 시각이며, 결과가 없으면 null입니다. */
    val departureTime: LocalTime?,
    /** 지금부터 출발까지 남은 분입니다. */
    val minutesUntil: Int?,
    /** 같은 행선지의 다음 출발 시각입니다. */
    val followingDepartureTime: LocalTime?,
)

object AssistantResponses {
    const val STATUS_OK = "OK"
    const val STATUS_NO_MENU = "NO_MENU"
    const val STATUS_UNKNOWN_CAFETERIA = "UNKNOWN_CAFETERIA"
    const val STATUS_NO_MORE_SERVICE = "NO_MORE_SERVICE"
    const val STATUS_UNKNOWN_STOP = "UNKNOWN_STOP"
    const val STATUS_UNKNOWN_DESTINATION = "UNKNOWN_DESTINATION"
    const val STATUS_NETWORK_ERROR = "NETWORK_ERROR"

    fun cafeteria(answer: CafeteriaMenuAnswer, formatter: AssistantResponseFormatter): CafeteriaMenuResponse = when (answer) {
        is CafeteriaMenuAnswer.Menus -> CafeteriaMenuResponse(
            formatter.format(answer),
            STATUS_OK,
            answer.date,
            answer.meal.name,
            answer.cafeterias.map {
                CafeteriaMenuEntry(formatter.cafeteriaName(it.seq), it.runningTime, it.foods)
            },
        )
        is CafeteriaMenuAnswer.NoMenu -> CafeteriaMenuResponse(
            formatter.format(answer), STATUS_NO_MENU, answer.date, answer.meal.name, emptyList(),
        )
        is CafeteriaMenuAnswer.UnknownCafeteria -> CafeteriaMenuResponse(
            formatter.format(answer), STATUS_UNKNOWN_CAFETERIA, null, null, emptyList(),
        )
        CafeteriaMenuAnswer.NetworkError -> CafeteriaMenuResponse(
            formatter.format(answer), STATUS_NETWORK_ERROR, null, null, emptyList(),
        )
    }

    fun shuttle(answer: NextShuttleAnswer, formatter: AssistantResponseFormatter): NextShuttleResponse = when (answer) {
        is NextShuttleAnswer.Departure -> NextShuttleResponse(
            formatter.format(answer),
            STATUS_OK,
            formatter.stopName(answer.stopCode),
            formatter.destinationName(answer.destinationCode),
            answer.departureTime,
            answer.minutesUntil.toInt(),
            answer.followingTime,
        )
        is NextShuttleAnswer.NoMoreService -> NextShuttleResponse(
            formatter.format(answer), STATUS_NO_MORE_SERVICE, formatter.stopName(answer.stopCode),
            answer.destinationCode?.let(formatter::destinationName), null, null, null,
        )
        is NextShuttleAnswer.UnknownStop -> NextShuttleResponse(
            formatter.format(answer), STATUS_UNKNOWN_STOP, null, null, null, null, null,
        )
        is NextShuttleAnswer.UnknownDestination -> NextShuttleResponse(
            formatter.format(answer), STATUS_UNKNOWN_DESTINATION, null, null, null, null, null,
        )
        NextShuttleAnswer.NetworkError -> NextShuttleResponse(
            formatter.format(answer), STATUS_NETWORK_ERROR, null, null, null, null, null,
        )
    }
}
