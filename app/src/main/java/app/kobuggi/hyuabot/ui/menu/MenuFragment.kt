package app.kobuggi.hyuabot.ui.menu

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.databinding.FragmentMenuBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MenuFragment : Fragment() {
    companion object {
        fun newInstance() = MenuFragment()
    }
    private val viewModel: MenuViewModel by viewModels()
    private val binding by lazy { FragmentMenuBinding.inflate(layoutInflater) }
}