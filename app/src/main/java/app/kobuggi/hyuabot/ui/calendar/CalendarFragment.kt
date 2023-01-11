package app.kobuggi.hyuabot.ui.calendar

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.databinding.FragmentLibraryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CalendarFragment : Fragment() {
    companion object {
        fun newInstance() = CalendarFragment()
    }
    private val viewModel: CalendarViewModel by viewModels()
    private val binding by lazy { FragmentLibraryBinding.inflate(layoutInflater) }
}