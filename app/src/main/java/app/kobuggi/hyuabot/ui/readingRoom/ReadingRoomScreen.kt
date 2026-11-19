package app.kobuggi.hyuabot.ui.readingRoom

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ui.common.compose.CampusChip
import app.kobuggi.hyuabot.ui.common.compose.ErrorCard
import app.kobuggi.hyuabot.ui.common.compose.HyuabotIcons
import app.kobuggi.hyuabot.ui.common.compose.HyuabotPullToRefresh
import app.kobuggi.hyuabot.ui.common.compose.MorphingSurface
import app.kobuggi.hyuabot.ui.common.compose.SettingsDimens
import app.kobuggi.hyuabot.ui.common.compose.SettingsScaffold
import app.kobuggi.hyuabot.ui.common.compose.SettingsSheet
import app.kobuggi.hyuabot.ui.common.compose.fullLine
import app.kobuggi.hyuabot.ui.common.compose.settingsItemShape
import java.time.LocalDateTime

/** 코치마크가 가리키는 자리. 화면이 위치를 알려 주면 Fragment가 같은 자리에 보이지 않는 뷰를 놓는다. */
enum class ReadingRoomAnchor { Status, Bell, Alarm }

data class ReadingRoomActions(
    val onBack: () -> Unit,
    val onCampus: () -> Unit,
    val onRefresh: () -> Unit,
    val onRetry: () -> Unit,
    val onBell: (Int) -> Unit,
    val onAlarm: (Int) -> Unit,
    val onCancelAlarm: () -> Unit,
    val onSheetPrimary: () -> Unit,
    val onSheetSecondary: () -> Unit,
    val onSheetDismiss: () -> Unit,
    val onMessageShown: () -> Unit,
    val onAnchor: (ReadingRoomAnchor, Rect) -> Unit = { _, _ -> },
)

/** 열람실 화면이 쓰는 간격 토큰 (4dp 그리드). 정렬선: 16 · 32 · 오른쪽 32 (벨 원 끝 = 표시기 끝). */
private object RoomDimens {
    val Touch = 48.dp
    val BellCircle = 40.dp
    val BlockGap = 16.dp
    val NoticeGap = 12.dp
    val ListGap = SettingsDimens.GroupGap
    val RowPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 12.dp, bottom = 16.dp)
    val BarHeight = 6.dp
    val NumberColumn = 56.dp
}

@Composable
fun ReadingRoomScreen(state: ReadingRoomUiState, actions: ReadingRoomActions) {
    val busy = state.load != ReadingRoomLoad.Idle
    val status = when {
        busy -> stringResource(R.string.reading_room_status_loading)
        state.failed -> stringResource(R.string.reading_room_status_failed)
        state.updatedAt != null -> stringResource(R.string.reading_room_status_updated, ReadingRoomLogic.clock(state.updatedAt))
        else -> ""
    }
    val campusName = stringResource(if (state.campus == 1) R.string.campus_seoul else R.string.campus_erica)
    val campusDescription = stringResource(R.string.contact_campus_description, campusName)
    val snackbar = remember { SnackbarHostState() }
    val message = state.message?.let { messageText(it, state) }
    LaunchedEffect(state.message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            actions.onMessageShown()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        SettingsScaffold(
            title = stringResource(R.string.campus_reading_room_title),
            onBack = actions.onBack,
            subtitle = status,
            pullToRefresh = true,
            subtitleLeading = { LiveDot(active = !busy && !state.failed) },
            subtitleModifier = Modifier
                .semantics { liveRegion = LiveRegionMode.Polite }
                .onGloballyPositioned { actions.onAnchor(ReadingRoomAnchor.Status, it.boundsInRoot()) },
            actions = {
                CampusChip(
                    name = campusName,
                    onClick = actions.onCampus,
                    modifier = Modifier.semantics { contentDescription = campusDescription },
                )
                RefreshButton(spinning = busy, onClick = actions.onRefresh)
            },
        ) { padding ->
            HyuabotPullToRefresh(
                isRefreshing = state.load == ReadingRoomLoad.Refresh,
                onRefresh = actions.onRefresh,
                loadingLabel = stringResource(R.string.commuter_loading_a11y),
                modifier = Modifier.padding(padding).fillMaxSize(),
            ) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
                    AlarmCard(state, actions)
                    when {
                        state.load == ReadingRoomLoad.First -> RoomSkeleton()
                        state.failed -> ErrorCard(
                            message = stringResource(R.string.reading_room_status_failed),
                            retryLabel = stringResource(R.string.reading_room_retry),
                            onRetry = actions.onRetry,
                            topGap = RoomDimens.BlockGap,
                        )
                        else -> {
                            RoomList(state, actions)
                            Text(
                                stringResource(R.string.reading_room_notice),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = RoomDimens.NoticeGap),
                            )
                        }
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
        ) { data -> Snackbar(snackbarData = data, shape = RoundedCornerShape(8.dp)) }
    }
    state.sheet?.let { sheet -> PermissionSheet(sheet, state, actions) }
}

