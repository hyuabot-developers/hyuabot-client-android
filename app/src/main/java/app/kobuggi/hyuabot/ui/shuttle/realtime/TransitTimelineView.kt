package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.util.TransitRow
import app.kobuggi.hyuabot.util.TransitTimelineEntry
import app.kobuggi.hyuabot.util.TransitVehicleType
import kotlin.math.max
import kotlin.math.min

class TransitTimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private companion object {
        const val SUBWAY_SIDE_STATIONS = 3
        const val BUS_VISIBLE_STOPS = 7
    }

    private var row: TransitRow? = null
    private val density = resources.displayMetrics.density
    private val appTypeface = ResourcesCompat.getFont(context, R.font.godo)
    private val secondaryText = ContextCompat.getColor(context, R.color.secondary_text)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2.dp
        strokeCap = Paint.Cap.ROUND
    }
    private val dashedLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2.dp
        strokeCap = Paint.Cap.ROUND
        pathEffect = DashPathEffect(floatArrayOf(4.dp, 4.dp), 0f)
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
    }
    private val bubbleFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    private val bubbleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = secondaryText
        strokeWidth = 1.dp
        style = Paint.Style.STROKE
    }
    private val vehiclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val subwayIcon = ContextCompat.getDrawable(context, R.drawable.ic_subway)?.whiteIcon()
    private val busIcon = ContextCompat.getDrawable(context, R.drawable.ic_bus)?.whiteIcon()
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = secondaryText
        textSize = 11.sp
        typeface = appTypeface
    }
    private val emphasisLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary_text)
        textSize = 11.sp
        typeface = appTypeface
        isFakeBoldText = true
    }
    private val targetLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary_text)
        textSize = 12.sp
        isFakeBoldText = true
        typeface = appTypeface
    }

    fun bind(row: TransitRow) {
        this.row = row
        visibility = if (row.timeline.isEmpty()) GONE else VISIBLE
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val row = row ?: return
        val entries = row.timeline.take(2)
        if (entries.isEmpty()) return

        val color = ContextCompat.getColor(context, row.colorRes)
        linePaint.color = color
        dashedLinePaint.color = color
        dotPaint.color = color
        vehiclePaint.color = color

        val horizontalInset = 34.dp
        val left = paddingLeft + horizontalInset
        val right = width - paddingRight - horizontalInset
        val centerY = 22.dp
        val targetX = if (row.vehicleType == TransitVehicleType.SUBWAY) {
            (left + right) / 2f
        } else {
            right
        }
        val directions = entries.mapIndexed { index, entry ->
            val direction = if (entries.size > 1 && row.vehicleType == TransitVehicleType.SUBWAY && index == 1) 1 else -1
            direction to entry
        }
        if (row.vehicleType == TransitVehicleType.SUBWAY) {
            drawSubwayTrack(canvas, targetX, centerY, color, left, right, directions)
        }

        directions.forEach { (direction, entry) ->
            drawEntry(canvas, row.vehicleType, entry, targetX, centerY, direction, color, left, right)
        }
        drawTarget(canvas, targetX, centerY, color)
        drawTargetLabel(canvas, row.vehicleType, targetX, centerY)
    }

    private fun subwayStep(targetX: Float, left: Float, right: Float): Float =
        min(targetX - left, right - targetX) / SUBWAY_SIDE_STATIONS

    private fun busStep(targetX: Float, left: Float): Float =
        (targetX - left) / BUS_VISIBLE_STOPS

    private fun drawSubwayTrack(
        canvas: Canvas,
        targetX: Float,
        centerY: Float,
        color: Int,
        left: Float,
        right: Float,
        entriesByDirection: List<Pair<Int, TransitTimelineEntry>>,
    ) {
        linePaint.color = color
        dotPaint.color = color
        val step = subwayStep(targetX, left, right)
        listOf(-1, 1).forEach { direction ->
            val shouldCompress = entriesByDirection
                .firstOrNull { it.first == direction }
                ?.second
                ?.let { subwayStops(it) > SUBWAY_SIDE_STATIONS }
                ?: false
            val edgeX = (targetX + direction * SUBWAY_SIDE_STATIONS * step).coerceIn(left, right)
            if (shouldCompress) {
                canvas.drawLine(targetX, centerY, targetX + direction * 2 * step, centerY, linePaint)
                canvas.drawLine(targetX + direction * 2 * step, centerY, edgeX, centerY, dashedLinePaint)
            } else {
                canvas.drawLine(targetX, centerY, edgeX, centerY, linePaint)
            }
            for (i in 1..SUBWAY_SIDE_STATIONS) {
                val dotX = targetX + direction * i * step
                if (dotX in left..right) {
                    canvas.drawCircle(dotX, centerY, 4.dp, dotPaint)
                    canvas.drawCircle(dotX, centerY, 2.dp, whitePaint)
                }
            }
        }
    }

    private fun drawEntry(
        canvas: Canvas,
        type: TransitVehicleType,
        entry: TransitTimelineEntry,
        targetX: Float,
        centerY: Float,
        direction: Int,
        color: Int,
        left: Float,
        right: Float,
    ) {
        val stops = if (type == TransitVehicleType.SUBWAY) {
            subwayStops(entry)
        } else {
            entry.stops ?: entry.minutes?.let { max(1, min(5, (it + 2) / 3)) } ?: 2
        }
        val visibleStops = if (type == TransitVehicleType.SUBWAY) {
            stops.coerceIn(1, SUBWAY_SIDE_STATIONS)
        } else {
            stops.coerceIn(1, BUS_VISIBLE_STOPS)
        }
        val step = if (type == TransitVehicleType.SUBWAY) {
            subwayStep(targetX, left, right)
        } else {
            busStep(targetX, left)
        }
        val vehicleX = (targetX + direction * visibleStops * step).coerceIn(left, right)
        val far = entry.isRealtime && if (type == TransitVehicleType.SUBWAY) {
            stops >= visibleStops
        } else {
            stops > visibleStops
        }

        if (type == TransitVehicleType.BUS) {
            if (far) {
                val dashStart = targetX + direction * max(1, visibleStops - 1) * step
                canvas.drawLine(targetX, centerY, dashStart, centerY, linePaint)
                canvas.drawLine(dashStart, centerY, vehicleX, centerY, dashedLinePaint)
            } else {
                canvas.drawLine(targetX, centerY, targetX + direction * BUS_VISIBLE_STOPS * step, centerY, linePaint)
            }
            for (i in 1..BUS_VISIBLE_STOPS) {
                val dotX = targetX + direction * i * step
                canvas.drawCircle(dotX, centerY, 4.dp, dotPaint)
                canvas.drawCircle(dotX, centerY, 2.dp, whitePaint)
            }
        }
        drawVehicle(canvas, type, vehicleX, centerY, color, -direction)

        vehicleBubbleLines(type, entry).takeIf { it.isNotEmpty() }?.let {
            drawInfoBubble(canvas, it, vehicleX, centerY + 16.dp, left, right)
        }
    }

    private fun drawInfoBubble(
        canvas: Canvas,
        lines: List<BubbleLine>,
        anchorX: Float,
        top: Float,
        left: Float,
        right: Float,
    ) {
        val maxBubbleWidth = min(if (lines.size == 1) 88.dp else 58.dp, width - paddingLeft - paddingRight - 4.dp)
        val horizontalPadding = 6.dp
        val displayLines = lines.take(2).map { line ->
            line.copy(text = line.text.ellipsize(line.paint, (maxBubbleWidth - horizontalPadding * 2).toInt()))
        }
        val textWidth = displayLines.maxOf { it.paint.measureText(it.text) }
        val bubbleWidth = textWidth + horizontalPadding * 2
        val bubbleHeight = if (displayLines.size > 1) 32.dp else 20.dp
        val bubbleLeft = (anchorX - bubbleWidth / 2f).coerceIn(left - 14.dp, right + 14.dp - bubbleWidth)
        val bubble = RectF(bubbleLeft, top, bubbleLeft + bubbleWidth, top + bubbleHeight)
        val pointer = Path().apply {
            moveTo(anchorX - 4.dp, top)
            lineTo(anchorX, top - 4.dp)
            lineTo(anchorX + 4.dp, top)
            close()
        }
        canvas.drawRoundRect(bubble, 6.dp, 6.dp, bubbleFillPaint)
        canvas.drawPath(pointer, bubbleFillPaint)
        canvas.drawLine(anchorX - 4.dp, top, anchorX, top - 4.dp, bubbleStrokePaint)
        canvas.drawLine(anchorX, top - 4.dp, anchorX + 4.dp, top, bubbleStrokePaint)
        canvas.drawLine(bubble.left + 6.dp, bubble.top, anchorX - 4.dp, bubble.top, bubbleStrokePaint)
        canvas.drawLine(anchorX + 4.dp, bubble.top, bubble.right - 6.dp, bubble.top, bubbleStrokePaint)
        canvas.drawArc(RectF(bubble.left, bubble.top, bubble.left + 12.dp, bubble.top + 12.dp), 180f, 90f, false, bubbleStrokePaint)
        canvas.drawLine(bubble.left, bubble.top + 6.dp, bubble.left, bubble.bottom - 6.dp, bubbleStrokePaint)
        canvas.drawArc(RectF(bubble.left, bubble.bottom - 12.dp, bubble.left + 12.dp, bubble.bottom), 90f, 90f, false, bubbleStrokePaint)
        canvas.drawLine(bubble.left + 6.dp, bubble.bottom, bubble.right - 6.dp, bubble.bottom, bubbleStrokePaint)
        canvas.drawArc(RectF(bubble.right - 12.dp, bubble.bottom - 12.dp, bubble.right, bubble.bottom), 0f, 90f, false, bubbleStrokePaint)
        canvas.drawLine(bubble.right, bubble.bottom - 6.dp, bubble.right, bubble.top + 6.dp, bubbleStrokePaint)
        canvas.drawArc(RectF(bubble.right - 12.dp, bubble.top, bubble.right, bubble.top + 12.dp), 270f, 90f, false, bubbleStrokePaint)
        displayLines.forEachIndexed { index, line ->
            canvas.drawText(line.text, bubbleLeft + horizontalPadding, top + 14.dp + index * 12.dp, line.paint)
        }
    }

    private fun drawVehicle(canvas: Canvas, type: TransitVehicleType, x: Float, y: Float, color: Int, direction: Int) {
        vehiclePaint.color = color
        val width = 22.dp
        val height = 18.dp
        val halfBody = width / 2f - 4.dp
        val backX = x - direction * halfBody
        val frontBaseX = x + direction * halfBody
        val frontTipX = x + direction * width / 2f
        val top = y - height / 2f
        val bottom = y + height / 2f
        val body = Path().apply {
            moveTo(backX, top)
            lineTo(frontBaseX, top)
            lineTo(frontTipX, y)
            lineTo(frontBaseX, bottom)
            lineTo(backX, bottom)
            close()
        }
        canvas.drawPath(body, vehiclePaint)
        val icon = if (type == TransitVehicleType.BUS) busIcon else subwayIcon
        icon?.drawCentered(canvas, x, y, 13.dp)
    }

    private fun drawTarget(canvas: Canvas, x: Float, y: Float, color: Int) {
        dotPaint.color = color
        canvas.drawCircle(x, y, 7.dp, dotPaint)
        canvas.drawCircle(x, y, 3.dp, whitePaint)
    }

    private fun drawTargetLabel(canvas: Canvas, type: TransitVehicleType, targetX: Float, centerY: Float) {
        val isSubway = type == TransitVehicleType.SUBWAY
        val label = context.getString(
            if (isSubway) R.string.transfer_subway_boarding_station else R.string.transfer_bus_boarding_stop
        )
        targetLabelPaint.color = ContextCompat.getColor(context, R.color.primary_text)
        targetLabelPaint.isFakeBoldText = true
        val maxLabelWidth = if (isSubway) 72.dp else 48.dp
        val lines = label.split("\n").take(2).map {
            it.ellipsize(targetLabelPaint, maxLabelWidth.toInt())
        }
        val firstBaseline = centerY + if (isSubway) 26.dp else 26.dp
        drawCenteredLines(canvas, lines, targetX, firstBaseline, targetLabelPaint)
    }

    private fun subwayStops(entry: TransitTimelineEntry): Int =
        entry.stops ?: entry.locationLabel?.takeIf { it.isNotBlank() }?.let { SUBWAY_SIDE_STATIONS }
        ?: entry.minutes?.let { max(1, min(5, (it + 2) / 3)) }
        ?: 2

    private fun vehicleBubbleLines(type: TransitVehicleType, entry: TransitTimelineEntry): List<BubbleLine> {
        val minutes = entry.minutes
        val stops = entry.stops
        return if (type == TransitVehicleType.SUBWAY) {
            listOfNotNull(
                BubbleLine(context.getString(R.string.transfer_vehicle_destination, entry.destination), labelPaint),
                minutes?.let { BubbleLine(context.getString(R.string.transfer_bus_minutes_format, it), emphasisLabelPaint) },
            )
        } else {
            listOfNotNull(
                when {
                    minutes != null && stops != null ->
                        BubbleLine(context.getString(R.string.transfer_vehicle_minutes_stops, minutes, stops), labelPaint)
                    minutes != null -> BubbleLine(context.getString(R.string.transfer_bus_minutes_format, minutes), labelPaint)
                    stops != null -> BubbleLine(context.getString(R.string.transfer_bus_stops_suffix, stops).trim(), labelPaint)
                    else -> null
                }
            )
        }
    }

    private data class BubbleLine(
        val text: String,
        val paint: Paint,
    )

    private fun drawCenteredLines(
        canvas: Canvas,
        lines: List<String>,
        centerX: Float,
        firstBaseline: Float,
        paint: Paint,
    ) {
        lines.forEachIndexed { index, line ->
            val textWidth = paint.measureText(line)
            canvas.drawText(line, centerX - textWidth / 2f, firstBaseline + index * 12.dp, paint)
        }
    }

    private fun String.ellipsize(paint: Paint, maxWidth: Int): String {
        if (paint.measureText(this) <= maxWidth) return this
        var end = length
        while (end > 1 && paint.measureText(take(end) + "...") > maxWidth) {
            end--
        }
        return take(end) + "..."
    }

    private fun Drawable.whiteIcon(): Drawable = DrawableCompat.wrap(mutate()).also {
        DrawableCompat.setTint(it, android.graphics.Color.WHITE)
        it.alpha = 255
    }

    private fun Drawable.drawCentered(canvas: Canvas, centerX: Float, centerY: Float, size: Float) {
        val halfSize = size / 2f
        setBounds(
            (centerX - halfSize).toInt(),
            (centerY - halfSize).toInt(),
            (centerX + halfSize).toInt(),
            (centerY + halfSize).toInt(),
        )
        draw(canvas)
    }

    private val Int.dp: Float get() = this * density
    private val Int.sp: Float get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        toFloat(),
        resources.displayMetrics,
    )
}
