package app.kobuggi.hyuabot.service.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleServiceNoticeQuery
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShuttleServiceNoticeScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apolloClient: ApolloClient,
) {
    suspend fun sync() = withContext(Dispatchers.IO) {
        val today = LocalDate.now(SERVICE_ZONE)
        val response =
            try {
                apolloClient.query(
                    ShuttleServiceNoticeQuery(
                        start = today,
                        end = today.plusDays(LOOKAHEAD_DAYS),
                    ),
                ).fetchPolicy(FetchPolicy.NetworkOnly).execute()
            } catch (_: Exception) {
                return@withContext
            }
        if (response.exception != null || response.hasErrors()) return@withContext
        val notices = response.data?.shuttle?.serviceNotices ?: return@withContext

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.getStringSet(KEY_NOTICE_IDS, emptySet()).orEmpty().forEach(::cancel)
        notices.forEach(::schedule)
        prefs.edit().putStringSet(KEY_NOTICE_IDS, notices.map { it.id }.toSet()).apply()
    }

    private fun schedule(notice: ShuttleServiceNoticeQuery.ServiceNotice) {
        val triggerAt = triggerTime(notice.date) ?: return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ShuttleServiceNoticeReceiver::class.java).apply {
            putExtra(ShuttleServiceNoticeReceiver.EXTRA_NOTICE_ID, notice.id)
            putExtra(ShuttleServiceNoticeReceiver.EXTRA_TITLE, context.getString(R.string.shuttle_service_notice_title))
            putExtra(ShuttleServiceNoticeReceiver.EXTRA_BODY, notice.bodyText())
        }
        val pendingIntent = pendingIntent(notice.id, intent)
        val triggerAtMillis = triggerAt.toInstant().toEpochMilli()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun cancel(noticeId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(noticeId, Intent(context, ShuttleServiceNoticeReceiver::class.java)))
    }

    private fun pendingIntent(
        noticeId: String,
        intent: Intent,
    ): PendingIntent = PendingIntent.getBroadcast(
        context,
        noticeId.hashCode(),
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun triggerTime(date: LocalDate): ZonedDateTime? {
        val now = ZonedDateTime.now(SERVICE_ZONE)
        val preferred = date.minusDays(1).atTime(LocalTime.of(9, 0)).atZone(SERVICE_ZONE)
        val fallback = date.atTime(LocalTime.of(8, 0)).atZone(SERVICE_ZONE)
        return when {
            preferred.isAfter(now) -> preferred
            fallback.isAfter(now) -> fallback
            else -> null
        }
    }

    private fun ShuttleServiceNoticeQuery.ServiceNotice.bodyText(): String {
        val dateText = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(date)
        return when (kind) {
            "period" -> context.getString(
                R.string.shuttle_service_notice_period_body,
                dateText,
                period?.type?.let(::periodName) ?: context.getString(R.string.shuttle_service_notice_period_unknown),
            )
            "holiday" -> context.getString(
                if (holiday?.type == "halt") {
                    R.string.shuttle_service_notice_halt_body
                } else {
                    R.string.shuttle_service_notice_holiday_body
                },
                dateText,
            )
            else -> context.getString(R.string.shuttle_service_notice_generic_body, dateText)
        }
    }

    private fun periodName(type: String): String = when (type) {
        "semester" -> context.getString(R.string.shuttle_service_notice_period_semester)
        "vacation" -> context.getString(R.string.shuttle_service_notice_period_vacation)
        "vacation_session" -> context.getString(R.string.shuttle_service_notice_period_session)
        else -> type
    }

    companion object {
        private val SERVICE_ZONE: ZoneId = ZoneId.of("Asia/Seoul")
        private const val LOOKAHEAD_DAYS = 30L
        private const val PREF_NAME = "shuttle_service_notice"
        private const val KEY_NOTICE_IDS = "notice_ids"
    }
}
