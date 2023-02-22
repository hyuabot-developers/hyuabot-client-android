package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.MainActivity
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.component.card.shuttle.RealtimeStopCardAdapter
import app.kobuggi.hyuabot.component.card.shuttle.SubCardAdapter
import app.kobuggi.hyuabot.databinding.FragmentShuttleRealtimeBinding
import com.google.firebase.analytics.FirebaseAnalytics
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
        val shuttleSubCardAdapter = SubCardAdapter(requireContext())
        binding.shuttleRealtimeRecyclerView.adapter = shuttleRealtimeStopCardAdapter
        binding.shuttleRealtimeRecyclerView.itemAnimator = null
        binding.shuttleSubCardRecyclerView.adapter = shuttleSubCardAdapter
        binding.shuttleSubCardRecyclerView.itemAnimator = null
        viewModel.shuttleArrivalList.observe(viewLifecycleOwner) {
            shuttleRealtimeStopCardAdapter.updateStopList(it)
            shuttleSubCardAdapter.updateStopList(it)
        }
        viewModel.k251ArrivalList.observe(viewLifecycleOwner) {
            shuttleSubCardAdapter.updateSubwayArrival(it.realtime)
        }
        viewModel.suwonArrivalList.observe(viewLifecycleOwner) {
            shuttleSubCardAdapter.updateBusArrivalToSuwon(it)
        }
        viewModel.sangnoksuArrivalList.observe(viewLifecycleOwner) {
            shuttleSubCardAdapter.updateBusArrivalFromSangnoksu(it)
        }
        viewModel.fromGwangmyeongArrivalList.observe(viewLifecycleOwner) {
            shuttleSubCardAdapter.updateBusArrivalFromGwangmyeong(it)
        }
        viewModel.toGwangmyeongArrivalList.observe(viewLifecycleOwner) {
            shuttleSubCardAdapter.updateBusArrivalToGwangmyeong(it)
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
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            if (it) {
                val toast = Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT)
                toast.show()
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
        if (activity is MainActivity) {
            (activity as MainActivity).getAnalytics().logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, "셔틀 실시간 도착 정보")
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, "ShuttleRealtimeFragment")
            })
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        viewModel.openShuttleStopInformation(-1)
    }
}