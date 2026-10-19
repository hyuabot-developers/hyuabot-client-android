package app.kobuggi.hyuabot.ui.common

import android.content.Context
import android.util.AttributeSet
import android.graphics.Typeface
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.GradientDrawable
import android.content.res.Configuration
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.core.widget.addTextChangedListener
import app.kobuggi.hyuabot.R
import java.time.Instant
import java.time.ZoneId

/** Shared status presentation. The badge itself is decorative; put [label] in the row's
 * content description so assistive technology reads the status in context. */
class StatusBadge @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatTextView(context, attrs) {
    enum class Kind { REALTIME, SCHEDULED, APPROACHING, LAST_RUN, ENDED, CLOSED, STALE }

    init {
        textSize = 12f
        setTypeface(typeface, Typeface.BOLD)
        gravity = Gravity.CENTER
        setPadding(dp(8), dp(2), dp(8), dp(2))
        minHeight = dp(20)
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    fun show(label: CharSequence, kind: Kind) {
        text = label
        val dark = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val (foreground, backgroundColor) = when (kind) {
            Kind.REALTIME -> (if (dark) 0xFF4ADE80.toInt() else 0xFF166534.toInt()) to (if (dark) 0xFF14301F.toInt() else 0xFFE8F5EC.toInt())
            Kind.SCHEDULED -> resolveColor(com.google.android.material.R.attr.colorOnSurfaceVariant) to null
            Kind.APPROACHING -> (if (dark) 0xFFFF9F0A.toInt() else 0xFFC2410C.toInt()) to (if (dark) 0xFF3A2410.toInt() else 0xFFFFF1E6.toInt())
            Kind.LAST_RUN -> (if (dark) 0xFFFF6961.toInt() else 0xFFB42318.toInt()) to (if (dark) 0xFF3B1D1B.toInt() else 0xFFFDECEA.toInt())
            Kind.ENDED, Kind.CLOSED -> (if (dark) 0xFFC3C6CF.toInt() else 0xFF43474E.toInt()) to (if (dark) 0xFF33353A.toInt() else 0xFFE1E2E8.toInt())
            Kind.STALE -> (if (dark) 0xFFFFB340.toInt() else 0xFFA15C00.toInt()) to (if (dark) 0xFF3A2A10.toInt() else 0xFFFFF6E0.toInt())
        }
        setTextColor(foreground)
        this.background = backgroundColor?.let { color ->
            GradientDrawable().apply {
                setColor(color)
                cornerRadius = dp(4).toFloat()
            }
        }
        compoundDrawablePadding = dp(4)
        val mark = when (kind) {
            Kind.REALTIME -> StatusMark(dp(6), foreground, false)
            else -> null
        }
        setCompoundDrawablesRelative(mark, null, null, null)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun resolveColor(attribute: Int): Int {
        val value = android.util.TypedValue()
        context.theme.resolveAttribute(attribute, value, true)
        return if (value.resourceId != 0) resources.getColor(value.resourceId, context.theme) else value.data
    }
}

/** A timestamp label that only schedules its one-second ticker while attached to a visible view. */
class FreshnessLabel @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatTextView(context, attrs) {
    enum class Source { SCHEDULE, REALTIME, READING_ROOM, HOURLY, OBSERVED }

    var source: Source = Source.SCHEDULE
        set(value) { field = value; render() }
    var analyticsDomain: String? = null
    var onSpokenChanged: ((CharSequence) -> Unit)? = null
    /** Screen name read before the freshness text, e.g. the bus or subway screen title. */
    var spokenPrefix: CharSequence? = null
    private var wasStale = false
    var analyticsScreen: String? = null
    var updatedAt: Instant? = null
        set(value) { field = value; render() }
    var delayAfter: java.time.Duration = java.time.Duration.ofMinutes(3)
        set(value) { field = value; render() }

    private val ticker = object : Runnable {
        override fun run() {
            render()
            if (isEffectivelyVisible()) postDelayed(this, 1_000L)
        }
    }

    init {
        textSize = 12f
        // Same typography as the bottom tab labels: Godo Bold, LabelMedium 12sp (spec D3, revised).
        setTypeface(ResourcesCompat.getFont(context, R.font.godo), Typeface.BOLD)
        accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_NONE
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        removeCallbacks(ticker)
        render()
        if (isEffectivelyVisible()) postDelayed(ticker, 1_000L)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(ticker)
        super.onDetachedFromWindow()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        removeCallbacks(ticker)
        if (visibility == View.VISIBLE && isEffectivelyVisible()) post(ticker)
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        removeCallbacks(ticker)
        if (isVisible && isEffectivelyVisible()) post(ticker)
    }

    private fun isEffectivelyVisible(): Boolean = isAttachedToWindow && isShown && windowVisibility == View.VISIBLE

    private fun render() {
        val category = when (source) {
            Source.SCHEDULE -> Freshness.Category.SCHEDULE
            Source.REALTIME -> Freshness.Category.REALTIME
            Source.READING_ROOM -> Freshness.Category.READING_ROOM
            Source.HOURLY -> Freshness.Category.HOURLY
            Source.OBSERVED -> Freshness.Category.OBSERVED
        }
        val display = Freshness.display(category, updatedAt, Instant.now(), ZoneId.systemDefault(), ResourceFreshnessText(context), staleAfterOverride = delayAfter)
        val stale = display.stale
        // Once per fresh -> stale transition for this label (one label per screen view).
        if (stale && !wasStale) {
            val domain = analyticsDomain ?: when (source) {
                Source.READING_ROOM -> "readingRoom"
                else -> null
            }
            val screen = analyticsScreen ?: if (source == Source.READING_ROOM) "reading_room" else null
            if (domain != null && screen != null) {
                app.kobuggi.hyuabot.util.AnalyticsManager.logFreshnessStaleShown(domain, screen)
            }
        }
        wasStale = stale
        text = display.text
        val spoken = spokenPrefix?.let { "$it, ${display.spoken}" } ?: display.spoken
        if (contentDescription != spoken) {
            contentDescription = spoken
            onSpokenChanged?.invoke(spoken)
        }
        val dark = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val color = when {
            stale && dark -> 0xFFFFB340.toInt()
            stale -> 0xFFA15C00.toInt()
            else -> resolveColor(com.google.android.material.R.attr.colorOnSurfaceVariant)
        }
        setTextColor(color)
        background = if (stale) GradientDrawable().apply {
            setColor(if (dark) 0xFF3A2A10.toInt() else 0xFFFFF6E0.toInt())
            cornerRadius = 4f * resources.displayMetrics.density
        } else null
        val mark = if (stale) StatusMark((14 * resources.displayMetrics.density).toInt(), color, true) else null
        compoundDrawablePadding = if (stale) (4 * resources.displayMetrics.density).toInt() else 0
        setCompoundDrawablesRelative(mark, null, null, null)
    }

    private fun resolveColor(attribute: Int): Int {
        val value = android.util.TypedValue()
        context.theme.resolveAttribute(attribute, value, true)
        return if (value.resourceId != 0) resources.getColor(value.resourceId, context.theme) else value.data
    }
}

/** Right-aligned fixed-width time text for departure rows. */
class TimeText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatTextView(context, attrs) {
    init {
        textSize = 22f
        setTypeface(typeface, Typeface.BOLD)
        gravity = Gravity.CENTER_VERTICAL or Gravity.END
        includeFontPadding = false
        refreshMinimumWidth()
    }

    fun show(minutes: Long, stale: Boolean = false, approaching: Boolean = false) {
        text = when {
            minutes >= 60 -> Freshness.timeText(minutes, ResourceFreshnessText(context))
            minutes < 0 -> Freshness.timeText(minutes, ResourceFreshnessText(context))
            else -> SpannableString(Freshness.timeText(minutes, ResourceFreshnessText(context))).apply {
                setSpan(RelativeSizeSpan(.55f), length - 1, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        setTextColor(when {
            stale || minutes < 0 -> 0xFF43474E.toInt()
            approaching -> 0xFFC2410C.toInt()
            else -> 0xFF1A1B1F.toInt()
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        refreshMinimumWidth()
    }

    private fun refreshMinimumWidth() {
        minWidth = paint.measureText("99:59").toInt()
    }
}

/** Reusable section heading with a consistent title and optional trailing action. */
class SectionHeader @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    val titleView = AppCompatTextView(context)
    val actionView = AppCompatTextView(context)

    init {
        titleView.addTextChangedListener { refreshDescription() }
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        minimumHeight = (48 * resources.displayMetrics.density).toInt()
        setPadding((16 * resources.displayMetrics.density).toInt(), 0, (16 * resources.displayMetrics.density).toInt(), 0)
        setBackgroundColor(resolveColor(com.google.android.material.R.attr.colorSurface))
        isFocusable = true
        descendantFocusability = FOCUS_BLOCK_DESCENDANTS
        titleView.textSize = 16f
        titleView.setTypeface(titleView.typeface, Typeface.BOLD)
        titleView.setTextColor(resolveColor(com.google.android.material.R.attr.colorOnSurface))
        // Heading semantics belong on the focusable container: the title child is hidden from
        // accessibility, so TalkBack would otherwise never expose this row as a heading.
        isAccessibilityHeading = true
        titleView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        titleView.layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        actionView.textSize = 14f
        actionView.visibility = View.GONE
        actionView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        addView(titleView)
        addView(actionView)
        updateStacking()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateStacking()
    }

    private fun updateStacking() {
        val stacked = resources.configuration.fontScale >= 1.5f
        orientation = if (stacked) VERTICAL else HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL or if (stacked) Gravity.START else Gravity.CENTER
        titleView.layoutParams = if (stacked) {
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        } else LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
    }

    fun show(title: CharSequence, action: CharSequence? = null) {
        titleView.text = title
        actionView.text = action ?: ""
        actionView.visibility = if (action.isNullOrEmpty()) View.GONE else View.VISIBLE
        refreshDescription()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        refreshDescription()
    }

    /** One focus for the whole header: title, action, and the freshness label read together (spec 4.7). */
    private fun refreshDescription() {
        val parts = listOfNotNull(
            titleView.text.takeIf { it.isNotEmpty() },
            actionView.text.takeIf { actionView.visibility == View.VISIBLE && it.isNotEmpty() },
        )
        contentDescription = parts.joinToString(", ")
    }

    private fun resolveColor(attribute: Int): Int {
        val value = android.util.TypedValue()
        context.theme.resolveAttribute(attribute, value, true)
        return if (value.resourceId != 0) resources.getColor(value.resourceId, context.theme) else value.data
    }
}

private class StatusMark(
    private val size: Int,
    private val color: Int,
    private val warning: Boolean,
) : android.graphics.drawable.Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL }
    override fun draw(canvas: Canvas) {
        val b = bounds
        if (!warning) {
            canvas.drawCircle(b.exactCenterX(), b.exactCenterY(), size / 2f, paint)
        } else {
            val path = Path().apply {
                moveTo(b.exactCenterX(), b.top.toFloat())
                lineTo(b.right.toFloat(), b.bottom.toFloat())
                lineTo(b.left.toFloat(), b.bottom.toFloat())
                close()
            }
            paint.color = color
            canvas.drawPath(path, paint)
            paint.color = 0xFFFFFFFF.toInt()
            paint.textSize = size * .68f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("!", b.exactCenterX(), b.bottom - size * .16f, paint)
        }
    }
    override fun setAlpha(alpha: Int) { paint.alpha = alpha }
    override fun setColorFilter(filter: android.graphics.ColorFilter?) { paint.colorFilter = filter }
    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
    override fun getIntrinsicWidth(): Int = size
    override fun getIntrinsicHeight(): Int = size
}

/** Freshness wording from the app's string resources (Korean default, English in values-en). */
private class ResourceFreshnessText(private val context: Context) : FreshnessText {
    private fun str(id: Int, vararg args: Any): String = context.getString(id, *args)
    override fun scheduleBasis() = str(R.string.freshness_schedule_basis)
    override fun hourly() = str(R.string.freshness_hourly)
    override fun observed(time: String) = str(R.string.freshness_observed, time)
    override fun delayed(minutes: Long) = str(R.string.freshness_delayed, minutes)
    override fun readingRoomUpdated(minutes: Long) = str(R.string.freshness_reading_room_updated, minutes)
    override fun secondsUpdated(seconds: Long) = str(R.string.freshness_seconds_updated, seconds)
    override fun minutesUpdated(minutes: Long) = str(R.string.freshness_minutes_updated, minutes)
    override fun spokenDelayed(minutes: Long) = str(R.string.freshness_spoken_delayed, minutes)
    override fun spokenUnderMinute() = str(R.string.freshness_spoken_under_minute)
    override fun spokenMinutes(minutes: Long) = str(R.string.freshness_spoken_minutes, minutes)
    override fun minuteUnit(minutes: Long) = str(R.string.freshness_minute_unit, minutes)
}
