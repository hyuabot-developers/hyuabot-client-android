package app.kobuggi.hyuabot.ui.cafeteria

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.component.card.cafeteria.RestaurantCardAdapter
import app.kobuggi.hyuabot.databinding.FragmentCafeteriaBinding
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
}