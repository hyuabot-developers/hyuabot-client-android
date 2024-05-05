package app.kobuggi.hyuabot.ui.bus.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentBusRealtimeTabBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BusTabSuwonFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentBusRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: BusRealtimeViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.apply {
            headerFirst.text = getString(R.string.bus_header_format, "707-1", getString(R.string.bus_stop_main_gate))
            headerSecond.text = getString(R.string.bus_header_format, "110", getString(R.string.bus_stop_entrance))
            headerThird.text = getString(R.string.bus_header_format, "7070", getString(R.string.bus_stop_entrance))
            headerFourth.text = getString(R.string.bus_header_format, "9090", getString(R.string.bus_stop_entrance))
        }
        return binding.root
    }
}
