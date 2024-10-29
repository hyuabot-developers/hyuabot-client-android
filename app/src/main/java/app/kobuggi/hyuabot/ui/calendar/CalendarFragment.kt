package app.kobuggi.hyuabot.ui.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.databinding.FragmentCalendarBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CalendarFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentCalendarBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<CalendarViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.apply {
            fetchCalendarVersion()
            events.observe(viewLifecycleOwner) {
                Log.d("CalendarFragment", it.toString())
            }
        }
        return binding.root
    }
}