// ───────────────────────── 앱 바 · 갱신 줄 ─────────────────────────

/** 갱신 줄 점 8: 정상이면 초록, 불러오는 중·실패면 회색. 깜빡이지 않는다. */
@Composable
private fun LiveDot(active: Boolean) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(if (active) colorResource(R.color.reading_room_good) else MaterialTheme.colorScheme.outline),
    )
}

/** 아이콘 버튼 48 (글리프 24 오른쪽 끝 = 16선). 불러오는 동안 글리프가 돈다. */
@Composable
private fun RefreshButton(spinning: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        val turn = if (spinning) {
            val angle by rememberInfiniteTransition(label = "spin").animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
                label = "spinAngle",
            )
            angle
        } else {
            0f
        }
        Icon(
            HyuabotIcons.Refresh,
            contentDescription = stringResource(R.string.reading_room_refresh_a11y),
            modifier = Modifier.size(24.dp).rotate(turn),
        )
    }
}

// ───────────────────────── 이용 종료 알림 카드 ─────────────────────────

/** 카드 24 · 패딩 16 · 머리 44 → 12 → 버튼 48 (설정되면 안쪽 컨테이너). */
@Composable
private fun AlarmCard(state: ReadingRoomUiState, actions: ReadingRoomActions) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .padding(horizontal = SettingsDimens.ScreenPadding)
            .fillMaxWidth()
            .onGloballyPositioned { actions.onAnchor(ReadingRoomAnchor.Alarm, it.boundsInRoot()) }
            .clip(RoundedCornerShape(SettingsDimens.OuterRadius))
            .background(scheme.surfaceContainerLowest)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.heightIn(min = 44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(scheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(HyuabotIcons.Timer, null, tint = scheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.reading_room_alarm_title),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    stringResource(R.string.reading_room_alarm_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        val alarm = state.alarm
        if (alarm == null) {
            AlarmButtons(onPick = actions.onAlarm)
        } else {
            AlarmSet(alarm, onCancel = actions.onCancelAlarm)
        }
    }
}

/** 연결 버튼 그룹: 높이 48, 간격 2, 바깥 24 / 안쪽 8. 누르는 동안 모서리가 바뀐다. */
@Composable
private fun AlarmButtons(onPick: (Int) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val groupLabel = stringResource(R.string.reading_room_alarm_group)
    Row(
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = groupLabel },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        listOf(3 to R.string.reading_room_alarm_3_hour, 4 to R.string.reading_room_alarm_4_hour).forEachIndexed { index, (hours, label) ->
            val outer = 24.dp
            val inner = 8.dp
            val source = remember { MutableInteractionSource() }
            val pressed by source.collectIsPressedAsState()
            val big by animateDpAsState(if (pressed) inner else outer, spring(0.6f, 400f), label = "outer")
            val shape: Shape = if (index == 0) {
                RoundedCornerShape(topStart = big, bottomStart = big, topEnd = inner, bottomEnd = inner)
            } else {
                RoundedCornerShape(topStart = inner, bottomStart = inner, topEnd = big, bottomEnd = big)
            }
            Surface(
                onClick = { onPick(hours) },
                modifier = Modifier.weight(1f).heightIn(min = RoomDimens.Touch),
                shape = shape,
                color = scheme.primaryContainer,
                contentColor = scheme.onPrimaryContainer,
                interactionSource = source,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    Icon(HyuabotIcons.AddAlarm, null, modifier = Modifier.size(20.dp))
                    Text(stringResource(label), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/** 설정됨: 안쪽 컨테이너 16 · 패딩 12/12/12/16 · 시각 24/32 · 취소 40 (터치 48). 끝 = 카드 안 32선. */
@Composable
private fun AlarmSet(alarm: ReadingRoomAlarm, onCancel: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val time = ReadingRoomLogic.clock(alarm.at)
    val line = stringResource(R.string.reading_room_alarm_at, time)
    val styled = remember(line, time) { highlightTime(line, time) }
    val end = ReadingRoomLogic.clock(ReadingRoomLogic.endAt(alarm.at))
    val detail = if (alarm.hours > 0) {
        stringResource(R.string.reading_room_alarm_detail_hours, alarm.hours, end)
    } else {
        stringResource(R.string.reading_room_alarm_detail_end, end)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(scheme.primaryContainer)
            .padding(start = 16.dp, top = 12.dp, end = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                styled,
                style = MaterialTheme.typography.bodyMedium.fullLine().copy(lineHeight = 32.sp, fontFeatureSettings = "tnum"),
                color = scheme.onPrimaryContainer,
            )
            Text(
                detail,
                style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                color = scheme.onSurfaceVariant,
            )
        }
        val cancelLabel = stringResource(R.string.reading_room_alarm_cancel)
        Box(
            modifier = Modifier
                .requiredHeight(RoomDimens.Touch)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onCancel),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(scheme.surfaceContainerLowest)
                    .padding(start = 12.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(HyuabotIcons.Close, null, tint = colorResource(R.color.reading_room_full), modifier = Modifier.size(18.dp))
                Text(cancelLabel, style = MaterialTheme.typography.labelLarge, color = colorResource(R.color.reading_room_full))
            }
        }
    }
}

/** 문장 안의 시각만 24 굵게 키운다. 어순이 언어마다 달라 시각의 위치를 찾아서 꾸민다. */
private fun highlightTime(line: String, time: String): AnnotatedString = buildAnnotatedString {
    val at = line.indexOf(time)
    if (at < 0) {
        append(line)
        return@buildAnnotatedString
    }
    append(line.substring(0, at))
    withStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)) { append(time) }
    append(line.substring(at + time.length))
}

// ───────────────────────── 좌석 목록 ─────────────────────────

@Composable
private fun RoomList(state: ReadingRoomUiState, actions: ReadingRoomActions) {
    Column(
        modifier = Modifier.padding(start = SettingsDimens.ScreenPadding, end = SettingsDimens.ScreenPadding, top = RoomDimens.BlockGap),
        verticalArrangement = Arrangement.spacedBy(RoomDimens.ListGap),
    ) {
        state.rooms.forEachIndexed { index, room ->
            RoomRow(
                room = room,
                on = room.seq in state.subscribed,
                shape = settingsItemShape(isFirst = index == 0, isLast = index == state.rooms.lastIndex),
                onBell = { actions.onBell(room.seq) },
                onBellBounds = if (index == 0) ({ actions.onAnchor(ReadingRoomAnchor.Bell, it) }) else null,
            )
        }
    }
}

/** 행 패딩 16/12/16/16 · 정보 48 (이름 24 + 4 + 태그 20) · 12 · 표시기 6 = 98. */
@Composable
private fun RoomRow(room: ReadingRoomItem, on: Boolean, shape: Shape, onBell: () -> Unit, onBellBounds: ((Rect) -> Unit)?) {
    val scheme = MaterialTheme.colorScheme
    val label = readingRoomLabel(room)
    val (name, floor) = ReadingRoomLogic.splitName(label)
    val level = ReadingRoomLogic.level(room.available, room.active)
    val shown = ReadingRoomLogic.shownAvailable(room.available)
    val levelName = stringResource(levelNameRes(level))
    val (levelText, levelFill) = levelColors(level)
    val rowDescription = stringResource(R.string.reading_room_row_accessibility, label, shown, room.active, levelName)
    val bellDescription = stringResource(if (on) R.string.reading_room_bell_off_a11y else R.string.reading_room_bell_on_a11y, label)
    val bigText = LocalDensity.current.fontScale >= 1.3f
    Surface(shape = shape, color = scheme.surfaceContainerLowest, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(RoomDimens.RowPadding)) {
            Row(
                modifier = Modifier.heightIn(min = RoomDimens.Touch),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.weight(1f).clearAndSetSemantics { contentDescription = rowDescription },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                name,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                maxLines = if (bigText) 2 else 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            if (floor.isNotEmpty()) {
                                Text(floor, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant, maxLines = 1)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LevelTag(levelName, levelText, levelFill)
                            Text(
                                stringResource(R.string.reading_room_total_seats, room.active),
                                style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                                color = scheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.widthIn(min = RoomDimens.NumberColumn),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Text(
                            shown.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 24.sp, lineHeight = 32.sp, fontFeatureSettings = "tnum"),
                            color = levelText,
                            maxLines = 1,
                        )
                        Text(
                            stringResource(R.string.reading_room_seat_unit),
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier.padding(bottom = 2.dp),
                        )
                    }
                }
                BellToggle(on, bellDescription, onBell, onBellBounds)
            }
            UsageBar(
                usage = ReadingRoomLogic.usage(room.available, room.active),
                active = levelText,
                track = scheme.surfaceContainerHighest,
                modifier = Modifier.padding(top = 12.dp, end = 4.dp),
            )
        }
    }
}

