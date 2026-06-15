package app.kobuggi.hyuabot.ui.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import androidx.lifecycle.lifecycleScope
import app.kobuggi.hyuabot.databinding.FragmentMenuBinding
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.service.safeNavigate
import app.kobuggi.hyuabot.ui.common.coachmark.Coachmarks
import app.kobuggi.hyuabot.ui.common.coachmark.CoachmarkStep
import app.kobuggi.hyuabot.ui.common.coachmark.showCoachmarkOnce
import app.kobuggi.hyuabot.util.InAppReviewManager
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
        MenuItem(R.drawable.ic_book, R.string.menu_book),
        MenuItem(R.drawable.ic_map, R.string.menu_map),
        MenuItem(R.drawable.ic_contact, R.string.menu_contact),
        MenuItem(R.drawable.ic_calendar, R.string.menu_calendar),
        MenuItem(R.drawable.ic_settings, R.string.menu_settings),
        MenuItem(R.drawable.ic_chat, R.string.menu_chat),
        MenuItem(R.drawable.ic_donate, R.string.menu_donate),
        MenuItem(R.drawable.ic_star, R.string.menu_review),
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val menuAdapter = MenuListAdapter(requireContext(), menuList, ::onClickMenu)
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

        binding.menuRecyclerView.apply {
            adapter = menuAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(decoration)
        }
        showCoachmarkOnce(userPreferencesRepository, Coachmarks.MENU) {
            listOf(
                CoachmarkStep(
                    { binding.menuRecyclerView },
                    R.string.coachmark_menu_title, R.string.coachmark_menu_desc
                ),
            )
        }
        return binding.root
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
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                startActivity(intent)
            }
            R.string.menu_donate -> {
                val url = "https://qr.kakaopay.com/FWxVPo8iO"
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                startActivity(intent)
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
