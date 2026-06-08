package app.kobuggi.hyuabot.ui.common.coachmark

import android.view.View

enum class CoachmarkShape { ROUNDED_RECT, CIRCLE }

data class CoachmarkStep(
    val targetProvider: () -> View?,
    val titleRes: Int,
    val descRes: Int,
    val shape: CoachmarkShape = CoachmarkShape.ROUNDED_RECT,
    val allowTapThrough: Boolean = false,
    val onShow: ((View) -> Unit)? = null,
    val centered: Boolean = false,
)
