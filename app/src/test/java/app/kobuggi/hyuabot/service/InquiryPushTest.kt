package app.kobuggi.hyuabot.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InquiryPushTest {
    @Test
    fun isInquiryPayloadRecognizesInquiryData() {
        assertTrue(
            InquiryPush.isInquiryPayload(
                mapOf("type" to "inquiry", "threadId" to "00000000-0000-0000-0000-000000000001"),
            ),
        )
        assertFalse(InquiryPush.isInquiryPayload(mapOf("id" to "reading_room_1", "available" to "3")))
        assertFalse(InquiryPush.isInquiryPayload(emptyMap()))
    }

    @Test
    fun isInquiryLaunchRecognizesInquiryLaunchSignals() {
        assertTrue(InquiryPush.isInquiryLaunch(false, "inquiry"))
        assertTrue(InquiryPush.isInquiryLaunch(true, null))
        assertFalse(InquiryPush.isInquiryLaunch(false, null))
        assertFalse(InquiryPush.isInquiryLaunch(false, "other"))
    }

    @Test
    fun exposesNotificationRegistrationConstants() {
        assertEquals("inquiry_reply", InquiryPush.CHANNEL_ID)
        assertEquals("FCM", InquiryPush.PROVIDER_FCM)
        assertEquals("ANDROID", InquiryPush.PLATFORM_ANDROID)
    }
}
