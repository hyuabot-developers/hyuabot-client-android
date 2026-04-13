package app.kobuggi.hyuabot.ui.subway.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentSubwayRealtimeTabBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SubwayTabTransferFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentSubwayRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: SubwayRealtimeViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val decoration = DividerItemDecoration(requireContext(), VERTICAL)
        val upAdapter = SubwayTransferListAdapter(requireContext(), "up")
        val downAdapter = SubwayTransferListAdapter(requireContext(), "down")
        binding.apply {
            headerUp.text = getString(R.string.subway_transfer_up)
            realtimeViewUp.apply {
                adapter = upAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(decoration)
            }
            entireTimetableUp.visibility = View.GONE
            headerDown.text = getString(R.string.subway_transfer_down)
            realtimeViewDown.apply {
                adapter = downAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(decoration)
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
            if (it.oidoBlue == null || it.oidoYellow == null || it.campusBlue == null || it.campusYellow == null) {
                return@observe
            }
            val upRealtimeWithoutTransfer = it.oidoYellow.arrival.first { arrival -> arrival.direction == "up" }.entries.filter {
                entry -> entry.terminal.stationID < "K251" && entry.isRealtime
            }.map {
                entry -> SubwayTransferItem(
                    take = entry,
                    transfer = null
                )
            }
            val upTimetableToTransfer = it.oidoBlue.arrival.first { arrival -> arrival.direction == "up" }.entries
            val upRealtimeWithTransfer = it.oidoYellow.arrival.first { arrival -> arrival.direction == "up" }.entries.filter {
                    entry -> entry.terminal.stationID >= "K251" && entry.isRealtime
            }.map {
                entry -> SubwayTransferItem(
                    take = entry,
                    transfer = upTimetableToTransfer.firstOrNull { transfer -> transfer.minutes > entry.minutes }
                )
            }
            val downRealtimeWithoutTransfer = it.campusYellow.arrival.first { arrival -> arrival.direction == "down" }.entries.filter {
                entry -> entry.terminal.stationID > "K258" && entry.isRealtime && entry.terminal.stationID.startsWith("K2")
            }.map {
                entry -> SubwayTransferItem(
                    take = entry,
                    transfer = null
                )
            }
            val downTimetableToTransfer = it.oidoYellow.arrival.first { arrival -> arrival.direction == "down" }.entries.filter {
                    entry -> entry.origin?.stationID == "K258"
            }
            val downRealtimeWithTransfer = it.campusBlue.arrival.first { arrival -> arrival.direction == "down" }.entries.filter {
                entry -> entry.terminal.stationID == "K456"
            }.map {
                entry ->
                val firstItemWithOutTransfer = downRealtimeWithoutTransfer.firstOrNull { realtime -> realtime.take.minutes > entry.minutes }
                SubwayTransferItem(
                    take = entry,
                    transfer = downTimetableToTransfer.firstOrNull { transfer ->
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
            val upEntries = (upRealtimeWithoutTransfer + upRealtimeWithTransfer).sortedBy { entry -> entry.take.minutes }
            val downEntries = (downRealtimeWithoutTransfer + downRealtimeWithTransfer).sortedBy { entry -> entry.take.minutes }
            if (upEntries.isEmpty()) {
                binding.noRealtimeDataUp.visibility = View.VISIBLE
            } else {
                binding.noRealtimeDataUp.visibility = View.GONE
            }
            if (downEntries.isEmpty()) {
                binding.noRealtimeDataDown.visibility = View.VISIBLE
            } else {
                binding.noRealtimeDataDown.visibility = View.GONE
            }
            upAdapter.updateData(upEntries)
            downAdapter.updateData(downEntries)
        }
        return binding.root
    }
}
