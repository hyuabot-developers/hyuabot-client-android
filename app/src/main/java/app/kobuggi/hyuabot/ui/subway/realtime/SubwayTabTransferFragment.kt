package app.kobuggi.hyuabot.ui.subway.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentSubwayRealtimeTabBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import app.kobuggi.hyuabot.util.disableViewStateSaving

@AndroidEntryPoint
class SubwayTabTransferFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentSubwayRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: SubwayRealtimeViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val incheonAdapter = SubwayTransferListAdapter(requireContext(), "down")
        val chojiAdapter = SubwayTransferListAdapter(requireContext(), "choji")
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.apply {
            headerUp.text = getString(R.string.subway_transfer_incheon_oido)
            realtimeViewUp.apply {
                adapter = incheonAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(decoration)
                configureTransferList()
            }
            entireTimetableUp.visibility = View.GONE
            headerDown.text = getString(R.string.subway_transfer_ilsan_bucheon)
            realtimeViewDown.apply {
                adapter = chojiAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                configureTransferList()
            }
            entireTimetableDown.visibility = View.GONE
            swipeRefreshLayout.setOnRefreshListener {
                parentViewModel.fetchData()
            }
        }
        parentViewModel.isLoading.observe(viewLifecycleOwner) {
            if (!it) binding.swipeRefreshLayout.isRefreshing = false
        }
        parentViewModel.combinedData.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            if (it.oidoYellow == null || it.campusBlue == null || it.campusYellow == null || it.chojiSeohae == null) {
                return@observe
            }
            val incheonDirect = (it.campusYellow.arrival.firstOrNull { arrival -> arrival.direction == "down" }?.entries ?: emptyList()).filter {
                entry -> entry.terminal.stationID > "K258" && entry.isRealtime && entry.terminal.stationID.startsWith("K2")
            }.map {
                entry -> SubwayTransferItem(
                    take = entry,
                    transfer = null
                )
            }
            val oidoToIncheon = (it.oidoYellow.arrival.firstOrNull { arrival -> arrival.direction == "down" }?.entries ?: emptyList()).filter {
                    entry -> entry.origin?.stationID == "K258" && entry.terminal.stationID == "K272"
            }
            val incheonViaOido = (it.campusBlue.arrival.firstOrNull { arrival -> arrival.direction == "down" }?.entries ?: emptyList()).filter {
                entry -> entry.terminal.stationID == "K456"
            }.map {
                entry ->
                val firstItemWithOutTransfer = incheonDirect.firstOrNull { realtime -> realtime.take.minutes > entry.minutes }
                SubwayTransferItem(
                    take = entry,
                    transfer = oidoToIncheon.firstOrNull { transfer ->
                        if (firstItemWithOutTransfer == null) {
                            transfer.minutes > entry.minutes + 20
                        } else {
                            transfer.minutes > entry.minutes + 20 && transfer.minutes < firstItemWithOutTransfer.take.minutes + 20
                        }
                    }
                )
            }.filter {
                entry -> entry.transfer != null
            }
            val chojiFirstLegs = ((it.campusBlue.arrival.firstOrNull { arrival -> arrival.direction == "down" }?.entries ?: emptyList()).filter {
                entry -> entry.terminal.stationID >= "K452" && entry.terminal.stationID.startsWith("K4")
            } + (it.campusYellow.arrival.firstOrNull { arrival -> arrival.direction == "down" }?.entries ?: emptyList()).filter {
                entry -> entry.terminal.stationID >= "K254" && entry.terminal.stationID.startsWith("K2")
            }).sortedBy { entry -> entry.minutes }
            val chojiSecondLegs = (it.chojiSeohae.arrival.firstOrNull { arrival -> arrival.direction == "up" }?.entries ?: emptyList()).filter {
                entry -> entry.terminal.stationID <= "S16" && entry.terminal.stationID.startsWith("S")
            }
            val chojiTransfers = chojiFirstLegs.mapNotNull { firstLeg ->
                SubwayTransferItem(
                    take = firstLeg,
                    transfer = chojiSecondLegs.firstOrNull { secondLeg ->
                        secondLeg.minutes > firstLeg.minutes + CHOJI_TRANSFER_BUFFER_MINUTES
                    } ?: return@mapNotNull null
                )
            }
            val incheonEntries = (incheonDirect + incheonViaOido).sortedBy { entry -> entry.take.minutes }.take(MAX_TRANSFER_ITEMS_PER_SECTION)
            val chojiEntries = chojiTransfers.sortedBy { entry -> entry.take.minutes }.take(MAX_TRANSFER_ITEMS_PER_SECTION)
            if (incheonEntries.isEmpty()) {
                binding.noRealtimeDataUp.visibility = View.VISIBLE
            } else {
                binding.noRealtimeDataUp.visibility = View.GONE
            }
            if (chojiEntries.isEmpty()) {
                binding.noRealtimeDataDown.visibility = View.VISIBLE
            } else {
                binding.noRealtimeDataDown.visibility = View.GONE
            }
            incheonAdapter.updateData(incheonEntries)
            chojiAdapter.updateData(chojiEntries)
        }
        return binding.root.also { disableViewStateSaving(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
    }

    companion object {
        private const val CHOJI_TRANSFER_BUFFER_MINUTES = 16
        private const val MAX_TRANSFER_ITEMS_PER_SECTION = 2
    }

    private fun RecyclerView.configureTransferList() {
        setPadding(0, 0, 0, 0)
        clipToPadding = false
        (layoutParams as ViewGroup.MarginLayoutParams).apply {
            marginStart = 0
            marginEnd = 0
        }
    }
}
