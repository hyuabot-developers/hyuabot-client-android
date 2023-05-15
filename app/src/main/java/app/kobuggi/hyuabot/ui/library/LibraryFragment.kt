package app.kobuggi.hyuabot.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.MainActivity
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentLibraryBinding
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LibraryFragment : Fragment() {
    companion object {
        fun newInstance() = LibraryFragment()
    }
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
        vm.errorMessage.observe(viewLifecycleOwner) {
            if (it) {
                val toast = Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT)
                toast.show()
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
        if (activity is MainActivity) {
            (activity as MainActivity).getAnalytics().logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, "열람실 실시간 정보")
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, "ReadingRoomFragment")
            })
        }
    }

    override fun onPause() {
        super.onPause()
        vm.stop()
    }
}