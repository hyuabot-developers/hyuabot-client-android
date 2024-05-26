package app.kobuggi.hyuabot.ui.cafeteria

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentCafeteriaBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject

@AndroidEntryPoint
class CafeteriaFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentCafeteriaBinding.inflate(layoutInflater) }
    private val viewModel: CafeteriaViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewpagerAdapter = CafeteriaViewPagerAdapter(childFragmentManager, lifecycle)
        val tabLabelList = listOf(
            R.string.cafeteria_tab_breakfast,
            R.string.cafeteria_tab_lunch,
            R.string.cafeteria_tab_dinner
        )

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.cafeteria_date_title)
            .setSelection(viewModel.date.value?.toEpochSecond(
                ZoneOffset.ofHours(9)
            )?.times(1000))
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .build()
        datePicker.addOnPositiveButtonClickListener {
            viewModel.apply {
                date.value = LocalDateTime.ofEpochSecond(it / 1000, 0, ZoneOffset.ofHours(9))
                fetchData()
            }
        }

        viewModel.apply {
            fetchData()
            isLoading.observe(viewLifecycleOwner) {
                binding.loadingLayout.visibility = if (it) View.VISIBLE else View.GONE
            }
        }
        binding.viewPager.adapter = viewpagerAdapter
        binding.dateFab.setOnClickListener {
            datePicker.show(childFragmentManager, "datePicker")
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getString(tabLabelList[position])
        }.attach()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPager.adapter = null
    }
}
