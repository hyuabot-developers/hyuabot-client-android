package app.kobuggi.hyuabot.ui.cafeteria

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.MainActivity
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.component.card.cafeteria.RestaurantCardAdapter
import app.kobuggi.hyuabot.databinding.FragmentCafeteriaBinding
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class CafeteriaFragment : Fragment() {
    companion object {
        fun newInstance() = CafeteriaFragment()
    }
    private val viewModel: CafeteriaViewModel by viewModels()
    private val binding by lazy { FragmentCafeteriaBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val adapter = RestaurantCardAdapter(requireContext())
        val now = LocalTime.now()
        val sharedPreferences = requireActivity().getSharedPreferences("hyuabot", 0)
        val campusID = sharedPreferences.getInt("campus", 2)
        viewModel.setCampusID(campusID)

        viewModel.fetchData()
        viewModel.currentDate.observe(viewLifecycleOwner) {
            binding.currentDateText.text = it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            viewModel.fetchData()
        }
        viewModel.breakfast.observe(viewLifecycleOwner) {
            adapter.updateData(0, it)
        }
        viewModel.lunch.observe(viewLifecycleOwner) {
            adapter.updateData(1, it)
        }
        viewModel.dinner.observe(viewLifecycleOwner) {
            adapter.updateData(2, it)
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            if (it) {
                val toast = Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT)
                toast.show()
            }
        }
        if (now.hour < 10) {
            binding.cafeteriaRecyclerView.smoothScrollToPosition(0)
        } else if (now.hour < 15) {
            binding.cafeteriaRecyclerView.smoothScrollToPosition(1)
        } else {
            binding.cafeteriaRecyclerView.smoothScrollToPosition(2)
        }

        binding.previousDateButton.setOnClickListener { viewModel.previousDate() }
        binding.nextDateButton.setOnClickListener { viewModel.nextDate() }
        binding.cafeteriaRecyclerView.adapter = adapter
        binding.cafeteriaRecyclerView.itemAnimator = null
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (activity is MainActivity) {
            (activity as MainActivity).getAnalytics().logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, "학식 정보")
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, "CafeteriaFragment")
            })
        }
    }
}