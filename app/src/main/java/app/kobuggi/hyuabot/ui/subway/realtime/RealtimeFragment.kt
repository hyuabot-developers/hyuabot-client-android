package app.kobuggi.hyuabot.ui.subway.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.component.card.subway.RealtimeRouteCardAdapter
import app.kobuggi.hyuabot.databinding.FragmentSubwayRealtimeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RealtimeFragment : Fragment() {
    companion object {
        fun newInstance() = RealtimeFragment()
    }
    private val viewModel: RealtimeViewModel by viewModels()
    private val binding by lazy { FragmentSubwayRealtimeBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val adapter = RealtimeRouteCardAdapter(
            requireContext(),
            0,
            { index -> viewModel.setBookmark(index) },
            { stationID, heading -> viewModel.openTimetable(stationID, heading) }
        )
        viewModel.fetchData()
        viewModel.getBookmark()
        viewModel.start()
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.subwayArrivalProgress.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.bookmarkIndex.observe(viewLifecycleOwner) {
            adapter.updateBookmarkIndex(it)
            binding.subwayRealtimeRecyclerView.scrollToPosition(it)
        }
        viewModel.k251ArrivalList.observe(viewLifecycleOwner) {
            adapter.updateK251Data(it.realtime, it.timetable)
        }
        viewModel.k449ArrivalList.observe(viewLifecycleOwner) {
            adapter.updateK449Data(it.realtime, it.timetable)
        }
        viewModel.k258ArrivalList.observe(viewLifecycleOwner) {
            adapter.updateK258Data(it.realtime, it.timetable)
        }
        viewModel.k456ArrivalList.observe(viewLifecycleOwner) {
            adapter.updateK456Data(it.realtime, it.timetable)
        }
        viewModel.openTimetableEvent.observe(viewLifecycleOwner) {
            if (it.stationID.isNotEmpty()) {
                val action = RealtimeFragmentDirections.openSubwayTimetable(it.stationID, it.heading)
                viewModel.openTimetable("", "")
                findNavController().navigate(action)
            }
        }
        binding.subwayRealtimeRecyclerView.adapter = adapter
        binding.subwayRealtimeRecyclerView.itemAnimator = null
        binding.refreshLayout.setOnRefreshListener {
            viewModel.fetchData()
            binding.refreshLayout.isRefreshing = false
        }
        return binding.root
    }
}