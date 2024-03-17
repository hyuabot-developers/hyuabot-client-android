package app.kobuggi.hyuabot.ui.bus.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import app.kobuggi.hyuabot.databinding.DialogBusStopBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BusStopDialog : DialogFragment() {
    private val binding by lazy { DialogBusStopBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }
}
