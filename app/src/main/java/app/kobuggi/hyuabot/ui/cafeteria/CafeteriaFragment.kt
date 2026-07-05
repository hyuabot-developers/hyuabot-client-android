package app.kobuggi.hyuabot.ui.cafeteria
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentCafeteriaBinding
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.ui.common.coachmark.Coachmarks
import app.kobuggi.hyuabot.ui.common.coachmark.CoachmarkStep
import app.kobuggi.hyuabot.ui.common.coachmark.showCoachmarkOnce
import app.kobuggi.hyuabot.util.setSkeletonLoading
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
    private val args: CafeteriaFragmentArgs by navArgs()

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

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
            AnalyticsManager.logSelect(AnalyticsItem.CAFETERIA_DATE_CHANGED, type = AnalyticsContentType.DATE_CONTROL)
            viewModel.date.value = LocalDateTime.ofEpochSecond(it / 1000, 0, ZoneOffset.ofHours(9))
        }

        viewModel.apply {
            campusID.observe(viewLifecycleOwner) {
                fetchData(it)
            }
            isLoading.observe(viewLifecycleOwner) {
                binding.loadingLayout.setSkeletonLoading(it)
            }
            queryError.observe(viewLifecycleOwner) {
                it?.let { Toast.makeText(requireContext(), getString(R.string.cafeteria_error), Toast.LENGTH_SHORT).show() }
            }
            date.observe(viewLifecycleOwner) {
                binding.dateText.text = it.format(selectedDateFormatter)
                fetchData(campusID.value ?: 2)
                updateShareButtonState()
            }
            breakfast.observe(viewLifecycleOwner) { updateShareButtonState() }
            lunch.observe(viewLifecycleOwner) { updateShareButtonState() }
            dinner.observe(viewLifecycleOwner) { updateShareButtonState() }
        }
        binding.viewPager.adapter = viewpagerAdapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateShareButtonState()
            }
        })
        binding.dateButton.setOnClickListener {
            if (!datePicker.isAdded) {
                datePicker.show(childFragmentManager, "CafeteriaDatePicker")
            }
        }
        binding.prevButton.setOnClickListener { AnalyticsManager.logSelect(AnalyticsItem.CAFETERIA_PREVIOUS_DATE, type = AnalyticsContentType.DATE_CONTROL); viewModel.date.value = viewModel.date.value?.minusDays(1) }
        binding.nextButton.setOnClickListener { AnalyticsManager.logSelect(AnalyticsItem.CAFETERIA_NEXT_DATE, type = AnalyticsContentType.DATE_CONTROL); viewModel.date.value = viewModel.date.value?.plusDays(1) }
        binding.shareCafeteriaButton.setOnClickListener {
            val (mealType, menus) = currentMealMenus()
            if (menus.isEmpty()) return@setOnClickListener
            AnalyticsManager.logSelect(AnalyticsItem.CAFETERIA_SHARE_BUTTON)
            shareCafeteriaMenus(
                requireContext(),
                viewModel.date.value,
                mealType,
                menus,
            )
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getString(tabLabelList[position])
        }.attach()
        val initialTab = when (args.tab) {
            "breakfast" -> 0
            "lunch" -> 1
            "dinner" -> 2
            else -> defaultMealTab(viewModel.date.value ?: LocalDateTime.now())
        }
        binding.viewPager.setCurrentItem(initialTab, false)
        showCoachmarkOnce(userPreferencesRepository, Coachmarks.CAFETERIA) {
            listOf(
                CoachmarkStep(
                    { binding.tabLayout },
                    R.string.coachmark_cafeteria_tab_title, R.string.coachmark_cafeteria_tab_desc
                ),
                CoachmarkStep(
                    { binding.datePickerLayout },
                    R.string.coachmark_cafeteria_date_title, R.string.coachmark_cafeteria_date_desc
                ),
                CoachmarkStep(
                    { null },
                    R.string.coachmark_cafeteria_widget_title, R.string.coachmark_cafeteria_widget_desc,
                    centered = true
                ),
            )
        }
        return binding.root
    }

    private fun updateShareButtonState() {
        val hasMenus = currentMealMenus().second.isNotEmpty()
        binding.shareCafeteriaButton.isEnabled = hasMenus
        binding.shareCafeteriaButton.alpha = if (hasMenus) 1f else 0.45f
    }

    private fun currentMealMenus() = when (binding.viewPager.currentItem) {
        0 -> "breakfast" to (viewModel.breakfast.value ?: emptyList())
        1 -> "lunch" to (viewModel.lunch.value ?: emptyList())
        2 -> "dinner" to (viewModel.dinner.value ?: emptyList())
        else -> "lunch" to (viewModel.lunch.value ?: emptyList())
    }

    private fun defaultMealTab(dateTime: LocalDateTime): Int = when (dateTime.hour) {
        in 0..10 -> 0
        in 11..16 -> 1
        else -> 2
    }

    override fun onDestroyView() {
        super.onDestroyView()
        childFragmentManager.fragments.toList().forEach {
            childFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
        }
        binding.viewPager.adapter = null
    }
}
