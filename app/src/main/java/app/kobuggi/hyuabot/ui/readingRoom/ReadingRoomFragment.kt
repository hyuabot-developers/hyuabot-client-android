package app.kobuggi.hyuabot.ui.readingRoom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.databinding.FragmentReadingRoomBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReadingRoomFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentReadingRoomBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<ReadingRoomViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val roomListAdapter = ReadingRoomListAdapter(requireContext(), emptyList())
        viewModel.apply {
            fetchRooms()
            rooms.observe(viewLifecycleOwner) {
                binding.readingRoomSwipeRefreshLayout.isRefreshing = false
                roomListAdapter.updateData(it)
            }
        }

        binding.apply {
            readingRoomSwipeRefreshLayout.setOnRefreshListener {
                viewModel.fetchRooms()
            }
            readingRoomRecyclerView.apply {
                setHasFixedSize(true)
                adapter = roomListAdapter
                layoutManager = LinearLayoutManager(context)
            }
        }

        return binding.root
    }
}
