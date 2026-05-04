package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import app.kobuggi.hyuabot.R
import androidx.core.graphics.withRotation

class ShuttleRouteItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    data class Route(
        val color: Int,
        val stops: List<Int>,
        val currentStopIndex: Int,
        val labels: Map<Int, Int>? = null,
    )
    private val allStops = listOf(
        R.string.shuttle_tab_dormitory_out,
        R.string.shuttle_tab_shuttlecock_out,
        R.string.shuttle_tab_station,
        R.string.shuttle_tab_terminal,
        R.string.shuttle_type_jungang,
        R.string.shuttle_tab_shuttlecock_in,
        R.string.shuttle_type_dormitory
    )
    private val passedColor = Color.LTGRAY
    private var routeData: Route? = null
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 28f
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }
    private var colWidth = 0f
    private var centerY = 0f
    private var paddingWidth = 0f
    private val stopPositions = FloatArray(allStops.size)

    fun bind(data: Route) {
        routeData = data
        recalculateStopPositions()
        invalidate()
    }

    private fun recalculateStopPositions() {
        routeData?.stops?.forEachIndexed { index, stop ->
            stopPositions[index] = paddingWidth + allStops.indexOf(stop) * colWidth
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        centerY = 26f
        colWidth = width / allStops.size.toFloat()
        paddingWidth = colWidth / 2f
        recalculateStopPositions()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val data = routeData ?: return
        // Draw lines
        for (i in 0 until data.stops.size - 1) {
            linePaint.color = if (i < data.currentStopIndex) passedColor else data.color
            canvas.drawLine(stopPositions[i], centerY, stopPositions[i + 1], centerY, linePaint)
        }
        // Draw circles and labels
        for (i in 0 until data.stops.size) {
            val x = stopPositions[i]
            val isPassed = i < data.currentStopIndex
            val isCurrent = i == data.currentStopIndex
            val color = if (isPassed) passedColor else data.color
            if (isCurrent) {
                linePaint.color = data.color
                canvas.drawCircle(x, centerY, 22f, linePaint)
            }
            circlePaint.color = color
            canvas.drawCircle(x, centerY, if (isCurrent) 18f else 12f, circlePaint)
            data.labels?.get(data.stops[i])?.let { label ->
                if (label <= 0) return@let
                textPaint.color = if (isPassed) Color.GRAY else Color.WHITE
                val radius = if (isCurrent) 18f else 12f
                val pivotX = x + radius + 35
                val pivotY = centerY + radius + 15
                canvas.withRotation(45f, pivotX, pivotY) {
                    drawText(
                        context.getString(R.string.shuttle_realtime_duration_format, label),
                        pivotX,
                        pivotY + textPaint.textSize,
                        textPaint
                    )
                }
            }
        }
    }
}
