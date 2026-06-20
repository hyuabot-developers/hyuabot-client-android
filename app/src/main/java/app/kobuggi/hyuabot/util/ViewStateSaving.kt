package app.kobuggi.hyuabot.util

import android.view.View
import android.view.ViewGroup

fun disableViewStateSaving(view: View) {
    view.isSaveEnabled = false
    view.isSaveFromParentEnabled = false
    if (view is ViewGroup) {
        for (index in 0 until view.childCount) {
            disableViewStateSaving(view.getChildAt(index))
        }
    }
}
