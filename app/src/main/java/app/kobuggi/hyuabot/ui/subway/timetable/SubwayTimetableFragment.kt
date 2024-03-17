package app.kobuggi.hyuabot.ui.subway.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.kobuggi.hyuabot.databinding.FragmentSubwayRealtimeBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SubwayTimetableFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentSubwayRealtimeBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }
}
