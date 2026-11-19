package app.kobuggi.hyuabot.ui.commuter

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ui.common.compose.ErrorCard
import app.kobuggi.hyuabot.ui.common.compose.HyuabotIcons
import app.kobuggi.hyuabot.ui.common.compose.HyuabotPullToRefresh
import app.kobuggi.hyuabot.ui.common.compose.SettingsDimens
import app.kobuggi.hyuabot.ui.common.compose.SettingsScaffold
import app.kobuggi.hyuabot.ui.common.compose.SettingsSheet
import app.kobuggi.hyuabot.ui.common.compose.SheetAppRow
import app.kobuggi.hyuabot.ui.common.compose.settingsItemShape
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 행간 안에서 글자를 세로 가운데에 두어 레일 점(이름 줄 가운데)과 맞춘다. */
private val CenteredLine = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)

/** 노선 제목 → 노선 카드 간격. 제목 위(방향 → 제목)는 BlockGap 12. */
private val TitleGap = 8.dp

/** 통학버스 화면이 쓰는 간격 토큰 (4dp 그리드). 정렬선: 16 · 32 · 레일 중심 96 · 정류장 이름 116. */
private object CommuterDimens {
    val StopRowHeight = 60.dp
    val TimeColumn = 44.dp
    val TimeColumnLarge = 52.dp
    val RailColumn = 16.dp
    val RailGap = 12.dp
    val RailWidth = 2.dp
    val DirectionHeight = 40.dp
    val TouchTarget = 48.dp
    val RouteCardMinHeight = 64.dp
    val BlockGap = 12.dp
}

data class CommuterActions(
    val onBack: () -> Unit,
    val onDirection: (CommuterDirection) -> Unit,
    val onRoute: (String) -> Unit,
    val onReset: () -> Unit,
    val onExpand: () -> Unit,
    val onRetry: () -> Unit,
    val onRefresh: () -> Unit,
    val onOpenMap: (CommuterStop) -> Unit,
    val onCloseSheet: () -> Unit,
    val onPickMap: (CommuterMapApp, CommuterStop) -> Unit,
)

enum class CommuterMapApp { Google, Naver, Kakao }

@Composable
fun CommuterScreen(state: CommuterUiState, installedMaps: Set<CommuterMapApp>, actions: CommuterActions) {
    val model = state.model
    val subtitle = when {
        !state.hasContext -> ""
        else -> when (val s = model?.subtitle) {
            is CommuterSubtitle.Semester -> stringResource(R.string.commuter_period_semester_until, dayText(s.endDay))
            else -> stringResource(R.string.commuter_period_weekdays_only)
        }
    }
    SettingsScaffold(stringResource(R.string.campus_commuter_title), actions.onBack, subtitle = subtitle, pullToRefresh = true) { padding ->
        CommuterBody(state, padding, actions)
    }
    state.sheetStop?.let { stop ->
        CommuterMapSheet(stop, installedMaps, actions.onCloseSheet) { app -> actions.onPickMap(app, stop) }
    }
}

@Composable
private fun CommuterBody(state: CommuterUiState, padding: PaddingValues, actions: CommuterActions) {
    val model = state.model
    val refreshing = state.load == CommuterLoad.Refresh
    HyuabotPullToRefresh(
        isRefreshing = refreshing,
        onRefresh = actions.onRefresh,
        loadingLabel = stringResource(R.string.commuter_loading_a11y),
        modifier = Modifier.padding(padding).fillMaxWidth(),
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
            val firstLoad = !state.hasContext
            // 첫 로딩에 실패하면 방향·노선 없이 오류 카드만 보여 준다.
            val controlsHidden = firstLoad && state.failed
            if (!controlsHidden) {
                if (model != null && !firstLoad) DirectionGroup(model, actions.onDirection) else DirectionPlaceholder()
                RouteHeader(showReset = model?.showsResetToNearest == true, onReset = actions.onReset)
                if (model != null && !firstLoad) RouteGrid(model.routes, actions.onRoute) else RoutePlaceholder()
            }
            when {
                state.load != CommuterLoad.Idle -> TimelineSkeleton()
                state.failed -> ErrorCard(stringResource(R.string.commuter_error), stringResource(R.string.commuter_retry), actions.onRetry)
                model?.summary != null -> {
                    TimelineCard(model, actions)
                    Text(
                        stringResource(R.string.commuter_disclaimer),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = CommuterDimens.BlockGap),
                    )
                }
            }
        }
    }
}

