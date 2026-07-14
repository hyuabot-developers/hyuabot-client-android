package app.kobuggi.hyuabot.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import androidx.lifecycle.lifecycleScope
import app.kobuggi.hyuabot.databinding.FragmentMenuBinding
import app.kobuggi.hyuabot.databinding.ItemCampusToolBinding
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.service.safeNavigate
import app.kobuggi.hyuabot.ui.common.coachmark.Coachmarks
import app.kobuggi.hyuabot.ui.common.coachmark.CoachmarkStep
import app.kobuggi.hyuabot.ui.common.coachmark.showCoachmarkOnce
import app.kobuggi.hyuabot.util.InAppReviewManager
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MenuFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentMenuBinding.inflate(layoutInflater) }

    @Inject
    lateinit var inAppReviewManager: InAppReviewManager

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private val menuList = listOf(
        MenuItem(R.drawable.ic_settings, R.string.menu_settings, "settings"),
        MenuItem(R.drawable.ic_chat, R.string.menu_chat, "inquiry"),
        MenuItem(R.drawable.ic_donate, R.string.menu_donate, "donate"),
        MenuItem(R.drawable.ic_star, R.string.menu_review, "review"),
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val menuAdapter = MenuListAdapter(requireContext(), menuList, ::onClickMenu)
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        setupCampusTools()

        binding.menuRecyclerView.apply {
            adapter = menuAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(decoration)
        }
        showCoachmarkOnce(userPreferencesRepository, Coachmarks.MENU) {
            listOf(
                CoachmarkStep(
                    { binding.campusToolsGrid },
                    R.string.coachmark_menu_title, R.string.coachmark_menu_desc
                ),
            )
        }
        return binding.root
    }

    private fun setupCampusTools() {
        bindCampusTool(
            binding.campusMapCard,
            R.drawable.ic_map,
            R.string.campus_map_title,
            R.string.campus_map_subtitle,
            "map",
        ) { onClickMenu(MenuItem(R.drawable.ic_map, R.string.menu_map, "map")) }
        bindCampusTool(
            binding.campusReadingRoomCard,
            R.drawable.ic_book,
            R.string.campus_reading_room_title,
            R.string.campus_reading_room_subtitle,
            "reading_room",
        ) { onClickMenu(MenuItem(R.drawable.ic_book, R.string.menu_book, "reading_room")) }
        bindCampusTool(
            binding.campusCalendarCard,
            R.drawable.ic_calendar,
            R.string.campus_calendar_title,
            R.string.campus_calendar_subtitle,
            "calendar",
        ) { onClickMenu(MenuItem(R.drawable.ic_calendar, R.string.menu_calendar, "calendar")) }
        bindCampusTool(
            binding.campusContactCard,
            R.drawable.ic_contact,
            R.string.campus_contact_title,
            R.string.campus_contact_subtitle,
            "contact",
        ) { onClickMenu(MenuItem(R.drawable.ic_contact, R.string.menu_contact, "contact")) }
    }

    private fun bindCampusTool(
        binding: ItemCampusToolBinding,
        iconRes: Int,
        titleRes: Int,
        subtitleRes: Int,
        analyticsName: String,
        onClick: () -> Unit,
    ) {
        val title = getString(titleRes)
        val subtitle = getString(subtitleRes)
        binding.toolIcon.setImageResource(iconRes)
        binding.toolTitle.text = title
        binding.toolSubtitle.text = subtitle
        binding.toolCard.contentDescription = "$title. $subtitle"
        binding.toolCard.setOnClickListener {
            AnalyticsManager.logSelect(
                AnalyticsItem.CAMPUS_SELECT_TOOL,
                type = AnalyticsContentType.LIST_ITEM,
                name = analyticsName,
                destinationId = analyticsName,
            )
            onClick()
        }
    }

    private fun onClickMenu(menuItem: MenuItem) {
        when(menuItem.titleResource) {
            R.string.menu_book -> {
                MenuFragmentDirections.actionMenuFragmentToReadingRoomFragment().also {
                    findNavController().safeNavigate(it)
                }
            }
            R.string.menu_map -> {
                MenuFragmentDirections.actionMenuFragmentToMapFragment().also {
                    findNavController().safeNavigate(it)
                }
            }
            R.string.menu_contact -> {
                MenuFragmentDirections.actionMenuFragmentToContactFragment().also {
                    findNavController().safeNavigate(it)
                }
            }
            R.string.menu_calendar -> {
                MenuFragmentDirections.actionMenuFragmentToCalendarFragment().also {
                    findNavController().safeNavigate(it)
                }
            }
            R.string.menu_settings -> {
                MenuFragmentDirections.actionMenuFragmentToSettingFragment().also {
                    findNavController().safeNavigate(it)
                }
            }
            R.string.menu_chat -> {
                val url = "https://open.kakao.com/o/sW2kAinb"
                MenuFragmentDirections.actionMenuFragmentToNoticeWebViewFragment(url).also {
                    findNavController().safeNavigate(it)
                }
            }
            R.string.menu_donate -> {
                val url = "https://qr.kakaopay.com/FWxVPo8iO"
                MenuFragmentDirections.actionMenuFragmentToNoticeWebViewFragment(url).also {
                    findNavController().safeNavigate(it)
                }
            }
            R.string.menu_review -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    inAppReviewManager.launchReview(requireActivity())
                }
            }
            else -> {}
        }
    }
}
