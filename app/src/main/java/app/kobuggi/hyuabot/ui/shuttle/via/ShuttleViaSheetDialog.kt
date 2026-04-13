package app.kobuggi.hyuabot.ui.shuttle.via

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.ShuttleTimetablePageQuery
import app.kobuggi.hyuabot.databinding.DialogShuttleViaBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ShuttleViaSheetDialog(
    private val stopsOfTimetableByOrder: List<ShuttleRealtimePageQuery.Stop1> = emptyList(),
    private val stopsOfTimetableByDestination: List<ShuttleRealtimePageQuery.Stop2> = emptyList(),
    private val stopsOfEntireTimetable: List<ShuttleTimetablePageQuery.Stop1> = emptyList(),
): BottomSheetDialogFragment() {
    private val binding by lazy { DialogShuttleViaBinding.inflate(layoutInflater) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val decoration = DividerItemDecoration(requireContext(), VERTICAL)
        binding.apply {
            toolbar.setOnMenuItemClickListener {
                dismiss()
                true
            }
            viaList.apply {
                adapter = ShuttleViaListAdapter(requireContext(), stopsOfTimetableByOrder, stopsOfTimetableByDestination, stopsOfEntireTimetable)
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(decoration)
            }
        }
        return binding.root
    }
}
