package app.kobuggi.hyuabot.ui.bus.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import app.kobuggi.hyuabot.databinding.FragmentBusRealtimeBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BusTabCityFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentBusRealtimeBinding.inflate(layoutInflater) }
    private val parentViewModel: BusRealtimeViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val decoration = DividerItemDecoration(requireContext(), VERTICAL)
        return binding.root
    }
}
