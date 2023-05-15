package app.kobuggi.hyuabot.ui.bus.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.MainActivity
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.component.card.bus.RealtimeRouteCardAdapter
import app.kobuggi.hyuabot.databinding.FragmentBusRealtimeBinding
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RealtimeFragment : Fragment() {
    companion object {
        fun newInstance() = RealtimeFragment()
    }
    private val viewModel: RealtimeViewModel by viewModels()
    private val binding by lazy { FragmentBusRealtimeBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val adapter = RealtimeRouteCardAdapter(
            requireContext(),
            0,
            { index -> viewModel.setBookmark(index) },
            { routeID, routeName, startStop -> viewModel.openTimetable(routeID, routeName, startStop) }
        )
        viewModel.fetchData()
        viewModel.getBookmark()
        viewModel.start()
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.busArrivalProgress.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.bookmarkIndex.observe(viewLifecycleOwner) {
            adapter.updateBookmarkIndex(it)
            binding.busRealtimeRecyclerView.scrollToPosition(it)
        }
        viewModel.conventionCenterBusArrivalList.observe(viewLifecycleOwner) {
            adapter.updateConventionCenterData(it)
        }
        viewModel.mainGateBusArrivalList.observe(viewLifecycleOwner) {
            adapter.updateMainGateData(it)
        }
        viewModel.sangnoksuArrivalList.observe(viewLifecycleOwner) {
            adapter.updateSangnoksuData(it)
        }
        viewModel.seonganHighSchoolArrivalList.observe(viewLifecycleOwner) {
            adapter.updateSeonganHighSchoolData(it)
        }
        viewModel.timetableEvent.observe(viewLifecycleOwner) {
            if (it.routeID > 0) {
                val action = RealtimeFragmentDirections.openBusTimetable(it.routeID, it.routeName, it.stopID)
                viewModel.openTimetable(-1, "", -1)
                findNavController().navigate(action)
            }
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            if (it) {
                val toast = Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT)
                toast.show()
            }
        }
        binding.busRealtimeRecyclerView.adapter = adapter
        binding.busRealtimeRecyclerView.itemAnimator = null
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.start()
        if (activity is MainActivity) {
            (activity as MainActivity).getAnalytics().logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, "버스 실시간 도착 정보")
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, "BusRealtimeFragment")
            })
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
    }
}