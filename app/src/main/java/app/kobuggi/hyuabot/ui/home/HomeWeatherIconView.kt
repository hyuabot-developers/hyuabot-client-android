package app.kobuggi.hyuabot.ui.home

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class HomeWeatherIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private enum class Condition {
        CLEAR,
        CLOUD,
        RAIN,
        SLEET,
        SNOW,
    }

    private data class Palette(
        val sunCore: Int,
        val sunEdge: Int,
        val sunRay: Int,
        val cloudBack: Int,
        val cloudFrontTop: Int,
        val cloudFrontBottom: Int,
        val rain: Int,
        val snow: Int,
        val shadow: Int,
    )

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 12_000L
        interpolator = LinearInterpolator()
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            animationPhase = it.animatedValue as Float
            invalidate()
        }
    }

    private var condition = Condition.CLOUD
    private var animationPhase = 0.125f

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    fun setWeatherCondition(value: String) {
        val nextCondition = when (value) {
            "CLEAR" -> Condition.CLEAR
            "RAIN" -> Condition.RAIN
            "SLEET" -> Condition.SLEET
            "SNOW" -> Condition.SNOW
            else -> Condition.CLOUD
        }
        if (condition == nextCondition) return
        condition = nextCondition
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateAnimationState()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        updateAnimationState()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val scale = min(width, height) / DESIGN_SIZE
        if (scale <= 0f) return

        val palette = palette()
        val floatingOffset = sin(animationPhase * TWO_PI).toFloat() * 1.35f
        val horizontalOffset = cos(animationPhase * TWO_PI).toFloat() * 0.8f

        canvas.save()
        canvas.translate((width - DESIGN_SIZE * scale) / 2f, (height - DESIGN_SIZE * scale) / 2f)
        canvas.scale(scale, scale)
        canvas.translate(horizontalOffset, floatingOffset)

        when (condition) {
            Condition.CLEAR -> drawSun(canvas, 53f, 50f, 22f, palette, large = true)
            Condition.CLOUD -> {
                drawSun(canvas, 35f, 35f, 13f, palette, large = false)
                drawCloud(canvas, palette)
            }
            Condition.RAIN -> {
                drawCloud(canvas, palette)
                drawRain(canvas, palette, includeSleet = false)
            }
            Condition.SLEET -> {
                drawCloud(canvas, palette)
                drawRain(canvas, palette, includeSleet = true)
            }
            Condition.SNOW -> {
                drawCloud(canvas, palette)
                drawSnow(canvas, palette)
            }
        }
        canvas.restore()
    }

    private fun drawSun(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        radius: Float,
        palette: Palette,
        large: Boolean,
    ) {
        val rayInner = radius + if (large) 7f else 4.5f
        val rayOuter = rayInner + if (large) 7f else 4f
        strokePaint.shader = null
        strokePaint.color = palette.sunRay
        strokePaint.strokeWidth = if (large) 4.2f else 3f

        canvas.save()
        canvas.rotate(animationPhase * 360f, centerX, centerY)
        repeat(8) { index ->
            val angle = Math.toRadians((index * 45).toDouble())
            canvas.drawLine(
                centerX + cos(angle).toFloat() * rayInner,
                centerY + sin(angle).toFloat() * rayInner,
                centerX + cos(angle).toFloat() * rayOuter,
                centerY + sin(angle).toFloat() * rayOuter,
                strokePaint,
            )
        }
        canvas.restore()

        fillPaint.shader = RadialGradient(
            centerX - radius * 0.3f,
            centerY - radius * 0.35f,
            radius * 1.25f,
            intArrayOf(palette.sunCore, palette.sunEdge),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(centerX, centerY, radius, fillPaint)
        fillPaint.shader = null

        fillPaint.color = Color.argb(92, 255, 255, 255)
        canvas.drawCircle(centerX - radius * 0.28f, centerY - radius * 0.32f, radius * 0.22f, fillPaint)
    }

    private fun drawCloud(canvas: Canvas, palette: Palette) {
        val backPath = cloudPath(offsetX = 5f, offsetY = -5f, scale = 0.9f)
        fillPaint.shader = null
        fillPaint.color = palette.cloudBack
        canvas.drawPath(backPath, fillPaint)

        val frontPath = cloudPath()
        canvas.save()
        canvas.translate(0f, 2.2f)
        fillPaint.color = palette.shadow
        canvas.drawPath(frontPath, fillPaint)
        canvas.restore()

        fillPaint.shader = LinearGradient(
            26f,
            36f,
            76f,
            76f,
            palette.cloudFrontTop,
            palette.cloudFrontBottom,
            Shader.TileMode.CLAMP,
        )
        canvas.drawPath(frontPath, fillPaint)
        fillPaint.shader = null
    }

    private fun cloudPath(
        offsetX: Float = 0f,
        offsetY: Float = 0f,
        scale: Float = 1f,
    ): Path {
        fun x(value: Float) = 50f + (value - 50f) * scale + offsetX
        fun y(value: Float) = 53f + (value - 53f) * scale + offsetY
        fun size(value: Float) = value * scale

        return Path().apply {
            addCircle(x(35f), y(54f), size(17f), Path.Direction.CW)
            addCircle(x(55f), y(43f), size(22f), Path.Direction.CW)
            addCircle(x(75f), y(56f), size(15f), Path.Direction.CW)
            addRoundRect(
                x(18f),
                y(52f),
                x(90f),
                y(75f),
                size(11.5f),
                size(11.5f),
                Path.Direction.CW,
            )
        }
    }

    private fun drawRain(canvas: Canvas, palette: Palette, includeSleet: Boolean) {
        strokePaint.shader = null
        strokePaint.strokeWidth = 3.6f
        val positions = floatArrayOf(31f, 46f, 61f, 76f)
        positions.forEachIndexed { index, x ->
            val progress = ((animationPhase * 10f) + index * 0.23f) % 1f
            val alpha = (sin(progress * PI).toFloat() * 255).toInt().coerceIn(0, 255)
            val y = 76f + progress * 15f
            if (includeSleet && index % 2 == 1) {
                fillPaint.color = withAlpha(palette.snow, alpha)
                canvas.drawCircle(x, y + 2f, 2.5f, fillPaint)
            } else {
                strokePaint.color = withAlpha(palette.rain, alpha)
                canvas.drawLine(x + 2f, y, x - 1.5f, y + 6.5f, strokePaint)
            }
        }
    }

    private fun drawSnow(canvas: Canvas, palette: Palette) {
        strokePaint.shader = null
        strokePaint.strokeWidth = 1.9f
        val positions = floatArrayOf(31f, 46f, 62f, 77f)
        positions.forEachIndexed { index, x ->
            val progress = ((animationPhase * 4f) + index * 0.24f) % 1f
            val alpha = (sin(progress * PI).toFloat() * 255).toInt().coerceIn(0, 255)
            val y = 76f + progress * 15f
            val radius = if (index % 2 == 0) 3.2f else 2.6f
            strokePaint.color = withAlpha(palette.snow, alpha)
            canvas.save()
            canvas.rotate(progress * 120f, x, y)
            canvas.drawLine(x - radius, y, x + radius, y, strokePaint)
            canvas.drawLine(x, y - radius, x, y + radius, strokePaint)
            canvas.restore()
        }
    }

    private fun palette(): Palette {
        val isDark = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
        return if (isDark) {
            Palette(
                sunCore = Color.rgb(255, 218, 99),
                sunEdge = Color.rgb(255, 161, 48),
                sunRay = Color.rgb(255, 184, 64),
                cloudBack = Color.rgb(80, 139, 194),
                cloudFrontTop = Color.rgb(207, 230, 249),
                cloudFrontBottom = Color.rgb(143, 180, 211),
                rain = Color.rgb(84, 201, 255),
                snow = Color.rgb(190, 239, 255),
                shadow = Color.argb(72, 0, 20, 36),
            )
        } else {
            Palette(
                sunCore = Color.rgb(255, 221, 101),
                sunEdge = Color.rgb(255, 161, 35),
                sunRay = Color.rgb(255, 174, 42),
                cloudBack = Color.rgb(102, 174, 239),
                cloudFrontTop = Color.rgb(241, 248, 255),
                cloudFrontBottom = Color.rgb(180, 213, 240),
                rain = Color.rgb(28, 168, 232),
                snow = Color.rgb(66, 180, 229),
                shadow = Color.argb(30, 12, 52, 82),
            )
        }
    }

    private fun updateAnimationState() {
        if (!isAttachedToWindow || visibility != VISIBLE || !ValueAnimator.areAnimatorsEnabled()) {
            animator.cancel()
            animationPhase = 0.125f
            invalidate()
            return
        }
        if (!animator.isStarted) animator.start()
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))

    private companion object {
        private const val DESIGN_SIZE = 100f
        private const val TWO_PI = (PI * 2)
    }
}
