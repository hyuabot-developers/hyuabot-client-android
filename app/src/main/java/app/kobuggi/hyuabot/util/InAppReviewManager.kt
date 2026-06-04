package app.kobuggi.hyuabot.util

import android.app.Activity
import android.util.Log
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppReviewManager @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend fun maybeRequestReview(activity: Activity) {
        val launchCount = userPreferencesRepository.incrementLaunchCount()
        if (launchCount < LAUNCH_THRESHOLD) return
        val lastRequestedAt = userPreferencesRepository.reviewRequestedAt.first()
        val now = System.currentTimeMillis()
        if (now - lastRequestedAt < REVIEW_COOLDOWN_MILLIS) return
        if (launchReview(activity)) {
            userPreferencesRepository.setReviewRequestedAt(now)
            userPreferencesRepository.resetLaunchCount()
        }
    }

    suspend fun launchReview(activity: Activity): Boolean {
        return try {
            val manager = ReviewManagerFactory.create(activity)
            val reviewInfo = manager.requestReview()
            manager.launchReview(activity, reviewInfo)
            true
        } catch (e: Exception) {
            Log.w(TAG, "In-app review request failed", e)
            false
        }
    }

    companion object {
        private const val TAG = "InAppReviewManager"
        private const val LAUNCH_THRESHOLD = 10
        private const val REVIEW_COOLDOWN_MILLIS = 60L * 24 * 60 * 60 * 1000
    }
}
