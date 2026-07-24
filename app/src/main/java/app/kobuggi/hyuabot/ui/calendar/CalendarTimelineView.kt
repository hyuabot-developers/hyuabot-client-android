package app.kobuggi.hyuabot.ui.calendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.service.database.entity.Event
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class CalendarTimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var onDateSelected: ((LocalDate) -> Unit)? = null

    private var month: YearMonth = YearMonth.now()
    private var selectedDate: LocalDate? = null
    private var events: List<Event> = emptyList()
    private var firstDayOfWeek: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

    private val today = LocalDate.now()
    private val gridLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = color(R.color.calendar_grid_line)
        strokeWidth = dp(1f)
    }
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = color(R.color.calendar_selected_fill)
    }
    private val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = color(R.color.calendar_today_fill)
    }
    private val eventPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dateTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = color(R.color.primary_text)
        textAlign = Paint.Align.CENTER
        textSize = sp(16f)
        typeface = ResourcesCompat.getFont(context, R.font.godo)
    }
    private val eventTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = color(R.color.calendar_event_text)
        textSize = sp(11f)
        isFakeBoldText = true
        typeface = ResourcesCompat.getFont(context, R.font.godo)
    }

    fun setMonth(month: YearMonth) {
        this.month = month
        selectedDate = selectedDate?.takeIf { YearMonth.from(it) == month }
        invalidate()
    }

    fun setEvents(events: List<Event>) {
        this.events = events.sortedWith(compareBy<Event> { it.startDate }.thenByDescending {
            ChronoUnit.DAYS.between(LocalDate.parse(it.startDate), LocalDate.parse(it.endDate))
        })
        invalidate()
    }

    fun setFirstDayOfWeek(firstDayOfWeek: DayOfWeek) {
        this.firstDayOfWeek = firstDayOfWeek
        invalidate()
    }

    fun clearSelection() {
        selectedDate = null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val rowHeight = height / ROW_COUNT.toFloat()
        val columnWidth = width / COLUMN_COUNT.toFloat()
        val firstVisibleDate = firstVisibleDate()

        drawGrid(canvas, rowHeight)
        drawDates(canvas, firstVisibleDate, rowHeight, columnWidth)
        drawEvents(canvas, firstVisibleDate, rowHeight, columnWidth)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return true
        val rowHeight = height / ROW_COUNT.toFloat()
        val columnWidth = width / COLUMN_COUNT.toFloat()
        val row = floor(event.y / rowHeight).toInt().coerceIn(0, ROW_COUNT - 1)
        val column = floor(event.x / columnWidth).toInt().coerceIn(0, COLUMN_COUNT - 1)
        val date = firstVisibleDate().plusDays((row * COLUMN_COUNT + column).toLong())
        if (YearMonth.from(date) != month) return true

        selectedDate = date
        onDateSelected?.invoke(date)
        invalidate()
        return true
    }

    private fun drawGrid(canvas: Canvas, rowHeight: Float) {
        repeat(ROW_COUNT + 1) { row ->
            val y = row * rowHeight
            canvas.drawLine(0f, y, width.toFloat(), y, gridLinePaint)
        }
    }

    private fun drawDates(
        canvas: Canvas,
        firstVisibleDate: LocalDate,
        rowHeight: Float,
        columnWidth: Float
    ) {
        repeat(ROW_COUNT) { row ->
            repeat(COLUMN_COUNT) { column ->
                val date = firstVisibleDate.plusDays((row * COLUMN_COUNT + column).toLong())
                val centerX = column * columnWidth + columnWidth / 2f
                val dateCenterY = row * rowHeight + dp(22f)
                val isCurrentMonth = YearMonth.from(date) == month

                when {
                    selectedDate == date && today != date -> {
                        canvas.drawCircle(centerX, dateCenterY - dp(5f), dp(17f), selectedPaint)
                    }
                    today == date -> {
                        canvas.drawCircle(centerX, dateCenterY - dp(5f), dp(17f), todayPaint)
                    }
                }

                dateTextPaint.color = dateTextColor(date, isCurrentMonth)
                canvas.drawText(date.dayOfMonth.toString(), centerX, dateCenterY, dateTextPaint)
            }
        }
    }

    private fun drawEvents(
        canvas: Canvas,
        firstVisibleDate: LocalDate,
        rowHeight: Float,
        columnWidth: Float
    ) {
        val maxSlots = if (rowHeight >= dp(64f)) 2 else 1
        val segmentsByRow = buildEventSegments(firstVisibleDate, maxSlots)

        segmentsByRow.forEach { (row, segments) ->
            segments.forEach { segment ->
                val left = segment.startColumn * columnWidth + if (segment.roundLeft) dp(3f) else 0f
                val right = (segment.endColumn + 1) * columnWidth - if (segment.roundRight) dp(3f) else 0f
                val top = row * rowHeight + dp(38f) + segment.slot * dp(16f)
                val bottom = top + dp(14f)
                val color = eventColor(segment.event.category)
                val rect = RectF(left, top, right, bottom)

                eventPaint.color = color
                drawSegment(canvas, rect, segment.roundLeft, segment.roundRight)
                drawEventTitle(canvas, segment.event.title, rect)
            }
        }
    }

    private fun drawSegment(canvas: Canvas, rect: RectF, roundLeft: Boolean, roundRight: Boolean) {
        val radius = dp(4f)
        canvas.drawRoundRect(rect, radius, radius, eventPaint)
        if (!roundLeft) {
            canvas.drawRect(rect.left, rect.top, rect.left + radius, rect.bottom, eventPaint)
        }
        if (!roundRight) {
            canvas.drawRect(rect.right - radius, rect.top, rect.right, rect.bottom, eventPaint)
        }
    }

    private fun drawEventTitle(canvas: Canvas, title: String, rect: RectF) {
        val horizontalPadding = dp(6f)
        val textWidth = rect.width() - horizontalPadding * 2
        if (textWidth <= dp(12f)) return

        val titleText = TextUtils.ellipsize(
            title,
            eventTextPaint,
            textWidth,
            TextUtils.TruncateAt.END
        ).toString()
        val baseline = rect.centerY() - (eventTextPaint.descent() + eventTextPaint.ascent()) / 2f

        canvas.save()
        canvas.clipRect(rect)
        canvas.drawText(titleText, rect.left + horizontalPadding, baseline, eventTextPaint)
        canvas.restore()
    }

    private fun buildEventSegments(
        firstVisibleDate: LocalDate,
        maxSlots: Int
    ): Map<Int, List<EventSegment>> {
        val lastVisibleDate = firstVisibleDate.plusDays((ROW_COUNT * COLUMN_COUNT - 1).toLong())
        val rows = mutableMapOf<Int, MutableList<EventSegment>>()
        val occupiedRanges = Array(ROW_COUNT) { Array(maxSlots) { mutableListOf<IntRange>() } }

        events.forEach { event ->
            val eventStart = LocalDate.parse(event.startDate)
            val eventEnd = LocalDate.parse(event.endDate)
            if (eventEnd < firstVisibleDate || eventStart > lastVisibleDate) return@forEach

            repeat(ROW_COUNT) { row ->
                val weekStart = firstVisibleDate.plusDays((row * COLUMN_COUNT).toLong())
                val weekEnd = weekStart.plusDays((COLUMN_COUNT - 1).toLong())
                val segmentStart = maxOf(eventStart, weekStart, firstVisibleDate)
                val segmentEnd = minOf(eventEnd, weekEnd, lastVisibleDate)
                if (segmentStart > segmentEnd) return@repeat

                val startColumn = ChronoUnit.DAYS.between(weekStart, segmentStart).toInt()
                val endColumn = ChronoUnit.DAYS.between(weekStart, segmentEnd).toInt()
                val range = startColumn..endColumn
                val slot = (0 until maxSlots).firstOrNull { slot ->
                    occupiedRanges[row][slot].none { it.overlaps(range) }
                } ?: return@repeat

                occupiedRanges[row][slot].add(range)
                rows.getOrPut(row) { mutableListOf() }.add(
                    EventSegment(
                        event = event,
                        startColumn = startColumn,
                        endColumn = endColumn,
                        slot = slot,
                        roundLeft = segmentStart == eventStart,
                        roundRight = segmentEnd == eventEnd
                    )
                )
            }
        }
        return rows
    }

    private fun firstVisibleDate(): LocalDate {
        val firstOfMonth = month.atDay(1)
        val offset = Math.floorMod(
            firstOfMonth.dayOfWeek.value - firstDayOfWeek.value,
            COLUMN_COUNT
        )
        return firstOfMonth.minusDays(offset.toLong())
    }

    private fun dateTextColor(date: LocalDate, currentMonth: Boolean): Int {
        if (date == today) return color(R.color.calendar_event_text)
        if (!currentMonth) return color(R.color.secondary_text)
        return when (date.dayOfWeek) {
            DayOfWeek.SUNDAY -> color(R.color.calendar_sunday)
            DayOfWeek.SATURDAY -> color(R.color.calendar_saturday)
            else -> color(R.color.primary_text)
        }
    }

    private fun eventColor(category: String): Int {
        return when (category) {
            "1학년" -> color(R.color.calendar_category_blue)
            "2학년" -> color(R.color.calendar_category_orange)
            "3학년" -> color(R.color.calendar_category_green)
            "4학년" -> color(R.color.calendar_category_purple)
            else -> color(R.color.calendar_category_orange)
        }
    }

    private fun IntRange.overlaps(other: IntRange): Boolean {
        return first <= other.last && other.first <= last
    }

    private fun color(colorRes: Int): Int = ContextCompat.getColor(context, colorRes)

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    private fun sp(value: Float): Float = value * resources.displayMetrics.scaledDensity

    private data class EventSegment(
        val event: Event,
        val startColumn: Int,
        val endColumn: Int,
        val slot: Int,
        val roundLeft: Boolean,
        val roundRight: Boolean
    )

    companion object {
        private const val ROW_COUNT = 6
        private const val COLUMN_COUNT = 7
    }
}