// ───────────────────────── 방향 · 노선 ─────────────────────────

/** 연결 버튼 그룹: 바깥 20 / 안쪽 8, 선택하면 20 원형 + 체크. 높이 40, 터치 48. */
@Composable
private fun DirectionGroup(model: CommuterScreenModel, onPick: (CommuterDirection) -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = SettingsDimens.ScreenPadding)
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        model.directions.forEachIndexed { index, (direction, selected) ->
            val outer = 20.dp
            val inner by animateDpAsState(if (selected) outer else 8.dp, spring(dampingRatio = 0.6f, stiffness = 380f), label = "direction-corner")
            val shape = if (index == 0) RoundedCornerShape(outer, inner, inner, outer) else RoundedCornerShape(inner, outer, outer, inner)
            val label = stringResource(if (direction == CommuterDirection.ToCampus) R.string.commuter_direction_to else R.string.commuter_direction_from)
            val scheme = MaterialTheme.colorScheme
            Box(
                modifier = Modifier
                    .weight(1f)
                    .requiredHeight(CommuterDimens.TouchTarget)
                    .height(CommuterDimens.DirectionHeight)
                    .selectable(selected = selected, role = Role.Tab, onClick = { onPick(direction) }),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CommuterDimens.DirectionHeight)
                        .clip(shape)
                        .background(if (selected) scheme.primaryContainer else scheme.surfaceContainerHighest)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    if (selected) Icon(HyuabotIcons.Check, null, tint = scheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                    Text(
                        label,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal),
                        color = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun DirectionPlaceholder() {
    PulseRow {
        Row(
            modifier = Modifier.padding(horizontal = SettingsDimens.ScreenPadding).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            val color = MaterialTheme.colorScheme.surfaceContainerHighest
            Box(Modifier.weight(1f).height(CommuterDimens.DirectionHeight).clip(RoundedCornerShape(20.dp, 8.dp, 8.dp, 20.dp)).background(color))
            Box(Modifier.weight(1f).height(CommuterDimens.DirectionHeight).clip(RoundedCornerShape(8.dp, 20.dp, 20.dp, 8.dp)).background(color))
        }
    }
}

/** 방향 → 카드 44 (12 + 32 + 0). 제목은 32선, 오른쪽 텍스트 버튼은 40으로 위아래 −4 넘친다. */
@Composable
private fun RouteHeader(showReset: Boolean, onReset: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(start = SettingsDimens.ScreenPadding, end = SettingsDimens.ScreenPadding, top = CommuterDimens.BlockGap)
            .fillMaxWidth()
            .height(20.dp)
            .padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            stringResource(R.string.commuter_route_section),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (showReset) {
            Row(
                modifier = Modifier
                    .offset(x = 12.dp)
                    .requiredHeight(CommuterDimens.DirectionHeight)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onReset)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(HyuabotIcons.NearMe, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text(
                    stringResource(R.string.commuter_route_reset_nearest),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/** 2열 · 간격 8 · 최소 64. 선택하면 16 → 24 + primaryContainer + 체크. */
@Composable
private fun RouteGrid(routes: List<CommuterRouteItem>, onPick: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(start = SettingsDimens.ScreenPadding, end = SettingsDimens.ScreenPadding, top = TitleGap).fillMaxWidth().selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        routes.chunked(2).forEach { pair ->
            Row(modifier = Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { route -> RouteCard(route, onPick, Modifier.weight(1f).fillMaxHeight()) }
                if (pair.size == 1 && routes.size > 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RouteCard(route: CommuterRouteItem, onPick: (String) -> Unit, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    val selected = route.isSelected
    val corner by animateDpAsState(if (selected) 24.dp else 16.dp, spring(dampingRatio = 0.6f, stiffness = 380f), label = "route-corner")
    val description = stringResource(R.string.commuter_route_item_a11y, route.name, route.summary)
    Surface(
        modifier = modifier
            .heightIn(min = CommuterDimens.RouteCardMinHeight)
            .semantics {
                role = Role.RadioButton
                contentDescription = description
            }
            .selectable(selected = selected, role = Role.RadioButton, onClick = { onPick(route.name) }),
        shape = RoundedCornerShape(corner),
        color = if (selected) scheme.primaryContainer else scheme.surfaceContainerLowest,
        contentColor = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier
                        .widthIn(min = 40.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) scheme.primary else scheme.primaryContainer)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        route.name,
                        style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                        color = if (selected) scheme.onPrimary else scheme.onPrimaryContainer,
                    )
                }
                if (selected) Icon(HyuabotIcons.Check, null, modifier = Modifier.size(18.dp))
            }
            Text(
                route.summary,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RoutePlaceholder() {
    PulseRow {
        Row(
            modifier = Modifier.padding(start = SettingsDimens.ScreenPadding, end = SettingsDimens.ScreenPadding, top = TitleGap).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val color = MaterialTheme.colorScheme.surfaceContainerLowest
            Box(Modifier.weight(1f).height(CommuterDimens.RouteCardMinHeight).clip(RoundedCornerShape(16.dp)).background(color))
            Box(Modifier.weight(1f).height(CommuterDimens.RouteCardMinHeight).clip(RoundedCornerShape(16.dp)).background(color))
        }
    }
}

@Composable
private fun PulseRow(content: @Composable () -> Unit) {
    val alpha by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pulse-alpha",
    )
    Box(modifier = Modifier.graphicsLayer { this.alpha = alpha }) { content() }
}

// ───────────────────────── 타임라인 ─────────────────────────

@Composable
private fun TimelineCard(model: CommuterScreenModel, actions: CommuterActions) {
    val summary = model.summary ?: return
    val scheme = MaterialTheme.colorScheme
    val timelineLabel = stringResource(R.string.commuter_timeline_a11y, summary.routeName)
    Column(
        modifier = Modifier
            .padding(start = SettingsDimens.ScreenPadding, end = SettingsDimens.ScreenPadding, top = CommuterDimens.BlockGap)
            .fillMaxWidth()
            .semantics { contentDescription = timelineLabel },
        verticalArrangement = Arrangement.spacedBy(SettingsDimens.GroupGap),
    ) {
        SummarySegment(summary, model.routes.firstOrNull { it.isSelected }?.summary ?: summary.routeName)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(settingsItemShape(isFirst = false, isLast = true))
                .background(scheme.surfaceContainerLowest),
        ) {
            val showOff = model.offNotice != null
            if (showOff || summary.isPartial) {
                Column(
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    model.offNotice?.let { OffNotice(it) }
                    if (summary.isPartial) PartialNotice()
                }
            }
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                model.rows.forEach { row ->
                    when (row) {
                        is CommuterTimelineRow.Fold -> FoldRow(row.hiddenCount, actions.onExpand)
                        is CommuterTimelineRow.Stop -> StopRow(row.row, actions.onOpenMap)
                        is CommuterTimelineRow.Gap -> GapRow(row)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummarySegment(summary: CommuterRouteSummary, title: String) {
    val scheme = MaterialTheme.colorScheme
    Surface(shape = settingsItemShape(isFirst = true, isLast = false), color = scheme.surfaceContainerLowest, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f),
            )
            PhaseChip(summary.phase)
        }
    }
}

@Composable
private fun PhaseChip(phase: CommuterPhase) {
    val scheme = MaterialTheme.colorScheme
    val running = phase == CommuterPhase.Running
    val label = stringResource(
        when (phase) {
            CommuterPhase.Running -> R.string.commuter_status_running
            CommuterPhase.BeforeRun -> R.string.commuter_status_before
            CommuterPhase.Finished -> R.string.commuter_status_finished
            CommuterPhase.Off -> R.string.commuter_status_off
        },
    )
    Row(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (running) scheme.primaryContainer else scheme.surfaceContainerHighest)
            .padding(start = 10.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(if (running) scheme.primary else scheme.outline))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = if (running) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OffNotice(notice: CommuterOffNotice) {
    val scheme = MaterialTheme.colorScheme
    val day = dayText(notice.nextRunDay)
    val title = stringResource(
        when (notice.reason) {
            CommuterOffReason.Halt -> R.string.commuter_off_halt
            CommuterOffReason.Vacation -> R.string.commuter_off_vacation
            CommuterOffReason.VacationSession -> R.string.commuter_off_vacation_session
            CommuterOffReason.Weekend -> R.string.commuter_off_weekend
        },
    )
    val sub = if (notice.reason == CommuterOffReason.Vacation || notice.reason == CommuterOffReason.VacationSession) {
        stringResource(R.string.commuter_off_resume, day)
    } else {
        stringResource(R.string.commuter_off_next_run, day)
    }
    NoticeBox(HyuabotIcons.EventBusy, scheme.surfaceContainerHighest, scheme.onSurfaceVariant) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = scheme.onSurface)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PartialNotice() {
    val container = colorResource(R.color.md_warn_container)
    val content = colorResource(R.color.md_on_warn_container)
    NoticeBox(HyuabotIcons.Info, container, content) {
        Text(stringResource(R.string.commuter_partial_notice), style = MaterialTheme.typography.bodySmall.copy(lineBreak = LineBreak.Paragraph.copy(wordBreak = LineBreak.WordBreak.Phrase)), color = content, modifier = Modifier.padding(top = 1.dp))
    }
}

/** 안내 박스: 모서리 16 · 패딩 8/12 · 아이콘 20 · 간격 12. */
@Composable
private fun NoticeBox(icon: ImageVector, container: Color, iconTint: Color, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(container)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        Box(modifier = Modifier.weight(1f)) { content() }
    }
}

@Composable
private fun timeColumnWidth(): Dp =
    if (LocalDensity.current.fontScale > 1.0f) CommuterDimens.TimeColumnLarge else CommuterDimens.TimeColumn

@Composable
private fun FoldRow(hidden: Int, onExpand: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val traveled = colorResource(R.color.md_rail_traveled)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
            .height(IntrinsicSize.Min)
            .clickable(role = Role.Button, onClick = onExpand)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(timeColumnWidth()))
        Box(
            modifier = Modifier
                .padding(horizontal = CommuterDimens.RailGap)
                .width(CommuterDimens.RailColumn)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier.fillMaxHeight().width(CommuterDimens.RailWidth).drawBehind {
                    val stroke = CommuterDimens.RailWidth.toPx()
                    drawLine(
                        traveled, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = stroke,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(0.1f, stroke * 2f)), cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    )
                },
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.commuter_fold, hidden), style = MaterialTheme.typography.labelLarge, color = scheme.primary)
            Icon(HyuabotIcons.ExpandMore, null, tint = scheme.primary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun GapRow(gap: CommuterTimelineRow.Gap) {
    val scheme = MaterialTheme.colorScheme
    val line = if (gap.traveled) colorResource(R.color.md_rail_traveled) else colorResource(R.color.md_rail_soft)
    val text = durationText(gap.minutes)
    val label = stringResource(R.string.commuter_gap, text)
    val a11y = stringResource(R.string.commuter_gap_a11y, text)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .padding(horizontal = 16.dp)
            .semantics(mergeDescendants = true) { contentDescription = a11y },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(timeColumnWidth()))
        Box(
            modifier = Modifier.padding(horizontal = CommuterDimens.RailGap).width(CommuterDimens.RailColumn).fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier.fillMaxHeight().width(CommuterDimens.RailWidth).drawBehind {
                    val stroke = CommuterDimens.RailWidth.toPx()
                    drawLine(
                        line, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = stroke,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(stroke * 2f, stroke * 2f)),
                    )
                },
            )
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
    }
}

/** 정류장 행: 모두 60 고정 (설명 없으면 이름 줄을 가운데로). 시각 44 + 12 + 레일 16 + 12 → 이름 x 116. */
@Composable
private fun StopRow(row: CommuterStopRow, onOpenMap: (CommuterStop) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val stop = row.stop
    val known = stop.time != null
    val campus = stop.isCampus
    val hasSub = stop.detail.isNotEmpty() || row.isNear
    val pad = if (hasSub) 8.dp else 18.dp
    val dotSize = when {
        row.isNear -> 16.dp
        campus -> 12.dp
        else -> 10.dp
    }
    val traveled = colorResource(R.color.md_rail_traveled)
    val rail = scheme.outlineVariant
    fun railColor(style: CommuterRail) = when (style) {
        CommuterRail.Hidden -> Color.Transparent
        CommuterRail.Traveled -> traveled
        CommuterRail.Normal -> rail
    }
    val timeColor = when {
        !known || row.isPassed -> scheme.onSurfaceVariant
        campus || row.isNear -> scheme.primary
        else -> scheme.onSurface
    }
    val nearLabel = stringResource(R.string.commuter_near)
    val a11y = buildString {
        append(stop.time ?: "—").append(", ").append(stop.name)
        if (row.isNear) append(", ").append(nearLabel)
    }
    val mapLabel = stringResource(R.string.commuter_map_a11y, stop.name)
    val busLabel = stringResource(R.string.commuter_bus_a11y)
    val timeWidth = timeColumnWidth()
    val lineHeight = with(LocalDensity.current) { 24.sp.toDp() }

    Box(modifier = Modifier.then(if (row.hasBus) Modifier.zIndex(1f) else Modifier)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = CommuterDimens.StopRowHeight)
                .height(IntrinsicSize.Min)
                .background(if (row.isNear) colorResource(R.color.md_near_container) else Color.Transparent)
                .padding(start = 16.dp, end = 8.dp)
                .semantics(mergeDescendants = true) { contentDescription = a11y },
        ) {
            Text(
                stop.time ?: "—",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 15.sp, lineHeight = 24.sp, lineHeightStyle = CenteredLine,
                    fontWeight = if (known) FontWeight.Bold else FontWeight.Normal, fontFeatureSettings = "tnum",
                ),
                color = timeColor,
                textAlign = TextAlign.End,
                softWrap = false,
                maxLines = 1,
                modifier = Modifier.width(timeColumnWidth()).padding(top = pad),
            )
            Column(
                modifier = Modifier.padding(horizontal = CommuterDimens.RailGap).width(CommuterDimens.RailColumn).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.width(CommuterDimens.RailWidth).height(pad + lineHeight / 2 - dotSize / 2).background(railColor(row.lineTop)))
                val ring = scheme.primaryContainer
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .then(if (row.isNear) Modifier.drawBehind { drawCircle(ring, radius = size.minDimension / 2 + 4.dp.toPx()) } else Modifier)
                        .clip(CircleShape)
                        .then(
                            when (row.dot) {
                                CommuterDot.Traveled -> Modifier.background(traveled)
                                CommuterDot.Filled -> Modifier.background(scheme.primary)
                                CommuterDot.Hollow -> Modifier
                                    .background(scheme.surfaceContainerLowest)
                                    .border(2.dp, rail, CircleShape)
                            },
                        ),
                )
                Box(Modifier.width(CommuterDimens.RailWidth).weight(1f).background(railColor(row.lineBottom)))
            }
            Column(
                modifier = Modifier.weight(1f).padding(vertical = pad),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stop.name,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (campus || row.isNear) FontWeight.Bold else FontWeight.Normal, lineHeightStyle = CenteredLine),
                        color = if (row.isPassed && !row.isNear) scheme.onSurfaceVariant else scheme.onSurface,
                        maxLines = if (LocalDensity.current.fontScale >= 1.3f) 2 else 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    row.tag?.let { tag ->
                        Box(Modifier.padding(top = (lineHeight - 20.dp) / 2)) {
                            Chip(
                                stringResource(if (tag == CommuterTag.Arrival) R.string.commuter_tag_arrival else R.string.commuter_tag_departure),
                                scheme.primaryContainer, scheme.onPrimaryContainer,
                            )
                        }
                    }
                }
                if (hasSub) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (row.isNear) {
                            Chip(nearLabel, scheme.primary, scheme.onPrimary, icon = HyuabotIcons.NearMe)
                        }
                        if (stop.detail.isNotEmpty()) {
                            Text(
                                stop.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                                maxLines = if (LocalDensity.current.fontScale >= 1.3f) 2 else 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                        }
                    }
                }
            }
            if (stop.hasCoordinate) {
                // 터치 48 안에 원 32 · 글리프 18. 오른쪽 −8 → 원 끝이 상태 칩 끝(16)과 맞는다.
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(CommuterDimens.TouchTarget)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClickLabel = mapLabel) { onOpenMap(stop) }
                        .semantics { contentDescription = mapLabel },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(32.dp).clip(CircleShape).background(scheme.primaryContainer), contentAlignment = Alignment.Center) {
                        Icon(HyuabotIcons.Map, null, tint = scheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                    }
                }
            } else {
                Spacer(Modifier.width(8.dp))
            }
        }
        if (row.hasBus) BusMarker(
            label = busLabel,
            modifier = Modifier.align(Alignment.BottomStart).offset(x = timeWidth + 22.dp, y = 14.dp),
        )
    }
}

