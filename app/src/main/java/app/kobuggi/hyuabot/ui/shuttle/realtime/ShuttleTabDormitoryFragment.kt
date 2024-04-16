package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.kobuggi.hyuabot.databinding.FragmentShuttleRealtimeTabBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShuttleTabDormitoryFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentShuttleRealtimeTabBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.apply {
            headerBoundForDormitory.visibility = View.GONE
            realtimeViewBoundForDormitory.visibility = View.GONE
            noRealtimeDataBoundForDormitory.visibility = View.GONE
            entireTimetableBoundForDormitory.visibility = View.GONE
        }
        return binding.root
    }
}
