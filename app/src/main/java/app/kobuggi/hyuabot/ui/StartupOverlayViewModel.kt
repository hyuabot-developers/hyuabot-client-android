package app.kobuggi.hyuabot.ui

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import app.kobuggi.hyuabot.ui.common.coachmark.StartupOverlay
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@HiltViewModel
class StartupOverlayViewModel @Inject constructor() : ViewModel() {
    private val mutableDecision = MutableStateFlow<StartupOverlay?>(null)
    val decision: StateFlow<StartupOverlay?> = mutableDecision.asStateFlow()
    private val initializationMutex = Mutex()
    private var coachmarkClaimedKey: String? = null
    private var reviewRequested = false

    @Synchronized
    fun claimCoachmark(key: String): Boolean {
        if (mutableDecision.value != StartupOverlay.COACHMARK) return true
        val claimedKey = coachmarkClaimedKey
        if (claimedKey == key) return false
        if (claimedKey == null) coachmarkClaimedKey = key
        return true
    }

    @Synchronized
    fun claimReviewRequest(): Boolean {
        if (mutableDecision.value != StartupOverlay.REVIEW || reviewRequested) return false
        reviewRequested = true
        return true
    }

    @Synchronized
    fun releaseCoachmarkClaim() {
        if (mutableDecision.value == StartupOverlay.COACHMARK) coachmarkClaimedKey = null
    }

    @MainThread
    suspend fun initialize(decide: suspend () -> StartupOverlay) = initializationMutex.withLock {
        if (mutableDecision.value != null) return@withLock
        mutableDecision.value = try {
            decide()
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            StartupOverlay.NONE
        }
    }
}
