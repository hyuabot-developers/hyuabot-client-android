package app.kobuggi.hyuabot.util

import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import com.google.android.material.color.MaterialColors
import com.faltenreich.skeletonlayout.SkeletonLayout
import app.kobuggi.hyuabot.ui.common.LoadingPresentation

fun SkeletonLayout.setSkeletonLoading(isLoading: Boolean) {
    if (isLoading) {
        maskColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurfaceContainerHighest, maskColor)
        shimmerColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurfaceContainerHigh, shimmerColor)
        val scale = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        showShimmer = LoadingPresentation.shouldAnimate(scale)
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        isFocusable = true
        contentDescription = context.getString(app.kobuggi.hyuabot.R.string.state_loading)
        visibility = View.VISIBLE
        showSkeleton()
    } else {
        showOriginal()
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        isFocusable = false
        contentDescription = null
        visibility = View.GONE
    }
}
