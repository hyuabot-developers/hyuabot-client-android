package app.kobuggi.hyuabot.util

import android.view.View
import com.faltenreich.skeletonlayout.SkeletonLayout

fun SkeletonLayout.setSkeletonLoading(isLoading: Boolean) {
    if (isLoading) {
        visibility = View.VISIBLE
        showSkeleton()
    } else {
        showOriginal()
        visibility = View.GONE
    }
}
