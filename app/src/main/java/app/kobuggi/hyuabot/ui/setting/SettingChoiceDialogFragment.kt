package app.kobuggi.hyuabot.ui.setting

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.fragment.app.DialogFragment

abstract class SettingChoiceDialogFragment : DialogFragment() {
    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}
