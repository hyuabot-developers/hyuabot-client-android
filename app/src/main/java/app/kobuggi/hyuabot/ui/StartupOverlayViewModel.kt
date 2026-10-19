package app.kobuggi.hyuabot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.ui.common.coachmark.StartupOverlay
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

@HiltViewModel
class StartupOverlayViewModel @Inject constructor() : ViewModel() {
    private val mutableDecision = MutableStateFlow<StartupOverlay?>(null)
    val decision: StateFlow<StartupOverlay?> = mutableDecision.asStateFlow()
    private var initialized = false
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

    @Synchronized
    fun initialize(decide: suspend () -> StartupOverlay) {
        if (initialized) return
        initialized = true
        viewModelScope.launch {
            mutableDecision.value = try {
                decide()
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                mutableDecision.value = StartupOverlay.NONE
                null
            } ?: StartupOverlay.NONE
        }
    }
}
