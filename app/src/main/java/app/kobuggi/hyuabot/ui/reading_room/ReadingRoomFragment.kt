package app.kobuggi.hyuabot.ui.reading_room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.databinding.FragmentReadingRoomBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReadingRoomFragment : Fragment() {
    private val vm by viewModels<ReadingRoomViewModel>()
    private lateinit var binding: FragmentReadingRoomBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReadingRoomBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = vm

        return binding.root
    }
}