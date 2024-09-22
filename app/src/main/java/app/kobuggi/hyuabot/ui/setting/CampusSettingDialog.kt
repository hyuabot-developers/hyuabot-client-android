package app.kobuggi.hyuabot.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.databinding.DialogSettingCampusBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CampusSettingDialog @Inject constructor() : DialogFragment() {
    private val binding by lazy { DialogSettingCampusBinding.inflate(layoutInflater) }
    private val vm by viewModels<CampusSettingDialogViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.apply {
            campusSeoul.setOnClickListener {
                vm.setCampusID(1)
                dismiss()
            }
            campusErica.setOnClickListener {
                vm.setCampusID(2)
                dismiss()
            }
        }
        return binding.root
    }
}