/** 태그 20 · 모서리 10 · 점 6 + 4 · 12/16 굵게. */
@Composable
private fun LevelTag(name: String, text: Color, fill: Color) {
    Row(
        modifier = Modifier
            .heightIn(min = 20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(fill)
            .padding(start = 6.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(text))
        Text(name, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = text, maxLines = 1)
    }
}

/** 벨 토글: 터치 48 · 원 40. 켜면 primary로 채운다. 켜도 꺼도 원형. 원 끝 = 오른쪽 32선. */
@Composable
private fun BellToggle(on: Boolean, description: String, onToggle: () -> Unit, onBounds: ((Rect) -> Unit)?) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(RoomDimens.Touch)
            .let { if (onBounds != null) it.onGloballyPositioned { c -> onBounds(c.boundsInRoot()) } else it }
            .clip(CircleShape)
            .toggleable(value = on, role = Role.Switch, onValueChange = { onToggle() })
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(RoomDimens.BellCircle)
                .clip(CircleShape)
                .background(if (on) scheme.primary else scheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (on) HyuabotIcons.BellFilled else HyuabotIcons.Bell,
                contentDescription = null,
                tint = if (on) scheme.onPrimary else scheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** 사용률 표시기: 두께 6 · 활성–트랙 간격 4 · 끝 모서리 3. */
@Composable
private fun UsageBar(usage: Float, active: Color, track: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().height(RoomDimens.BarHeight).clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (usage > 0f) Box(Modifier.weight(usage).fillMaxSize().clip(RoundedCornerShape(3.dp)).background(active))
        if (usage < 1f) Box(Modifier.weight(1f - usage).fillMaxSize().clip(RoundedCornerShape(3.dp)).background(track))
    }
}

// ───────────────────────── 스켈레톤 ─────────────────────────

@Composable
private fun RoomSkeleton() {
    val scheme = MaterialTheme.colorScheme
    val loading = stringResource(R.string.commuter_loading_a11y)
    val pulse by rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 1f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    val widths = listOf(0.58f, 0.46f, 0.66f, 0.52f)
    Column(
        modifier = Modifier
            .padding(start = SettingsDimens.ScreenPadding, end = SettingsDimens.ScreenPadding, top = RoomDimens.BlockGap)
            .alpha(pulse)
            .semantics { contentDescription = loading },
        verticalArrangement = Arrangement.spacedBy(RoomDimens.ListGap),
    ) {
        widths.forEachIndexed { index, width ->
            Surface(
                shape = settingsItemShape(isFirst = index == 0, isLast = index == widths.lastIndex),
                color = scheme.surfaceContainerLowest,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(RoomDimens.RowPadding)) {
                    Row(
                        modifier = Modifier.heightIn(min = RoomDimens.Touch),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(Modifier.fillMaxWidth(width).height(16.dp).clip(RoundedCornerShape(8.dp)).background(scheme.surfaceContainerHighest))
                            Box(Modifier.fillMaxWidth(0.34f).height(12.dp).clip(RoundedCornerShape(6.dp)).background(scheme.surfaceContainerHighest))
                        }
                        Box(Modifier.size(width = 44.dp, height = 24.dp).clip(RoundedCornerShape(8.dp)).background(scheme.surfaceContainerHighest))
                        Box(Modifier.size(RoomDimens.Touch), contentAlignment = Alignment.Center) {
                            Box(Modifier.size(RoomDimens.BellCircle).clip(CircleShape).background(scheme.surfaceContainerHighest))
                        }
                    }
                    Box(
                        Modifier.padding(top = 12.dp, end = 4.dp).fillMaxWidth().height(RoomDimens.BarHeight)
                            .clip(RoundedCornerShape(3.dp)).background(scheme.surfaceContainerHighest),
                    )
                }
            }
        }
    }
}

