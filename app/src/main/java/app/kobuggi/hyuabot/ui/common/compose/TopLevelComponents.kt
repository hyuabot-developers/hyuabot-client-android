package app.kobuggi.hyuabot.ui.common.compose

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 디자인(HTML)처럼 줄 상자 전체 높이를 쓴다: 첫 줄 위·마지막 줄 아래를 깎지 않는다. 설정 행 같은 기존 화면에는 영향이 없도록 필요한 곳에서만 쓴다. */
fun TextStyle.fullLine(): TextStyle =
    copy(lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None))

/**
 * 하단 탭의 최상위 화면 앱 바: 제목 28/36 + 부제 14/20 (위 20 · 제목과 부제 4 · 아래 16, 뒤로 없음).
 * 스크롤해 제목이 지나가면 64dp 한 줄 바가 위에 나타난다. 본문은 [ColumnScope]에서 이어서 그린다.
 */
@Composable
fun TopLevelScaffold(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val scroll = rememberScrollState()
    val collapsePx = with(LocalDensity.current) { (20.dp + 36.dp).toPx() }
    // 내용이 짧아 제목이 다 지나가기 전에 스크롤이 끝나면 접힌 바를 쓰지 않는다(펼친 제목과 겹침 방지).
    val barAlpha = if (scroll.maxValue < collapsePx) 0f else (scroll.value / collapsePx).coerceIn(0f, 1f)
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll).padding(bottom = bottomPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = 1f - barAlpha }
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(title, style = MaterialTheme.typography.headlineMedium.fullLine(), modifier = Modifier.semantics { heading() })
                Text(subtitle, style = MaterialTheme.typography.bodyMedium.fullLine(), color = scheme.onSurfaceVariant)
            }
            content()
        }
        if (barAlpha > 0f) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(64.dp).graphicsLayer { alpha = barAlpha },
                color = scheme.surface,
            ) {
                Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/** 카드 밖 섹션 제목: 패딩 8/16 → 글자는 32 정렬선, 15/20 굵게 onSurfaceVariant. */
@Composable
fun SectionLabel(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall.fullLine(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).semantics { heading() },
    )
}

/** 누르는 동안 모서리가 [radius] → [pressedRadius]로 바뀌는 M3 표면 (스프링). */
@Composable
fun MorphingSurface(
    onClick: () -> Unit,
    radius: Dp,
    pressedRadius: Dp,
    color: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val corner by animateDpAsState(
        if (pressed) pressedRadius else radius,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "corner",
    )
    val shape: Shape = RoundedCornerShape(corner)
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        interactionSource = source,
        content = content,
    )
}
