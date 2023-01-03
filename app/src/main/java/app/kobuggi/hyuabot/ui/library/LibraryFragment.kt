package app.kobuggi.hyuabot.ui.library

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.databinding.FragmentLibraryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LibraryFragment : Fragment() {
    companion object {
        fun newInstance() = LibraryFragment()
    }
    private val viewModel: LibraryViewModel by viewModels()
    private val binding by lazy { FragmentLibraryBinding.inflate(layoutInflater) }
}