@Composable
private fun Chip(text: String, container: Color, content: Color, icon: ImageVector? = null) {
    Row(
        modifier = Modifier
            .height(20.dp)
            .clip(CircleShape)
            .background(container)
            .padding(start = if (icon != null) 6.dp else 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon != null) Icon(icon, null, tint = content, modifier = Modifier.size(12.dp))
        Text(text, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = content, maxLines = 1)
    }
}

/** 버스: 행을 만들지 않고 두 정류장 경계의 레일 위에 겹쳐 그린다 (원 28 · 2 테두리). */
@Composable
private fun BusMarker(label: String, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "bus-ping")
    val progress by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1800)), label = "bus-ping-progress")
    Box(
        modifier = modifier
            .size(28.dp)
            .semantics {
                role = Role.Image
                contentDescription = label
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(28.dp).scale(1f + 0.9f * progress).alpha(0.45f * (1f - progress)).clip(CircleShape).background(scheme.primary),
        )
        Box(
            Modifier
                .size(28.dp)
                .border(2.dp, scheme.surfaceContainerLowest, CircleShape)
                .clip(CircleShape)
                .background(scheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(HyuabotIcons.DirectionsBus, null, tint = scheme.onPrimary, modifier = Modifier.size(16.dp))
        }
    }
}

