package app.kobuggi.hyuabot.ui.common.coachmark

import android.view.View
import android.widget.FrameLayout
import androidx.compose.ui.geometry.Rect

/**
 * Compose 화면 위에 겹쳐 둔 보이지 않는 뷰를 [bounds](Compose 루트 좌표)에 맞춘다.
 * 코치마크는 View를 가리키므로, Compose 요소의 위치를 이 뷰로 알려 준다. 부모는 [FrameLayout]이어야 한다.
 */
fun View.placeAt(bounds: Rect) {
    val params = layoutParams as? FrameLayout.LayoutParams ?: return
    val width = bounds.width.toInt()
    val height = bounds.height.toInt()
    if (params.width != width || params.height != height) {
        params.width = width
        params.height = height
        layoutParams = params
    }
    translationX = bounds.left
    translationY = bounds.top
}
