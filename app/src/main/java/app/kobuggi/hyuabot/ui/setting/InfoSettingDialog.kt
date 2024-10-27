package app.kobuggi.hyuabot.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import app.kobuggi.hyuabot.databinding.DialogSettingDeveloperBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InfoSettingDialog : DialogFragment(){
    private lateinit var binding: DialogSettingDeveloperBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogSettingDeveloperBinding.inflate(inflater, container, false)
        return binding.root
    }
}
