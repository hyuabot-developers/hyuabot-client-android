package app.kobuggi.hyuabot.util

class TimetableUtil {
    companion object {
        fun add24Hour(timeItem: String): String {
            val time = timeItem.split(":")
            val hour = time[0].toInt()
            val minute = time[1].toInt()
            return if (hour < 4) {
                "${hour + 24}:${minute.toString().padStart(2, '0')}"
            } else {
                timeItem
            }
        }
    }
}