package app.kobuggi.hyuabot.ui.common.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.getValue
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 앱 바 오른쪽의 캠퍼스 칩 (칩 32dp · 터치 48dp).
 * 앱 바 끝 여백 4dp + 터치 여백 12dp로 칩의 오른쪽 끝이 화면 여백 16dp 선에 놓인다.
 */
@Composable
fun CampusChip(name: String, onClick: () -> Unit, modifier: Modifier = Modifier, description: String? = null) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .height(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(scheme.primaryContainer)
                .padding(start = 8.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(HyuabotIcons.School, contentDescription = description, tint = scheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
            Text(name, style = MaterialTheme.typography.labelLarge, color = scheme.onPrimaryContainer)
        }
    }
}

/** 목록이 비었을 때 위쪽(칩 아래 48)에 놓는 안내. 키보드가 올라와도 스크롤로 버튼에 닿는다 (아이콘 칸 88 · 제목 24 · 설명 8 · 버튼 16). */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String,
    actionIcon: ImageVector,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 32.dp, end = 32.dp, top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Box(
            modifier = Modifier.size(88.dp).clip(RoundedCornerShape(32.dp)).background(scheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = scheme.onPrimaryContainer, modifier = Modifier.size(40.dp))
        }
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp),
        )
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        FilledTonalButton(
            onClick = onAction,
            modifier = Modifier.padding(top = 16.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 24.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = scheme.primaryContainer,
                contentColor = scheme.onPrimaryContainer,
            ),
        ) {
            Icon(actionIcon, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(actionLabel, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

/**
 * 당겨서 새로고침. 컨테이너 32 · primaryContainer 위에 24 진행 표시 (M3 Expressive 로딩 표시기 모양).
 * [loadingLabel]은 진행 중일 때 읽어 주는 설명이다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HyuabotPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    loadingLabel: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val pullState = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = pullState,
        indicator = {
            val scheme = MaterialTheme.colorScheme
            val visible = isRefreshing || pullState.distanceFraction > 0f
            if (visible) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .graphicsLayer {
                            val fraction = if (isRefreshing) 1f else pullState.distanceFraction.coerceIn(0f, 1f)
                            translationY = (fraction - 1f) * 48.dp.toPx()
                            alpha = fraction
                        }
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(scheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).semantics { contentDescription = loadingLabel },
                            strokeWidth = 3.dp,
                            color = scheme.onPrimaryContainer,
                            trackColor = Color.Transparent,
                        )
                    } else {
                        CircularProgressIndicator(
                            progress = { pullState.distanceFraction.coerceIn(0f, 1f) },
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp,
                            color = scheme.onPrimaryContainer,
                            trackColor = Color.Transparent,
                        )
                    }
                }
            }
        },
        content = {
            val appBar = LocalTopBarNestedScroll.current
            Box(if (appBar != null) Modifier.nestedScroll(appBar) else Modifier) { content() }
        },
    )
}

/** 불러오지 못했을 때의 카드: 아이콘 56 → 12 → 문구 → 12 → 다시 시도 (40, 터치 48). 카드 모서리 24, 좌우 여백 16. */
@Composable
fun ErrorCard(message: String, retryLabel: String, onRetry: () -> Unit, modifier: Modifier = Modifier, topGap: Dp = 12.dp) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, top = topGap)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(scheme.surfaceContainerLowest)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(56.dp).clip(RoundedCornerShape(20.dp)).background(scheme.surfaceContainerHighest), contentAlignment = Alignment.Center) {
            Icon(HyuabotIcons.CloudOff, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(28.dp))
        }
        Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Box(
            modifier = Modifier
                .requiredHeight(48.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onRetry),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(scheme.primaryContainer)
                    .padding(start = 16.dp, end = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(HyuabotIcons.Refresh, null, tint = scheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                Text(retryLabel, style = MaterialTheme.typography.labelLarge, color = scheme.onPrimaryContainer)
            }
        }
    }
}
