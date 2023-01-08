package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.component.card.shuttle.RealtimeStopCardAdapter
import app.kobuggi.hyuabot.databinding.FragmentShuttleRealtimeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RealtimeFragment : Fragment(), DialogInterface.OnDismissListener {
    companion object {
        fun newInstance() = RealtimeFragment()
    }
    private val viewModel: RealtimeViewModel by viewModels()
    private val binding by lazy { FragmentShuttleRealtimeBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.fetchData()
        viewModel.getBookmark()
        viewModel.start()
        val shuttleRealtimeStopCardAdapter = RealtimeStopCardAdapter(
            requireContext(),
            0,
            { index -> viewModel.setBookmark(index) },
            { index -> viewModel.openShuttleStopInformation(index) },
            { stop, destination -> viewModel.openTimetable(stop, destination) }
        )
        binding.shuttleRealtimeRecyclerView.adapter = shuttleRealtimeStopCardAdapter
        binding.shuttleRealtimeRecyclerView.itemAnimator = null
        binding.refreshLayout.setOnRefreshListener {
            viewModel.fetchData()
            binding.refreshLayout.isRefreshing = false
        }
        viewModel.shuttleArrivalList.observe(viewLifecycleOwner) {
            shuttleRealtimeStopCardAdapter.updateStopList(it)
        }
        viewModel.k251ArrivalList.observe(viewLifecycleOwner) {
            shuttleRealtimeStopCardAdapter.updateSubwayArrival(it.realtime)
        }
        viewModel.suwonArrivalList.observe(viewLifecycleOwner) {
            shuttleRealtimeStopCardAdapter.updateBusArrivalToSuwon(it)
        }
        viewModel.sangnoksuArrivalList.observe(viewLifecycleOwner) {
            shuttleRealtimeStopCardAdapter.updateBusArrivalFromSangnoksu(it)
        }
        viewModel.fromGwangmyeongArrivalList.observe(viewLifecycleOwner) {
            shuttleRealtimeStopCardAdapter.updateBusArrivalFromGwangmyeong(it)
        }
        viewModel.toGwangmyeongArrivalList.observe(viewLifecycleOwner) {
            shuttleRealtimeStopCardAdapter.updateBusArrivalToGwangmyeong(it)
        }
        viewModel.bookmarkIndex.observe(viewLifecycleOwner) {
            if (it >= 0) {
                binding.shuttleRealtimeRecyclerView.smoothScrollToPosition(it)
                shuttleRealtimeStopCardAdapter.updateBookmarkIndex(it)
            }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it) {
                binding.shuttleArrivalProgress.visibility = View.VISIBLE
            } else {
                binding.shuttleArrivalProgress.visibility = View.GONE
            }
        }
        viewModel.shuttleStopInformationEvent.observe(viewLifecycleOwner) {
            if (it >= 0) {
                val dialog = ShuttleStopInformationDialog()
                dialog.show(childFragmentManager, "shuttleStopInformation")
            }
        }
        viewModel.openTimetableEvent.observe(viewLifecycleOwner) {
            if (it.stopID > 0) {
                val action = RealtimeFragmentDirections.openShuttleTimetable(it.stopID, it.destination)
                viewModel.openTimetable(-1, -1)
                findNavController().navigate(action)
            }
        }
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
    }

    override fun onResume() {
        super.onResume()
        viewModel.start()
        viewModel.openTimetable(-1, -1)
        viewModel.openShuttleStopInformation(-1)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        viewModel.openShuttleStopInformation(-1)
    }
}