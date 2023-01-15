package app.kobuggi.hyuabot.util

import androidx.wear.compose.material.PageIndicatorState
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState

@OptIn(ExperimentalPagerApi::class)
class PageScreenIndicatorState(private val state: PagerState) : PageIndicatorState {
    override val selectedPage: Int
        get() = state.currentPage

    override val pageCount: Int
        get() = state.pageCount

    override val pageOffset: Float
        get() = state.currentPageOffset
}