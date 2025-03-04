package app.kobuggi.hyuabot.ui.shuttle.via

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.databinding.SheetShuttleViaBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ShuttleViaSheetDialog(
    private val viaStops: List<ShuttleRealtimePageQuery.Vium>
): BottomSheetDialogFragment() {
    private val binding by lazy { SheetShuttleViaBinding.inflate(layoutInflater) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }
}
