package app.kobuggi.hyuabot.ui.shuttle.via

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.databinding.DialogShuttleViaBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ShuttleViaSheetDialog(
    private val viaStops: List<ShuttleRealtimePageQuery.Vium>
): BottomSheetDialogFragment() {
    private val binding by lazy { DialogShuttleViaBinding.inflate(layoutInflater) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding.toolbar.setOnMenuItemClickListener {
            dismiss()
            true
        }
        return binding.root
    }
}
