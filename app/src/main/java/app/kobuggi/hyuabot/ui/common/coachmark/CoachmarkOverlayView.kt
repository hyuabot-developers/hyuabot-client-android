package app.kobuggi.hyuabot.ui.common.coachmark

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ViewCoachmarkBubbleBinding

class CoachmarkOverlayView(context: Context) : FrameLayout(context) {
    private val scrimColor = "#B3000000".toColorInt()
    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2f)
        color = Color.WHITE
    }

    private val spotlight = RectF()
    private var hasSpotlight = false
    private var currentShape = CoachmarkShape.ROUNDED_RECT
    private val cornerRadius = dp(12f)
    private val gap = dp(16f)

    private var steps: List<CoachmarkStep> = emptyList()
    private var index = 0
    private var onFinish: () -> Unit = {}
    private var allowTapThrough = false

    private val bubbleBinding = ViewCoachmarkBubbleBinding.inflate(LayoutInflater.from(context), this, false)

    private val relayoutListener = ViewTreeObserver.OnGlobalLayoutListener { resolveCurrentSpotlight() }
    private val scrollListener = ViewTreeObserver.OnScrollChangedListener { resolveCurrentSpotlight() }

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)
        isClickable = true
        isFocusable = true
        val horizontalMargin = dp(16f).toInt()
        bubbleBinding.root.updateLayoutParams<LayoutParams> {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            marginStart = horizontalMargin
            marginEnd = horizontalMargin
        }
        addView(bubbleBinding.root)
        bubbleBinding.coachmarkNext.setOnClickListener { advance() }
        bubbleBinding.coachmarkSkip.setOnClickListener { finish() }
    }

    fun setSteps(steps: List<CoachmarkStep>, onFinish: () -> Unit) {
        this.steps = steps
        this.onFinish = onFinish
        this.index = 0
        doOnLayout { showStep() }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnGlobalLayoutListener(relayoutListener)
        viewTreeObserver.addOnScrollChangedListener(scrollListener)
    }

    override fun onDetachedFromWindow() {
        viewTreeObserver.removeOnGlobalLayoutListener(relayoutListener)
        viewTreeObserver.removeOnScrollChangedListener(scrollListener)
        super.onDetachedFromWindow()
    }

    private fun showStep() {
        if (index >= steps.size) {
            finish()
            return
        }
        val step = steps[index]
        val target = step.targetProvider()
        if (target == null || !target.isShown) {
            if (step.centered) {
                showCenteredStep(step)
            } else {
                index++
                showStep()
            }
            return
        }
        currentShape = step.shape
        allowTapThrough = step.allowTapThrough
        bindBubble(step)
        step.onShow?.invoke(target)
        target.requestRectangleOnScreen(Rect(0, 0, target.width, target.height), true)
        post { updateSpotlight(target) }
    }

    private fun showCenteredStep(step: CoachmarkStep) {
        hasSpotlight = false
        allowTapThrough = false
        bindBubble(step)
        positionBubble()
        invalidate()
    }

    private fun bindBubble(step: CoachmarkStep) {
        bubbleBinding.coachmarkTitle.setText(step.titleRes)
        bubbleBinding.coachmarkDesc.setText(step.descRes)
        bubbleBinding.coachmarkStep.text =
            context.getString(R.string.coachmark_step_indicator, index + 1, steps.size)
        bubbleBinding.coachmarkNext.setText(
            if (index == steps.size - 1) R.string.coachmark_done else R.string.coachmark_next
        )
    }

    private fun resolveCurrentSpotlight() {
        if (index >= steps.size) return
        steps[index].targetProvider()?.let { if (it.isShown) updateSpotlight(it) }
    }

    private fun updateSpotlight(target: View) {
        val targetLocation = IntArray(2)
        val selfLocation = IntArray(2)
        target.getLocationInWindow(targetLocation)
        getLocationInWindow(selfLocation)
        val padding = dp(8f)
        val left = targetLocation[0] - selfLocation[0] - padding
        val top = targetLocation[1] - selfLocation[1] - padding
        spotlight.set(left, top, left + target.width + padding * 2, top + target.height + padding * 2)
        hasSpotlight = true
        positionBubble()
        invalidate()
    }

    private fun positionBubble() {
        val bubble = bubbleBinding.root
        bubble.doOnLayout {
            if (!hasSpotlight) {
                bubble.updateLayoutParams<LayoutParams> {
                    gravity = Gravity.CENTER
                    topMargin = 0
                }
                return@doOnLayout
            }
            val below = spotlight.bottom + gap
            val topMargin = if (below + bubble.height <= height - paddingBottom) {
                below
            } else {
                (spotlight.top - gap - bubble.height).coerceAtLeast(gap)
            }
            bubble.updateLayoutParams<LayoutParams> {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                this.topMargin = topMargin.toInt()
            }
        }
    }

    private fun advance() {
        index++
        showStep()
    }

    private fun finish() {
        onFinish()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!hasSpotlight) {
            canvas.drawColor(scrimColor)
            return
        }
        val saved = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        canvas.drawColor(scrimColor)
        when (currentShape) {
            CoachmarkShape.ROUNDED_RECT -> {
                canvas.drawRoundRect(spotlight, cornerRadius, cornerRadius, clearPaint)
                canvas.restoreToCount(saved)
                canvas.drawRoundRect(spotlight, cornerRadius, cornerRadius, borderPaint)
            }
            CoachmarkShape.CIRCLE -> {
                val radius = maxOf(spotlight.width(), spotlight.height()) / 2f
                canvas.drawCircle(spotlight.centerX(), spotlight.centerY(), radius, clearPaint)
                canvas.restoreToCount(saved)
                canvas.drawCircle(spotlight.centerX(), spotlight.centerY(), radius, borderPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            if (allowTapThrough && hasSpotlight && spotlight.contains(event.x, event.y)) {
                return false
            }
            performClick()
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        advance()
        return true
    }

    private fun dp(value: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics
    )
}
