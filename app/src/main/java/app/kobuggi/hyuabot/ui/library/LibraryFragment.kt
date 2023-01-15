package app.kobuggi.hyuabot.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.databinding.FragmentLibraryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LibraryFragment : Fragment() {
    private val vm by viewModels<LibraryViewModel>()
    private val binding: FragmentLibraryBinding by lazy {
        FragmentLibraryBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = vm
        val sharedPreferences = requireActivity().getSharedPreferences("hyuabot", 0)
        val adapter = LibraryAdapter(requireContext(), sharedPreferences, listOf())
        vm.campusID = sharedPreferences.getInt("campus", 2)

        binding.readingRoomList.adapter = adapter
        binding.readingRoomList.layoutManager = LinearLayoutManager(requireContext())
        vm.fetchData()
        vm.start()
        vm.rooms.observe(viewLifecycleOwner) {
            adapter.setReadingRooms(it)
            if (it.isEmpty()){
                binding.readingRoomNoData.visibility = View.VISIBLE
                binding.readingRoomList.visibility = View.GONE
            } else {
                binding.readingRoomNoData.visibility = View.GONE
                binding.readingRoomList.visibility = View.VISIBLE
            }
        }
        binding.refreshLayout.setOnRefreshListener {
            vm.fetchData()
            binding.refreshLayout.isRefreshing = false
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        vm.start()
    }

    override fun onPause() {
        super.onPause()
        vm.stop()
    }
}