package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import app.kobuggi.hyuabot.databinding.DialogShuttleStopBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShuttleStopDialog @Inject constructor() : BottomSheetDialogFragment() {
    private val binding by lazy { DialogShuttleStopBinding.inflate(layoutInflater) }
    private val args : ShuttleStopDialogArgs by navArgs()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val stopID = args.stopID
        binding.apply {
            toolbar.apply {
                title = getString(stopID)
                setOnMenuItemClickListener {
                    dismiss()
                    true
                }
            }
        }
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
             behavior.isDraggable = false
        }
        return dialog
    }
}
