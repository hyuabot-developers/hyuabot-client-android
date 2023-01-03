package app.kobuggi.hyuabot.ui.cafeteria

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.databinding.FragmentCafeteriaBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CafeteriaFragment : Fragment() {
    companion object {
        fun newInstance() = CafeteriaFragment()
    }
    private val viewModel: CafeteriaViewModel by viewModels()
    private val binding by lazy { FragmentCafeteriaBinding.inflate(layoutInflater) }
}