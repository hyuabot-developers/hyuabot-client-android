package app.kobuggi.hyuabot.ui.common.coachmark

import android.app.Activity
import android.view.ViewGroup
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

object CoachmarkController {
    private const val OVERLAY_TAG = "coachmark_overlay"

    fun show(
        activity: Activity,
        steps: List<CoachmarkStep>,
        lifecycleOwner: LifecycleOwner? = null,
        onFinish: () -> Unit = {},
        onStepShown: (Int) -> Unit = {},
        onSkip: (Int) -> Unit = {},
        onComplete: (Int) -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        if (steps.isEmpty()) {
            onFinish()
            return
        }
        if (lifecycleOwner?.lifecycle?.currentState == Lifecycle.State.DESTROYED) return

        val root = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        if (root.findViewWithTag<CoachmarkOverlayView>(OVERLAY_TAG) != null) return

        val overlay = CoachmarkOverlayView(activity)
        overlay.tag = OVERLAY_TAG
        val accessibilityStates = root.children().associateWith { it.importantForAccessibility }
        accessibilityStates.keys.forEach { it.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS }
        fun restoreAccessibility() {
            accessibilityStates.forEach { (view, state) -> view.importantForAccessibility = state }
        }
        var finished = false
        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = overlay.skip()
        }
        (activity as? ComponentActivity)?.onBackPressedDispatcher?.addCallback(
            lifecycleOwner ?: activity,
            backCallback,
        )
        val lifecycleObserver = lifecycleOwner?.let {
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    if (finished) return
                    finished = true
                    backCallback.remove()
                    root.removeView(overlay)
                    restoreAccessibility()
                    onDismiss()
                }
            }
        }
        lifecycleObserver?.let { lifecycleOwner.lifecycle.addObserver(it) }

        root.addView(
            overlay,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        fun close(aborted: Boolean) {
            if (finished) return
            finished = true
            backCallback.remove()
            lifecycleObserver?.let { lifecycleOwner.lifecycle.removeObserver(it) }
            root.removeView(overlay)
            restoreAccessibility()
            if (aborted) onDismiss() else onFinish()
        }
        overlay.setSteps(
            steps = steps,
            onFinish = { close(false) },
            onSkip = onSkip,
            onComplete = onComplete,
            onAbort = { close(true) },
            onStepShown = onStepShown,
        )
    }

    private fun ViewGroup.children(): List<View> = (0 until childCount).map(::getChildAt)
}
