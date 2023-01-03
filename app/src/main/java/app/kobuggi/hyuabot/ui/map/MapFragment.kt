package app.kobuggi.hyuabot.ui.map

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.databinding.FragmentMapBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapFragment : Fragment() {
    companion object {
        fun newInstance() = MapFragment()
    }
    private val viewModel: MapViewModel by viewModels()
    private val binding by lazy { FragmentMapBinding.inflate(layoutInflater) }
}