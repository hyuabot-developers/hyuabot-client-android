package app.kobuggi.hyuabot.ui.subway.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentSubwayRealtimeTabBinding
import app.kobuggi.hyuabot.service.safeNavigate
import app.kobuggi.hyuabot.ui.common.ListPresentation
import app.kobuggi.hyuabot.ui.common.StateKind
import app.kobuggi.hyuabot.ui.common.StateView
import app.kobuggi.hyuabot.ui.common.resolveListPresentation
import app.kobuggi.hyuabot.util.AnalyticsScreen
import app.kobuggi.hyuabot.util.AnalyticsManager
import app.kobuggi.hyuabot.util.ManualRefreshDomain
import app.kobuggi.hyuabot.util.TransportMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import app.kobuggi.hyuabot.util.disableViewStateSaving

@AndroidEntryPoint
class SubwayTabTransferFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentSubwayRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: SubwayRealtimeViewModel by viewModels({ requireParentFragment() })
    private var hasIncheonPrerequisites = false
    private var hasChojiPrerequisites = false
    private var incheonCount = 0
    private var chojiCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val incheonAdapter = SubwayTransferListAdapter(requireContext(), "down")
        val chojiAdapter = SubwayTransferListAdapter(requireContext(), "choji")
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.apply {
            headerUp.show(getString(R.string.subway_transfer_incheon_oido))
            realtimeViewUp.apply {
                adapter = incheonAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(decoration)
                configureTransferList()
            }
            entireTimetableUp.visibility = View.GONE
            headerDown.show(getString(R.string.subway_transfer_ilsan_bucheon))
            realtimeViewDown.apply {
                adapter = chojiAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                configureTransferList()
            }
            entireTimetableDown.visibility = View.GONE
            swipeRefreshLayout.setOnRefreshListener {
                AnalyticsManager.logManualRefresh(ManualRefreshDomain.SUBWAY)
                parentViewModel.fetchData()
            }
        }
        parentViewModel.combinedData.observe(viewLifecycleOwner) { data ->
            if (data == null) return@observe
            parentViewModel.reportSectionUpdatedAt(2, "up", maxRealtimeUpdatedAt(listOf(data.campusYellow, data.oidoYellow, data.campusBlue).map(::subwaySectionUpdatedAt)))
            parentViewModel.reportSectionUpdatedAt(2, "down", maxRealtimeUpdatedAt(listOf(data.campusBlue, data.campusYellow, data.chojiSeohae).map(::subwaySectionUpdatedAt)))
        }
        parentViewModel.requestState.observe(viewLifecycleOwner) { request ->
            if (!request.loading) binding.swipeRefreshLayout.isRefreshing = false
            renderListStates()
        }
        parentViewModel.combinedData.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            if (it.oidoYellow == null || it.campusBlue == null || it.campusYellow == null || it.chojiSeohae == null) {
                hasIncheonPrerequisites = it.oidoYellow != null && it.campusBlue != null && it.campusYellow != null
                hasChojiPrerequisites = hasIncheonPrerequisites && it.chojiSeohae != null
                incheonCount = 0
                chojiCount = 0
                incheonAdapter.updateData(emptyList())
                chojiAdapter.updateData(emptyList())
                renderListStates()
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
                val transfer = chojiSecondLegs.firstOrNull { secondLeg ->
                    secondLeg.minutes > firstLeg.minutes + CHOJI_TRAVEL_MINUTES + CHOJI_TRANSFER_BUFFER_MINUTES
                } ?: return@mapNotNull null
                SubwayTransferItem(
                    take = firstLeg,
                    transfer = transfer,
                    transferWaitMinutes = transfer.minutes - firstLeg.minutes - CHOJI_TRAVEL_MINUTES,
                )
            }
            val incheonEntries = (incheonDirect + incheonViaOido).sortedBy { entry -> entry.take.minutes }.take(MAX_TRANSFER_ITEMS_PER_SECTION)
            val chojiEntries = chojiTransfers.sortedBy { entry -> entry.take.minutes }.take(MAX_TRANSFER_ITEMS_PER_SECTION)
            hasIncheonPrerequisites = true
            hasChojiPrerequisites = true
            incheonCount = incheonEntries.size
            chojiCount = chojiEntries.size
            incheonAdapter.updateData(incheonEntries)
            chojiAdapter.updateData(chojiEntries)
            renderListStates()
        }
        return binding.root.also { disableViewStateSaving(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
    }

    override fun onResume() {
        super.onResume()
        binding.noRealtimeDataUp.setScreenActive(true)
        binding.noRealtimeDataDown.setScreenActive(true)
    }

    override fun onPause() {
        binding.noRealtimeDataUp.setScreenActive(false)
        binding.noRealtimeDataDown.setScreenActive(false)
        super.onPause()
    }

    private fun renderListStates() {
        renderSection(binding.noRealtimeDataUp, binding.realtimeViewUp, hasIncheonPrerequisites, incheonCount) {
            SubwayRealtimeFragmentDirections.actionSubwayRealtimeFragmentToSubwayTimetableFragment("K251", "down").also {
                findNavController().safeNavigate(it)
            }
        }
        renderSection(binding.noRealtimeDataDown, binding.realtimeViewDown, hasChojiPrerequisites, chojiCount) {
            SubwayRealtimeFragmentDirections.actionSubwayRealtimeFragmentToSubwayTimetableFragment("S26", "up").also {
                findNavController().safeNavigate(it)
            }
        }
    }

    private fun renderSection(stateView: StateView, rows: View, hasSource: Boolean, count: Int, openTimetable: () -> Unit) {
        val presentation = resolveListPresentation(
            parentViewModel.requestState.value ?: app.kobuggi.hyuabot.ui.common.RequestUiState(),
            hasSource, count, TransportMode.SUBWAY, java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Seoul")),
        )
        when (presentation) {
            ListPresentation.Loading -> {
                stateView.clear()
                rows.isVisible = false
            }
            ListPresentation.Content -> {
                stateView.clear()
                rows.isVisible = true
            }
            is ListPresentation.State -> {
                rows.isVisible = false
                val action = when (presentation.model.kind) {
                    StateKind.ERROR -> ({ parentViewModel.fetchData() })
                    StateKind.ENDED -> openTimetable
                    else -> null
                }
                if (action == null) stateView.bind(presentation.model, AnalyticsScreen.SUBWAY_REALTIME)
                else stateView.bind(presentation.model, AnalyticsScreen.SUBWAY_REALTIME, action)
            }
        }
    }

    companion object {
        private const val CHOJI_TRAVEL_MINUTES = 8
        private const val CHOJI_TRANSFER_BUFFER_MINUTES = 8
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