// ───────────────────────── 로딩 · 오류 ─────────────────────────

@Composable
private fun TimelineSkeleton() {
    val scheme = MaterialTheme.colorScheme
    val loading = stringResource(R.string.commuter_loading_a11y)
    val widths = listOf(0.62f, 0.48f, 0.70f, 0.55f, 0.66f, 0.44f)
    PulseRow {
        Column(
            modifier = Modifier
                .padding(start = SettingsDimens.ScreenPadding, end = SettingsDimens.ScreenPadding, top = CommuterDimens.BlockGap)
                .fillMaxWidth()
                .semantics { contentDescription = loading },
            verticalArrangement = Arrangement.spacedBy(SettingsDimens.GroupGap),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(settingsItemShape(isFirst = true, isLast = false))
                    .background(scheme.surfaceContainerLowest)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            ) {
                Box(Modifier.fillMaxWidth(0.6f).height(16.dp).clip(RoundedCornerShape(8.dp)).background(scheme.surfaceContainerHighest))
                Box(Modifier.fillMaxWidth(0.36f).height(12.dp).clip(RoundedCornerShape(6.dp)).background(scheme.surfaceContainerHighest))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(settingsItemShape(isFirst = false, isLast = true))
                    .background(scheme.surfaceContainerLowest)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            ) {
                widths.forEach { fraction ->
                    Row(
                        modifier = Modifier.fillMaxWidth().height(CommuterDimens.StopRowHeight),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CommuterDimens.RailGap),
                    ) {
                        Box(Modifier.width(timeColumnWidth()).height(14.dp).clip(RoundedCornerShape(7.dp)).background(scheme.surfaceContainerHighest))
                        Box(Modifier.width(CommuterDimens.RailColumn), contentAlignment = Alignment.Center) {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(scheme.surfaceContainerHighest))
                        }
                        Box(Modifier.fillMaxWidth(fraction).height(14.dp).clip(RoundedCornerShape(7.dp)).background(scheme.surfaceContainerHighest))
                    }
                }
            }
        }
    }
}

