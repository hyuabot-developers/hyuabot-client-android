package app.kobuggi.hyuabot.ui.subway.realtime
import app.kobuggi.hyuabot.util.AnalyticsManager
import app.kobuggi.hyuabot.util.AnalyticsItem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentSubwayRealtimeTabBinding
import app.kobuggi.hyuabot.service.safeNavigate
import app.kobuggi.hyuabot.ui.common.ListPresentation
import app.kobuggi.hyuabot.ui.common.StateKind
import app.kobuggi.hyuabot.ui.common.StateModel
import app.kobuggi.hyuabot.ui.common.StateView
import app.kobuggi.hyuabot.ui.common.resolveListPresentation
import app.kobuggi.hyuabot.util.AnalyticsScreen
import app.kobuggi.hyuabot.util.TransportMode
import java.time.ZoneId
import java.time.ZonedDateTime
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import app.kobuggi.hyuabot.util.disableViewStateSaving

@AndroidEntryPoint
class SubwayTabBlueFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentSubwayRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: SubwayRealtimeViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val decoration = DividerItemDecoration(requireContext(), VERTICAL)
        val upAdapter = SubwayRealtimeListAdapter(requireContext(), R.color.subway_line4)
        val downAdapter = SubwayRealtimeListAdapter(requireContext(), R.color.subway_line4)
        binding.apply {
            headerUp.show(getString(R.string.subway_blue_up))
            realtimeViewUp.apply {
                adapter = upAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(decoration)
            }
            entireTimetableUp.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SUBWAY_SHOW_ENTIRE_TIMETABLE)
                SubwayRealtimeFragmentDirections.actionSubwayRealtimeFragmentToSubwayTimetableFragment("K449", "up").also {
                    findNavController().safeNavigate(it)
                }
            }
            headerDown.show(getString(R.string.subway_blue_down))
            realtimeViewDown.apply {
                adapter = downAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(decoration)
            }
            entireTimetableDown.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SUBWAY_SHOW_ENTIRE_TIMETABLE)
                SubwayRealtimeFragmentDirections.actionSubwayRealtimeFragmentToSubwayTimetableFragment("K449", "down").also {
                    findNavController().safeNavigate(it)
                }
            }
            swipeRefreshLayout.setOnRefreshListener {
                parentViewModel.fetchData()
            }
        }
        parentViewModel.campusBlue.observe(viewLifecycleOwner) { subway ->
            val updatedAt = subwaySectionUpdatedAt(subway)
            parentViewModel.reportSectionUpdatedAt(0, "up", updatedAt)
            parentViewModel.reportSectionUpdatedAt(0, "down", updatedAt)
        }
        parentViewModel.requestState.observe(viewLifecycleOwner) { request ->
            if (!request.loading) binding.swipeRefreshLayout.isRefreshing = false
            renderListStates()
        }
        parentViewModel.campusBlue.observe(viewLifecycleOwner) {
            val upEntries = it?.arrival?.firstOrNull { arrival -> arrival.direction == "up" }?.entries ?: emptyList()
            val downEntries = it?.arrival?.firstOrNull { arrival -> arrival.direction == "down" }?.entries ?: emptyList()
            upAdapter.updateData(upEntries)
            downAdapter.updateData(downEntries)
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
        val subway = parentViewModel.campusBlue.value
        val up = subway?.arrival?.firstOrNull { it.direction == "up" }
        val down = subway?.arrival?.firstOrNull { it.direction == "down" }
        renderSection(binding.noRealtimeDataUp, binding.realtimeViewUp, binding.entireTimetableUp, up != null, up?.entries?.size ?: 0)
        renderSection(binding.noRealtimeDataDown, binding.realtimeViewDown, binding.entireTimetableDown, down != null, down?.entries?.size ?: 0)
    }

    private fun renderSection(stateView: StateView, rows: View, timetable: View, hasSource: Boolean, count: Int) {
        val presentation = resolveListPresentation(
            parentViewModel.requestState.value ?: app.kobuggi.hyuabot.ui.common.RequestUiState(),
            hasSource, count, TransportMode.SUBWAY, ZonedDateTime.now(ZoneId.of("Asia/Seoul")),
        )
        when (presentation) {
            ListPresentation.Loading -> {
                stateView.clear()
                rows.isVisible = false
                timetable.isVisible = false
            }
            ListPresentation.Content -> {
                stateView.clear()
                rows.isVisible = true
                timetable.isVisible = true
            }
            is ListPresentation.State -> {
                rows.isVisible = false
                timetable.isVisible = false
                val action = when (presentation.model.kind) {
                    StateKind.ERROR -> ({ parentViewModel.fetchData() })
                    StateKind.ENDED -> ({ timetable.performClick(); Unit })
                    else -> null
                }
                if (action == null) stateView.bind(presentation.model, AnalyticsScreen.SUBWAY_REALTIME)
                else stateView.bind(presentation.model, AnalyticsScreen.SUBWAY_REALTIME, action)
            }
        }
    }
}
