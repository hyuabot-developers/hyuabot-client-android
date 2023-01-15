package app.kobuggi.hyuabot.ui.menu.campus

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.databinding.DialogCampusBinding
import app.kobuggi.hyuabot.ui.menu.MenuFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CampusDialog : DialogFragment(){
    private val binding by lazy { DialogCampusBinding.inflate(layoutInflater) }
    private val vm by viewModels<CampusDialogViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.vm = vm
        binding.lifecycleOwner = viewLifecycleOwner

        val sharedPreferences = requireActivity().getSharedPreferences("hyuabot", 0)
        vm.campusCode.observe(viewLifecycleOwner) {
            sharedPreferences.edit().apply {
                putInt("campus", it)
                apply()
            }
            dismiss()
        }
        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (parentFragment is MenuFragment){
            (parentFragment as MenuFragment).onDismiss(dialog)
        }
    }
}