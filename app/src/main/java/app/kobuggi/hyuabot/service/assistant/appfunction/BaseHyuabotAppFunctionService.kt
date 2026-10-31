package app.kobuggi.hyuabot.service.assistant.appfunction

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionDeclaration
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import app.kobuggi.hyuabot.service.assistant.AssistantResponseFormatter
import app.kobuggi.hyuabot.service.assistant.AssistantResponses
import app.kobuggi.hyuabot.service.assistant.CafeteriaMenuResponse
import app.kobuggi.hyuabot.service.assistant.GetNextShuttleUseCase
import app.kobuggi.hyuabot.service.assistant.GetTodayCafeteriaMenuUseCase
import app.kobuggi.hyuabot.service.assistant.NextShuttleResponse
import app.kobuggi.hyuabot.util.localizedContext

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AssistantEntryPoint {
    fun getTodayCafeteriaMenuUseCase(): GetTodayCafeteriaMenuUseCase
    fun getNextShuttleUseCase(): GetNextShuttleUseCase
}

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
@AppFunctionServiceEntryPoint(
    serviceName = "HyuabotAppFunctionService",
    appFunctionXmlFileName = "hyuabot_app_functions",
)
abstract class BaseHyuabotAppFunctionService : AppFunctionService() {
    private fun entryPoint() = EntryPointAccessors.fromApplication(
        applicationContext,
        AssistantEntryPoint::class.java,
    )

    private fun formatter() = AssistantResponseFormatter(localizedContext(applicationContext))

    /**
     * 오늘 한양대학교 학식(학생식당 등) 메뉴를 알려줍니다. 예: '오늘 학식 뭐야', '점심 학생식당 메뉴'.
     *
     * @param cafeteria 식당 이름 또는 별칭(예: 학생식당, 교직원식당, 기숙사, 학생회관). 생략하면 사용자가 앱에서 선택한 캠퍼스의 모든 식당.
     * @param meal 끼니(아침/조식, 점심/중식, 저녁/석식). 생략하면 현재 시간대 끼니.
     * @return summary에 읽어 줄 문장, 식당별 메뉴 목록과 상태 코드
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun getTodayCafeteriaMenu(
        cafeteria: String? = null,
        meal: String? = null,
    ): CafeteriaMenuResponse = AssistantResponses.cafeteria(
        entryPoint().getTodayCafeteriaMenuUseCase().invoke(cafeteria, meal),
        formatter(),
    )

    /**
     * 한양대 ERICA 셔틀버스의 다음 출발 시각과 남은 시간을 알려줍니다. 예: '다음 셔틀 언제 와', '기숙사에서 한대앞 가는 셔틀'.
     *
     * @param stop 탑승 정류장(기숙사, 셔틀콕, 셔틀콕 건너편, 한대앞, 예술인, 중앙역). 생략하면 기숙사.
     * @param destination 행선지(한대앞, 예술인, 중앙역, 기숙사/학교). 생략하면 가장 먼저 출발하는 차.
     * @return summary에 'n분 후 출발, 행선지 ○○' 형태의 문장과 출발 시각·남은 분·상태 코드
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun getNextShuttle(
        stop: String? = null,
        destination: String? = null,
    ): NextShuttleResponse = AssistantResponses.shuttle(
        entryPoint().getNextShuttleUseCase().invoke(stop, destination),
        formatter(),
    )
}
