package app.kobuggi.hyuabot.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.HorizontalPageIndicator
import app.kobuggi.hyuabot.util.PageScreenIndicatorState
import com.google.accompanist.pager.*

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PagerScreen(
    count: Int,
    state: PagerState = rememberPagerState(),
    content: @Composable() (PagerScope.(Int) -> Unit)
) {
    val shape = if (LocalConfiguration.current.isScreenRound) CircleShape else null
    Box(modifier = Modifier.fillMaxSize()){
        HorizontalPager(count = count, state = state) {
            Box(
                modifier = Modifier.fillMaxSize().run { if (shape != null) clip(shape) else this },
                contentAlignment = Alignment.Center
            ) {
                content(it)
            }
        }

        val pagerScreenState = PageScreenIndicatorState(state)
        HorizontalPageIndicator(
            modifier = Modifier.padding(6.dp),
            pageIndicatorState = pagerScreenState
        )
    }
}
