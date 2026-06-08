package app.kobuggi.hyuabot.ui.common.coachmark

import android.app.Activity
import android.view.ViewGroup

object CoachmarkController {
    private const val OVERLAY_TAG = "coachmark_overlay"

    fun show(activity: Activity, steps: List<CoachmarkStep>, onFinish: () -> Unit = {}) {
        if (steps.isEmpty()) {
            onFinish()
            return
        }
        val root = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        if (root.findViewWithTag<CoachmarkOverlayView>(OVERLAY_TAG) != null) return

        val overlay = CoachmarkOverlayView(activity)
        overlay.tag = OVERLAY_TAG
        root.addView(
            overlay,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        overlay.setSteps(steps) {
            root.removeView(overlay)
            onFinish()
        }
    }
}
