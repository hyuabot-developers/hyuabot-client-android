package app.kobuggi.hyuabot.util

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SwipeLayoutManager : LinearLayoutManager {
    constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams = scaledLayoutParams(super.generateDefaultLayoutParams())
    override fun generateLayoutParams(params: ViewGroup.LayoutParams): RecyclerView.LayoutParams = scaledLayoutParams(super.generateLayoutParams(params))
    override fun generateLayoutParams(
        c: Context?,
        attrs: AttributeSet?
    ): RecyclerView.LayoutParams = scaledLayoutParams(super.generateLayoutParams(c, attrs))
    private fun scaledLayoutParams(params: RecyclerView.LayoutParams): RecyclerView.LayoutParams {
        val width = (width * 0.8f).toInt()
        params.width = width
        return params
    }
}