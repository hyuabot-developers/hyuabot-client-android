package app.kobuggi.hyuabot.ui.menu

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ui.common.compose.HyuabotIcons
import app.kobuggi.hyuabot.ui.common.compose.IconBadge
import app.kobuggi.hyuabot.ui.common.compose.IconTone
import app.kobuggi.hyuabot.ui.common.compose.MorphingSurface
import app.kobuggi.hyuabot.ui.common.compose.SectionLabel
import app.kobuggi.hyuabot.ui.common.compose.SettingsDimens
import app.kobuggi.hyuabot.ui.common.compose.SettingsRow
import app.kobuggi.hyuabot.ui.common.compose.TopLevelScaffold
import app.kobuggi.hyuabot.ui.common.compose.fullLine
import app.kobuggi.hyuabot.ui.common.compose.TrailingIcon
import app.kobuggi.hyuabot.ui.common.compose.settingsItemShape

/** iOS CampusDestination과 같은 목적지 ID (분석 이벤트의 name · destination_id). */
enum class CampusDestination(val id: String) {
    CommuterBus("commuter_bus"),
    Map("map"),
    ReadingRoom("reading_room"),
    Calendar("calendar"),
    Contact("contact"),
    Setting("setting"),
    Inquiry("inquiry"),
    Donate("donate"),
}

private data class CampusTool(
    val destination: CampusDestination,
    val icon: ImageVector,
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
)

private data class SupportRow(
    val destination: CampusDestination,
    val icon: ImageVector,
    val tone: IconTone,
    @StringRes val title: Int,
    val external: Boolean = false,
)

private val tools = listOf(
    CampusTool(CampusDestination.Map, HyuabotIcons.Map, R.string.campus_map_title, R.string.campus_map_subtitle),
    CampusTool(CampusDestination.ReadingRoom, HyuabotIcons.Licenses, R.string.campus_reading_room_title, R.string.campus_reading_room_subtitle),
    CampusTool(CampusDestination.Calendar, HyuabotIcons.Calendar, R.string.campus_calendar_title, R.string.campus_calendar_subtitle),
    CampusTool(CampusDestination.Contact, HyuabotIcons.Call, R.string.campus_contact_title, R.string.campus_contact_subtitle),
)

private val supportRows = listOf(
    SupportRow(CampusDestination.Setting, HyuabotIcons.Settings, IconTone.Neutral, R.string.menu_settings),
    SupportRow(CampusDestination.Inquiry, HyuabotIcons.Chat, IconTone.Primary, R.string.menu_chat),
    SupportRow(CampusDestination.Donate, HyuabotIcons.Favorite, IconTone.Pink, R.string.menu_donate, external = true),
)

private val TileGap = 8.dp

/**
 * 캠퍼스 탭. 통학버스 강조 카드 → 도구 타일 2×2 → 지원 및 설정 행.
 * [onToolsBounds]는 코치마크가 가리킬 "캠퍼스 도구" 영역(루트 좌표)을 알려 준다.
 */
@Composable
fun CampusScreen(
    onSelect: (CampusDestination) -> Unit,
    onToolsBounds: (Rect) -> Unit,
) {
    TopLevelScaffold(
        title = stringResource(R.string.tabbar_campus),
        subtitle = stringResource(R.string.campus_tools_subtitle),
    ) {
        Column(modifier = Modifier.padding(horizontal = SettingsDimens.ScreenPadding)) {
            SectionLabel(stringResource(R.string.campus_tools_title), modifier = Modifier.padding(horizontal = 0.dp))
            Column(modifier = Modifier.onGloballyPositioned { onToolsBounds(it.boundsInRoot()) }) {
                CommuterCard(onClick = { onSelect(CampusDestination.CommuterBus) })
                tools.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.padding(top = TileGap).height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(TileGap),
                    ) {
                        pair.forEach { tool ->
                            ToolTile(tool, onClick = { onSelect(tool.destination) }, modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = 148.dp))
                        }
                    }
                }
            }
            SectionLabel(stringResource(R.string.campus_support_title))
            Column(verticalArrangement = Arrangement.spacedBy(SettingsDimens.GroupGap)) {
                supportRows.forEachIndexed { index, row ->
                    val title = stringResource(row.title)
                    SettingsRow(
                        icon = row.icon,
                        tone = row.tone,
                        title = title,
                        shape = settingsItemShape(isFirst = index == 0, isLast = index == supportRows.lastIndex),
                        minHeight = SettingsDimens.NavRowHeight,
                        onClick = { onSelect(row.destination) },
                        role = Role.Button,
                        trailing = {
                            if (row.external) {
                                TrailingIcon(HyuabotIcons.OpenInNew, stringResource(R.string.campus_donate_external))
                            } else {
                                TrailingIcon(HyuabotIcons.ChevronRight)
                            }
                        },
                    )
                }
            }
        }
    }
}

/** primary 채움 강조 카드: 최소 72 · 모서리 28 · 패딩 12/8/12/16 · 끝 화살표 칸 40. */
@Composable
private fun CommuterCard(onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val title = stringResource(R.string.campus_commuter_title)
    val subtitle = stringResource(R.string.campus_commuter_subtitle)
    val badge = stringResource(R.string.campus_commuter_badge)
    MorphingSurface(
        onClick = onClick,
        radius = 28.dp,
        pressedRadius = 20.dp,
        color = scheme.primary,
        contentColor = scheme.onPrimary,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 72.dp)
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp)
                .clearAndSetSemantics { contentDescription = "$title, $badge, $subtitle" },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SettingsDimens.ContentGap),
        ) {
            IconBadge(HyuabotIcons.Route, IconTone.Inverse)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SettingsDimens.TitleGap)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.bodyLarge.fullLine().copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Box(
                        modifier = Modifier.height(20.dp).clip(RoundedCornerShape(10.dp)).background(scheme.onPrimary).padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            badge,
                            style = MaterialTheme.typography.labelMedium.fullLine().copy(fontWeight = FontWeight.Bold, lineHeight = 20.sp),
                            color = scheme.primary,
                        )
                    }
                }
                Text(subtitle, style = MaterialTheme.typography.bodySmall.fullLine(), color = scheme.primaryContainer)
            }
            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Icon(HyuabotIcons.ChevronRight, contentDescription = null, modifier = Modifier.size(24.dp))
            }
        }
    }
}

/** 도구 타일: 모서리 24 · 패딩 16 · 아이콘 40(글리프 24) → 12 → 제목 16/24 굵게 · 부제 13/18 2줄 고정. */
@Composable
private fun ToolTile(tool: CampusTool, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val title = stringResource(tool.title)
    val subtitle = stringResource(tool.subtitle)
    MorphingSurface(
        onClick = onClick,
        radius = SettingsDimens.OuterRadius,
        pressedRadius = 16.dp,
        color = scheme.surfaceContainerLowest,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(16.dp).clearAndSetSemantics { contentDescription = "$title, $subtitle" },
            verticalArrangement = Arrangement.spacedBy(SettingsDimens.ContentGap),
        ) {
            IconBadge(tool.icon, IconTone.Primary, size = 40.dp, glyph = 24.dp)
            Column(verticalArrangement = Arrangement.spacedBy(SettingsDimens.TitleGap)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge.fullLine().copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall.fullLine(),
                    color = scheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.heightIn(min = 36.dp),
                )
            }
        }
    }
}