// ───────────────────────── 권한 안내 시트 ─────────────────────────

/** M3 모달 바텀 시트: 타일 56 · 제목 22/28 · 단계 행 56 · 버튼 48 · 아래 32. 스크림·뒤로·끌어내리기로 닫는다. */
@Composable
private fun PermissionSheet(sheet: ReadingRoomSheet, state: ReadingRoomUiState, actions: ReadingRoomActions) {
    val scheme = MaterialTheme.colorScheme
    val what = when (val job = state.job) {
        is ReadingRoomJob.Bell -> stringResource(
            R.string.reading_room_what_bell,
            state.rooms.firstOrNull { it.seq == job.seq }?.let { readingRoomLabel(it) }.orEmpty(),
        )
        is ReadingRoomJob.Alarm -> stringResource(R.string.reading_room_what_alarm, job.hours)
        null -> stringResource(R.string.reading_room_what_alarm, 3)
    }
    val content = when (sheet) {
        ReadingRoomSheet.Perm -> SheetContent(
            icon = HyuabotIcons.BellFilled,
            title = stringResource(R.string.reading_room_perm_title),
            body = stringResource(R.string.reading_room_perm_body, what),
            steps = listOf(stringResource(R.string.reading_room_perm_step1), stringResource(R.string.reading_room_perm_step2)),
            primary = stringResource(R.string.reading_room_perm_allow),
            secondary = stringResource(R.string.reading_room_perm_later),
            external = false,
        )
        ReadingRoomSheet.Denied -> SheetContent(
            icon = HyuabotIcons.BellOff,
            title = stringResource(R.string.reading_room_denied_title),
            body = stringResource(R.string.reading_room_denied_body, what),
            steps = listOf(stringResource(R.string.reading_room_denied_step1), stringResource(R.string.reading_room_denied_step2)),
            primary = stringResource(R.string.reading_room_sheet_open_settings),
            secondary = stringResource(R.string.reading_room_sheet_close),
            external = true,
        )
        ReadingRoomSheet.Exact -> SheetContent(
            icon = HyuabotIcons.Alarm,
            title = stringResource(R.string.reading_room_exact_title),
            body = stringResource(R.string.reading_room_exact_body),
            steps = listOf(stringResource(R.string.reading_room_exact_step1), stringResource(R.string.reading_room_exact_step2, what)),
            primary = stringResource(R.string.reading_room_sheet_open_settings),
            secondary = stringResource(R.string.reading_room_sheet_cancel),
            external = true,
        )
    }
    SettingsSheet(
        title = content.title,
        onDismiss = actions.onSheetDismiss,
        subtitle = content.body,
        subtitleStyle = MaterialTheme.typography.bodyMedium.copy(lineBreak = LineBreak.Paragraph.copy(wordBreak = LineBreak.WordBreak.Phrase)),
        contentTopGap = 16.dp,
        leading = {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(20.dp)).background(scheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(content.icon, null, tint = scheme.onPrimaryContainer, modifier = Modifier.size(28.dp))
            }
        },
    ) {
        content.steps.forEachIndexed { index, text ->
            SheetStepRow(index + 1, text, settingsItemShape(isFirst = index == 0, isLast = index == content.steps.lastIndex))
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                onClick = actions.onSheetSecondary,
                modifier = Modifier.weight(1f).height(RoomDimens.Touch),
                shape = CircleShape,
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(content.secondary, style = MaterialTheme.typography.titleSmall, color = scheme.primary, maxLines = 1)
                }
            }
            MorphingSurface(
                onClick = actions.onSheetPrimary,
                radius = 24.dp,
                pressedRadius = 12.dp,
                color = scheme.primary,
                contentColor = scheme.onPrimary,
                modifier = Modifier.weight(1f).height(RoomDimens.Touch),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    if (content.external) Icon(HyuabotIcons.OpenInNew, null, modifier = Modifier.size(18.dp))
                    Text(content.primary, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                }
            }
        }
    }
}

