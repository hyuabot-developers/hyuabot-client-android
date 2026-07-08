package app.kobuggi.hyuabot.ui.common

import android.app.AlertDialog
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import app.kobuggi.hyuabot.R

fun AlertDialog.applyGodoTypography() {
    val godo = ResourcesCompat.getFont(context, R.font.godo) ?: return
    window?.decorView?.applyGodoTypeface(godo)
}

private fun View.applyGodoTypeface(typeface: Typeface) {
    if (this is TextView) {
        setTypeface(typeface, this.typeface?.style ?: Typeface.NORMAL)
    }
    if (this is ViewGroup) {
        for (index in 0 until childCount) {
            getChildAt(index).applyGodoTypeface(typeface)
        }
    }
}
