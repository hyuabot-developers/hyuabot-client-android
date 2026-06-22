package app.kobuggi.hyuabot.ui.common.coachmark

import android.app.Activity
import android.view.ViewGroup
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
        var finished = false
        val lifecycleObserver = lifecycleOwner?.let {
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    if (finished) return
                    finished = true
                    root.removeView(overlay)
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
        overlay.setSteps(steps) {
            if (finished) return@setSteps
            finished = true
            lifecycleObserver?.let { lifecycleOwner.lifecycle.removeObserver(it) }
            root.removeView(overlay)
            onFinish()
        }
    }
}
