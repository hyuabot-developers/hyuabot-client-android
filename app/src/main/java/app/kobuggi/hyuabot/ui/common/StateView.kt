package app.kobuggi.hyuabot.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.util.AnalyticsManager
import app.kobuggi.hyuabot.util.AnalyticsScreen
import java.time.format.DateTimeFormatter

class StateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {
    private val header: LinearLayout
    private val icon: ImageView
    private val title: TextView
    private val body: TextView
    private val action: MaterialButton
    private val impressionTracker = StateImpressionTracker()
    private var model: StateModel? = null
    private var sourceScreen: AnalyticsScreen? = null
    private var screenActive = false

    init {
        orientation = VERTICAL
        setPadding(paddingLeft, 32.dp, paddingRight, 32.dp)
        LayoutInflater.from(context).inflate(R.layout.view_state, this, true)
        header = findViewById(R.id.state_header)
        icon = findViewById(R.id.state_icon)
        title = findViewById(R.id.state_title)
        body = findViewById(R.id.state_body)
        action = findViewById(R.id.state_action)
        visibility = View.GONE
    }

    fun bind(model: StateModel, sourceScreen: AnalyticsScreen, onAction: (() -> Unit)? = null) {
        val content = StatePresentation.forKind(model.kind)
        require((content.actionRes == null) == (onAction == null)) {
            "${model.kind} requires ${if (content.actionRes == null) "no" else "an"} action callback"
        }
        this.model = model
        this.sourceScreen = sourceScreen
        val singleLine = model.kind == StateKind.ERROR
        title.setText(content.titleRes)
        title.visibility = if (singleLine) View.GONE else View.VISIBLE
        body.text = when {
            singleLine -> context.getString(content.titleRes)
            model.kind == StateKind.SUSPENDED && model.noticeDate != null -> {
                val locale = resources.configuration.locales[0]
                val date = model.noticeDate.format(DateTimeFormatter.ofPattern(context.getString(R.string.state_suspended_date), locale))
                context.getString(R.string.state_suspended_body, date)
            }
            else -> context.getString(content.bodyRes)
        }
        if (content.iconRes == null) icon.setImageDrawable(null) else icon.setImageResource(content.iconRes)
        icon.visibility = if (content.iconRes == null) View.GONE else View.VISIBLE
        action.visibility = if (content.actionRes == null) View.GONE else View.VISIBLE
        if (content.actionRes == null) action.text = null else action.setText(content.actionRes)
        action.setOnClickListener(if (onAction == null) null else View.OnClickListener { onAction() })
        header.contentDescription = if (singleLine) body.text else context.getString(R.string.state_header_content_description, title.text, body.text)
        visibility = View.VISIBLE
        updateImpression()
    }

    fun clear() {
        model = null
        sourceScreen = null
        action.setOnClickListener(null)
        action.visibility = View.GONE
        action.text = null
        icon.setImageDrawable(null)
        icon.visibility = View.GONE
        title.text = null
        body.text = null
        header.contentDescription = null
        visibility = View.GONE
        impressionTracker.update(false, null)
    }

    fun setScreenActive(active: Boolean) {
        screenActive = active
        updateImpression()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateImpression()
    }

    override fun onDetachedFromWindow() {
        impressionTracker.update(false, null)
        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        updateImpression()
    }

    private fun updateImpression() {
        val kind = impressionTracker.update(screenActive && isShown, model) ?: return
        sourceScreen?.let { AnalyticsManager.logEmptyStateShown(kind, it.id) }
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
