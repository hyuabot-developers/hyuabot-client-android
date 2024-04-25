package app.kobuggi.hyuabot.ui.shuttle.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.databinding.FragmentShuttleTimetableTabBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShuttleTabWeekendsFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentShuttleTimetableTabBinding.inflate(layoutInflater) }
    private val parentViewModel: ShuttleTimetableViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return binding.root
    }
}
