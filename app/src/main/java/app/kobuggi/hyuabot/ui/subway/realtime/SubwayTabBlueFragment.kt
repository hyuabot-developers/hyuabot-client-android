package app.kobuggi.hyuabot.ui.subway.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentSubwayRealtimeTabBinding
import app.kobuggi.hyuabot.service.safeNavigate
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class SubwayTabBlueFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentSubwayRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: SubwayRealtimeViewModel by viewModels({ requireParentFragment() })
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val decoration = DividerItemDecoration(requireContext(), VERTICAL)
        val upAdapter = SubwayRealtimeListAdapter(requireContext(), listOf(), null, listOf(), null)
        val downAdapter = SubwayRealtimeListAdapter(requireContext(), null, listOf(), null, listOf())
        binding.apply {
            headerUp.text = getString(R.string.subway_blue_up)
            realtimeViewUp.apply {
                adapter = upAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(decoration)
            }
            entireTimetableUp.setOnClickListener {
                SubwayRealtimeFragmentDirections.actionSubwayRealtimeFragmentToSubwayTimetableFragment("K449", "up").also {
                    findNavController().safeNavigate(it)
                }
            }
            headerDown.text = getString(R.string.subway_blue_down)
            realtimeViewDown.apply {
                adapter = downAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(decoration)
            }
            entireTimetableDown.setOnClickListener {
                SubwayRealtimeFragmentDirections.actionSubwayRealtimeFragmentToSubwayTimetableFragment("K449", "down").also {
                    findNavController().safeNavigate(it)
                }
            }
            swipeRefreshLayout.setOnRefreshListener {
                parentViewModel.fetchData()
            }
        }
        parentViewModel.isLoading.observe(viewLifecycleOwner) {
            if (!it) binding.swipeRefreshLayout.isRefreshing = false
        }
        parentViewModel.K449.observe(viewLifecycleOwner) {
            upAdapter.updateData(
                it?.realtime?.up ?: listOf(),
                null,
                it?.timetable?.up?.filter { timetableItem ->
                    if (it.realtime.up.isEmpty()) {
                        return@filter true
                    }
                    val departureTime = LocalTime.parse(timetableItem.time, dateTimeFormatter)
                    departureTime.isAfter(LocalTime.now().plusMinutes(it.realtime.up.last().time.toLong()))
                } ?: listOf(),
                null
            )
            downAdapter.updateData(
                null,
                it?.realtime?.down ?: listOf(),
                null,
                it?.timetable?.down?.filter { timetableItem ->
                    if (it.realtime.down.isEmpty()) {
                        return@filter true
                    }
                    val departureTime = LocalTime.parse(timetableItem.time, dateTimeFormatter)
                    departureTime.isAfter(LocalTime.now().plusMinutes(it.realtime.down.last().time.toLong()))
                } ?: listOf()
            )
            if (it?.realtime?.up.isNullOrEmpty() && it?.timetable?.up.isNullOrEmpty()) {
                binding.noRealtimeDataUp.visibility = View.VISIBLE
            } else {
                binding.noRealtimeDataUp.visibility = View.GONE
            }
            if (it?.realtime?.down.isNullOrEmpty() && it?.timetable?.down.isNullOrEmpty()) {
                binding.noRealtimeDataDown.visibility = View.VISIBLE
            } else {
                binding.noRealtimeDataDown.visibility = View.GONE
            }
        }
        return binding.root
    }
}
