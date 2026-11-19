package app.kobuggi.hyuabot.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.service.safeNavigate
import app.kobuggi.hyuabot.ui.common.coachmark.CoachmarkStep
import app.kobuggi.hyuabot.ui.common.coachmark.Coachmarks
import app.kobuggi.hyuabot.ui.common.coachmark.showCoachmarkOnce
import app.kobuggi.hyuabot.ui.common.compose.HyuabotTheme
import app.kobuggi.hyuabot.ui.common.openCustomTab
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** 캠퍼스 탭 (Compose). 이동 방식은 [CampusDestination]마다 정해져 있다. */
@AndroidEntryPoint
class MenuFragment @Inject constructor() : Fragment() {
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    /** Compose 레이아웃 위치를 따라가는 투명 View. 코치마크(View 기반)가 가리킬 대상이다. */
    private var toolsAnchor: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val ctx = requireContext()
        val anchor = View(ctx).apply { importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO }
        toolsAnchor = anchor
        val compose = ComposeView(ctx).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { HyuabotTheme { CampusScreen(onSelect = ::onSelect, onToolsBounds = ::placeAnchor) } }
        }
        val root = FrameLayout(ctx).apply {
            addView(compose, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            addView(anchor, FrameLayout.LayoutParams(0, 0))
        }
        showCoachmarkOnce(userPreferencesRepository, Coachmarks.MENU) {
            listOf(CoachmarkStep({ toolsAnchor }, R.string.coachmark_menu_title, R.string.coachmark_menu_desc))
        }
        return root
    }

    override fun onDestroyView() {
        toolsAnchor = null
        super.onDestroyView()
    }

    private fun placeAnchor(bounds: Rect) {
        val anchor = toolsAnchor ?: return
        val lp = anchor.layoutParams as FrameLayout.LayoutParams
        val width = bounds.width.toInt()
        val height = bounds.height.toInt()
        if (lp.width != width || lp.height != height) {
            lp.width = width
            lp.height = height
            anchor.layoutParams = lp
        }
        anchor.translationX = bounds.left
        anchor.translationY = bounds.top
    }

    private fun onSelect(destination: CampusDestination) {
        AnalyticsManager.logSelect(
            AnalyticsItem.CAMPUS_SELECT_TOOL,
            type = AnalyticsContentType.LIST_ITEM,
            name = destination.id,
            destinationId = destination.id,
        )
        val nav = findNavController()
        when (destination) {
            CampusDestination.CommuterBus -> nav.safeNavigate(MenuFragmentDirections.actionMenuFragmentToCommuterFragment())
            CampusDestination.Map -> nav.safeNavigate(MenuFragmentDirections.actionMenuFragmentToMapFragment())
            CampusDestination.ReadingRoom -> nav.safeNavigate(MenuFragmentDirections.actionMenuFragmentToReadingRoomFragment())
            CampusDestination.Calendar -> nav.safeNavigate(MenuFragmentDirections.actionMenuFragmentToCalendarFragment())
            CampusDestination.Contact -> nav.safeNavigate(MenuFragmentDirections.actionMenuFragmentToContactFragment())
            CampusDestination.Setting -> nav.safeNavigate(MenuFragmentDirections.actionMenuFragmentToSettingFragment())
            CampusDestination.Inquiry -> nav.safeNavigate(MenuFragmentDirections.actionMenuFragmentToInquiryChatFragment())
            CampusDestination.Donate -> requireContext().openCustomTab(DONATE_URL)
        }
    }

    private companion object {
        const val DONATE_URL = "https://qr.kakaopay.com/FWxVPo8iO"
    }
}
