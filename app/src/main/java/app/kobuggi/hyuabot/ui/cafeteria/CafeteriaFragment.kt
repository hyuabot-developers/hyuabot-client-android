package app.kobuggi.hyuabot.ui.cafeteria

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentCafeteriaBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
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
        val selectedDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.")
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
                campusID.observe(viewLifecycleOwner) { campusID ->
                    fetchData(campusID)
                }
            }
        }

        viewModel.apply {
            campusID.observe(viewLifecycleOwner) {
                fetchData(it)
            }
            isLoading.observe(viewLifecycleOwner) {
                binding.loadingLayout.visibility = if (it) View.VISIBLE else View.GONE
            }
            queryError.observe(viewLifecycleOwner) {
                it?.let { Toast.makeText(requireContext(), getString(R.string.cafeteria_error), Toast.LENGTH_SHORT).show() }
            }
            date.observe(viewLifecycleOwner) {
                binding.dateText.text = it.format(selectedDateFormatter)
                fetchData(campusID.value ?: 2)
            }
        }
        binding.viewPager.adapter = viewpagerAdapter
        binding.dateButton.setOnClickListener { datePicker.show(childFragmentManager, "datePicker") }
        binding.prevButton.setOnClickListener { viewModel.date.value = viewModel.date.value?.minusDays(1) }
        binding.nextButton.setOnClickListener { viewModel.date.value = viewModel.date.value?.plusDays(1) }
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
