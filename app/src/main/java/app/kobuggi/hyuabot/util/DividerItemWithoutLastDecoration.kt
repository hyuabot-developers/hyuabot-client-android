package app.kobuggi.hyuabot.util

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

class DividerItemWithoutLastDecoration(
    context: Context,
    orientation: Int,
) : DividerItemDecoration(context, orientation) {

    init {
        setDrawable(ContextCompat.getDrawable(context, android.R.drawable.divider_horizontal_bright)!!)
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position: Int = parent.getChildAdapterPosition(view)
        if (position == state.itemCount - 1) {
            outRect.setEmpty()
        } else {
            super.getItemOffsets(outRect, view, parent, state)
        }
    }
}
