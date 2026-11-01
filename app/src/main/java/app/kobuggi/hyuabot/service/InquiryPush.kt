package app.kobuggi.hyuabot.service

/** Constants and payload helpers for inquiry reply notifications. The backend sends data {"type":"inquiry","threadId":"<uuid>"} from InquiryPushService.notifyAdminMessage. */
object InquiryPush {
    const val CHANNEL_ID = "inquiry_reply"
    const val NOTIFICATION_ID = 41_001
    const val DATA_TYPE_KEY = "type"
    const val TYPE_INQUIRY = "inquiry"
    const val DATA_THREAD_ID_KEY = "threadId"
    const val EXTRA_OPEN_INQUIRY = "app.kobuggi.hyuabot.extra.OPEN_INQUIRY"
    const val PROVIDER_FCM = "FCM"
    const val PLATFORM_ANDROID = "ANDROID"

    @Volatile
    var isChatVisible: Boolean = false

    fun isInquiryPayload(data: Map<String, String>): Boolean = data[DATA_TYPE_KEY] == TYPE_INQUIRY

    fun isInquiryLaunch(openInquiryExtra: Boolean, typeExtra: String?): Boolean =
        openInquiryExtra || typeExtra == TYPE_INQUIRY
}
