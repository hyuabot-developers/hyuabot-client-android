package app.kobuggi.hyuabot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.databinding.FragmentShuttleStopBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShuttleStopTab : Fragment() {
    private val binding by lazy { FragmentShuttleStopBinding.inflate(layoutInflater) }
    private val vm by viewModels<MainViewModel>({ requireActivity() })

    fun newInstance(position: Int): ShuttleStopTab {
        val bundle = Bundle(1)
        val fragment = ShuttleStopTab()
        bundle.putInt("position", position)
        fragment.arguments = bundle
        return fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.shuttleStop.text = getString(ShuttleData.stopList[arguments?.getInt("position") ?: 0])
        val dataSource = when(arguments?.getInt("position") ?: 0){
            0 -> vm.dormitoryArrival
            1 -> vm.shuttlecockOutArrival
            2 -> vm.stationArrival
            3 -> vm.terminalArrival
            4 -> vm.jungangArrival
            5 -> vm.shuttlecockInArrival
            else -> vm.dormitoryArrival
        }

        val adapter = ShuttleArrivalAdapter(requireContext(), ShuttleData.stopList[arguments?.getInt("position") ?: 0], dataSource.value ?: listOf(-1, -1, -1))
        binding.arrivalList.adapter = adapter
        binding.arrivalList.layoutManager = LinearLayoutManager(requireContext())
        binding.arrivalList.itemAnimator = null
        dataSource.observe(viewLifecycleOwner) {
            adapter.updateList(it)
        }

        return binding.root
    }
}