// ───────────────────────── 지도 앱 시트 ─────────────────────────

/** M3 모달 바텀 시트. Google 지도는 항상 첫 행, 네이버·카카오는 설치된 경우만. 취소 버튼은 없다(스크림·뒤로·끌어내리기). */
@Composable
private fun CommuterMapSheet(stop: CommuterStop, installed: Set<CommuterMapApp>, onDismiss: () -> Unit, onPick: (CommuterMapApp) -> Unit) {
    val apps = buildList {
        add(CommuterMapApp.Google)
        if (CommuterMapApp.Naver in installed) add(CommuterMapApp.Naver)
        if (CommuterMapApp.Kakao in installed) add(CommuterMapApp.Kakao)
    }
    SettingsSheet(
        title = stringResource(R.string.commuter_map_chooser_title),
        onDismiss = onDismiss,
        subtitle = stringResource(R.string.commuter_map_chooser_subtitle, stop.name),
        subtitleStyle = MaterialTheme.typography.bodyMedium.copy(lineBreak = LineBreak.Paragraph.copy(wordBreak = LineBreak.WordBreak.Phrase)),
        contentTopGap = 16.dp,
    ) {
        apps.forEachIndexed { index, app ->
            val (nameRes, tile) = when (app) {
                CommuterMapApp.Google -> R.string.commuter_map_google to (Color(0xFF4285F4) to Color.White)
                CommuterMapApp.Naver -> R.string.commuter_map_naver to (Color(0xFF03C75A) to Color.White)
                CommuterMapApp.Kakao -> R.string.commuter_map_kakao to (Color(0xFFFEE500) to Color(0xFF1F1F1F))
            }
            SheetAppRow(
                name = stringResource(nameRes),
                tileColor = tile.first,
                shape = settingsItemShape(isFirst = index == 0, isLast = index == apps.lastIndex),
                onClick = { onPick(app) },
            ) {
                when (app) {
                    CommuterMapApp.Google -> Icon(HyuabotIcons.Place, null, tint = tile.second, modifier = Modifier.size(24.dp))
                    else -> Text(
                        if (app == CommuterMapApp.Naver) "N" else "K",
                        style = MaterialTheme.typography.titleMedium,
                        color = tile.second,
                    )
                }
            }
        }
    }
}

// ───────────────────────── 문구 ─────────────────────────

@Composable
private fun durationText(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours > 0 && rest > 0 -> stringResource(R.string.commuter_duration_hours_minutes, hours, rest)
        hours > 0 -> stringResource(R.string.commuter_duration_hours, hours)
        else -> stringResource(R.string.commuter_duration_minutes, rest)
    }
}

@Composable
private fun dayText(day: LocalDate): String {
    val pattern = stringResource(R.string.commuter_date_format)
    val locale = Locale.getDefault()
    return remember(pattern, locale, day) { DateTimeFormatter.ofPattern(pattern, locale).format(day) }
}