private data class SheetContent(
    val icon: ImageVector,
    val title: String,
    val body: String,
    val steps: List<String>,
    val primary: String,
    val secondary: String,
    val external: Boolean,
)

/** 단계 행 56 · 번호 원 24 · 간격 16. */
@Composable
private fun SheetStepRow(number: Int, text: String, shape: Shape) {
    val scheme = MaterialTheme.colorScheme
    Surface(shape = shape, color = scheme.surfaceContainerLowest, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier.size(24.dp).clip(CircleShape).background(scheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(number.toString(), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = scheme.onPrimaryContainer)
            }
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium.copy(lineBreak = LineBreak.Paragraph.copy(wordBreak = LineBreak.WordBreak.Phrase)),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ───────────────────────── 이름 · 색 · 문구 ─────────────────────────

private fun nameRes(seq: Int): Int? = when (seq) {
    1 -> R.string.reading_room_1
    53 -> R.string.reading_room_53
    54 -> R.string.reading_room_54
    55 -> R.string.reading_room_55
    56 -> R.string.reading_room_56
    61 -> R.string.reading_room_61
    63 -> R.string.reading_room_63
    131 -> R.string.reading_room_131
    132 -> R.string.reading_room_132
    else -> null
}

/** 앱 문자열(reading_room_<seq>)을 먼저 쓰고, 모르는 열람실은 서버가 준 이름을 쓴다. */
@Composable
private fun readingRoomLabel(room: ReadingRoomItem): String = nameRes(room.seq)?.let { stringResource(it) } ?: room.name

private fun levelNameRes(level: SeatLevel): Int = when (level) {
    SeatLevel.Good -> R.string.reading_room_level_good
    SeatLevel.Mid -> R.string.reading_room_level_mid
    SeatLevel.Busy -> R.string.reading_room_level_busy
    SeatLevel.Full -> R.string.reading_room_level_full
}

/** (글자·표시기 색, 태그 배경색). 주간·야간 모두 글자 대비 4.5:1 이상. */
@Composable
private fun levelColors(level: SeatLevel): Pair<Color, Color> = when (level) {
    SeatLevel.Good -> colorResource(R.color.reading_room_good) to colorResource(R.color.reading_room_good_fill)
    SeatLevel.Mid -> colorResource(R.color.reading_room_mid) to colorResource(R.color.reading_room_mid_fill)
    SeatLevel.Busy -> colorResource(R.color.reading_room_busy) to colorResource(R.color.reading_room_busy_fill)
    SeatLevel.Full -> colorResource(R.color.reading_room_full) to colorResource(R.color.reading_room_full_fill)
}

@Composable
private fun messageText(message: ReadingRoomMessage, state: ReadingRoomUiState): String {
    val context = LocalContext.current
    return when (message) {
        is ReadingRoomMessage.Subscribed -> {
            val label = state.rooms.firstOrNull { it.seq == message.seq }?.let { room ->
                nameRes(room.seq)?.let { context.getString(it) } ?: room.name
            }.orEmpty()
            context.getString(if (message.on) R.string.reading_room_snack_subscribed else R.string.reading_room_snack_unsubscribed, label)
        }
        is ReadingRoomMessage.SubscribeFailed ->
            context.getString(if (message.on) R.string.reading_room_noti_on_error else R.string.reading_room_noti_off_error)
        is ReadingRoomMessage.AlarmScheduled -> context.getString(R.string.reading_room_snack_alarm_scheduled, ReadingRoomLogic.clock(message.at))
        ReadingRoomMessage.AlarmCancelled -> context.getString(R.string.reading_room_snack_alarm_cancelled)
        ReadingRoomMessage.PermissionDenied -> context.getString(R.string.reading_room_noti_denied)
    }
}
