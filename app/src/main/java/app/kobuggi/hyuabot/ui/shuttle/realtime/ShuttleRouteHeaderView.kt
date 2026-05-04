package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import app.kobuggi.hyuabot.R
import androidx.core.graphics.withTranslation

class ShuttleRouteHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val staticLayouts = mutableListOf<StaticLayout>()
    private val allStops = listOf(
        R.string.shuttle_tab_dormitory_out,
        R.string.shuttle_tab_shuttlecock_out,
        R.string.shuttle_tab_station,
        R.string.shuttle_tab_terminal,
        R.string.shuttle_type_jungang,
        R.string.shuttle_tab_shuttlecock_in,
        R.string.shuttle_type_dormitory
    )
    private var colWidth = 0f
    private var paddingWidth = 0f
    private val stopPositions = FloatArray(allStops.size)

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 30f
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        colWidth = width / allStops.size.toFloat()
        paddingWidth = colWidth / 2f
        textPaint.typeface = customTypeface
        textPaint.textSize = if (isEnglish()) 22f else 30f
        recalculateStopPositions()
        buildLayouts()
    }

    private fun recalculateStopPositions() {
        allStops.forEachIndexed { index, _ ->
            stopPositions[index] = paddingWidth + index * colWidth
        }
    }

    private val customTypeface by lazy {
        ResourcesCompat.getFont(context, R.font.godo)
    }

    private fun buildLayouts() {
        staticLayouts.clear()
        val maxWidth = colWidth.toInt().coerceAtLeast(1)
        allStops.forEach { stop ->
            val text = context.getString(stop).replace(" ", "\n")
            val layout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, maxWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1f)
                .setIncludePad(false)
                .build()
            staticLayouts.add(layout)
        }
    }

    private fun isEnglish(): Boolean {
        return context.resources.configuration.locales[0].language == "en"
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        allStops.forEachIndexed { index, _ ->
            val layout = staticLayouts.getOrNull(index) ?: return@forEachIndexed
            canvas.withTranslation(stopPositions[index], (height - layout.height) / 2f) {
                layout.draw(this)
            }
        }
    }
}
