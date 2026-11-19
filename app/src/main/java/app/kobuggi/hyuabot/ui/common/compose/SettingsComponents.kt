package app.kobuggi.hyuabot.ui.common.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.kobuggi.hyuabot.R

/** 설정·라이선스 화면이 함께 쓰는 간격/모양 토큰 (4dp 그리드). */
object SettingsDimens {
    val ScreenPadding = 16.dp
    val GroupGap = 2.dp
    val SectionGap = 16.dp
    val RowPaddingVertical = 8.dp
    val RowPaddingHorizontal = 16.dp
    val ValueRowHeight = 60.dp
    val NavRowHeight = 48.dp
    val BadgeSize = 32.dp
    val BadgeGlyph = 20.dp
    val ContentGap = 12.dp
    val TitleGap = 4.dp
    val SheetInset = 32.dp
    val OuterRadius = 24.dp
    val InnerRadius = 4.dp
}

/** 카드 안 위치에 따른 모서리: 첫 행 위 24, 마지막 행 아래 24, 사이는 4. */
fun settingsItemShape(isFirst: Boolean, isLast: Boolean): Shape = RoundedCornerShape(
    topStart = if (isFirst) SettingsDimens.OuterRadius else SettingsDimens.InnerRadius,
    topEnd = if (isFirst) SettingsDimens.OuterRadius else SettingsDimens.InnerRadius,
    bottomStart = if (isLast) SettingsDimens.OuterRadius else SettingsDimens.InnerRadius,
    bottomEnd = if (isLast) SettingsDimens.OuterRadius else SettingsDimens.InnerRadius,
)

enum class IconTone { Primary, Filled, Green, Neutral, Pink, Inverse }

/** 기본은 지름 32dp 원 안에 20dp 글리프. 큰 타일은 40/24, 강조 카드의 [IconTone.Inverse]는 primary 위에 놓는 배지. */
@Composable
fun IconBadge(
    icon: ImageVector,
    tone: IconTone,
    modifier: Modifier = Modifier,
    size: Dp = SettingsDimens.BadgeSize,
    glyph: Dp = SettingsDimens.BadgeGlyph,
) {
    val scheme = MaterialTheme.colorScheme
    val (container, content) = when (tone) {
        IconTone.Primary -> scheme.primaryContainer to scheme.onPrimaryContainer
        IconTone.Filled -> scheme.primary to scheme.onPrimary
        IconTone.Green -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        IconTone.Neutral -> scheme.secondaryContainer to scheme.onSecondaryContainer
        IconTone.Pink -> colorResource(R.color.md_pink_container) to colorResource(R.color.md_on_pink_container)
        IconTone.Inverse -> scheme.onPrimary to scheme.primary
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(50))
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(glyph))
    }
}

/** 카드 안 첫 행 제목 (48dp · 15/20 굵게). */
@Composable
fun SettingsSectionTitle(title: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Surface(
        modifier = modifier.fillMaxWidth().semantics { heading() },
        shape = settingsItemShape(isFirst = true, isLast = false),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Row(
            modifier = Modifier.heightIn(min = SettingsDimens.NavRowHeight).padding(horizontal = SettingsDimens.RowPaddingHorizontal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            if (trailing != null) {
                Text(trailing, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** 제목 행 + 항목들. 항목은 위치에 맞는 모서리를 받아 그린다. */
@Composable
fun SettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    items: List<@Composable (Shape) -> Unit>,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = SettingsDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(SettingsDimens.GroupGap),
    ) {
        SettingsSectionTitle(title, trailing = trailing)
        items.forEachIndexed { index, item -> item(settingsItemShape(isFirst = false, isLast = index == items.lastIndex)) }
    }
}

/** 설정 한 행: 배지 + 제목(+부제) + 끝 요소. 1줄/2줄 모두 최소 60dp로 통일. */
@Composable
fun SettingsRow(
    icon: ImageVector,
    tone: IconTone,
    title: String,
    shape: Shape,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    role: Role? = null,
    minHeight: Dp = SettingsDimens.ValueRowHeight,
    trailing: (@Composable () -> Unit)? = null,
) {
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .heightIn(min = minHeight)
                .padding(horizontal = SettingsDimens.RowPaddingHorizontal, vertical = SettingsDimens.RowPaddingVertical),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SettingsDimens.ContentGap),
        ) {
            IconBadge(icon, tone)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SettingsDimens.TitleGap)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            trailing?.invoke()
        }
    }
    val color = MaterialTheme.colorScheme.surfaceContainerLowest
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth().then(if (role != null) Modifier.semantics { this.role = role } else Modifier),
            shape = shape,
            color = color,
            content = content,
        )
    } else {
        Surface(modifier = modifier.fillMaxWidth(), shape = shape, color = color, content = content)
    }
}

/** 행 끝의 24dp 보조 아이콘. 장식이면 설명 없이, 의미가 있으면 contentDescription을 준다. */
@Composable
fun TrailingIcon(icon: ImageVector, contentDescription: String? = null) {
    Icon(
        icon,
        contentDescription = contentDescription,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(24.dp).let { if (contentDescription == null) it.clearAndSetSemantics {} else it },
    )
}

/** iOS 큰 제목처럼 스크롤하면 접히는 앱 바 + 본문. 본문은 받은 패딩을 적용해 스스로 스크롤한다. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scheme = MaterialTheme.colorScheme
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = scheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LargeTopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.semantics { heading() }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(HyuabotIcons.Back, contentDescription = stringResource(R.string.setting_back))
                    }
                },
                actions = actions,
                expandedHeight = 112.dp,
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = scheme.surface,
                    scrolledContainerColor = scheme.surface,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
        content = content,
    )
}

/** M3 바텀 시트: 위 모서리 28, 손잡이 위아래 16, 제목·설명 인셋 32. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
    overline: String? = null,
    subtitle: String? = null,
    contentTopGap: Dp = 12.dp,
    headerInset: Dp = SettingsDimens.SheetInset,
    content: @Composable () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { SheetHandle() },
    ) {
        Column(modifier = Modifier.padding(bottom = SettingsDimens.SheetInset)) {
            if (overline != null) {
                Text(
                    overline,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = headerInset, end = headerInset, bottom = 4.dp),
                )
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = headerInset).semantics { heading() },
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = headerInset, end = headerInset, top = 4.dp),
                )
            }
            if (hint != null) {
                Text(
                    hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = headerInset, end = headerInset, top = 4.dp),
                )
            }
            Column(
                modifier = Modifier.padding(start = SettingsDimens.ScreenPadding, end = SettingsDimens.ScreenPadding, top = contentTopGap),
                verticalArrangement = Arrangement.spacedBy(SettingsDimens.GroupGap),
            ) { content() }
        }
    }
}

@Composable
private fun SheetHandle() {
    Box(modifier = Modifier.padding(vertical = 16.dp)) {
        Box(
            Modifier
                .width(32.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.outline),
        )
    }
}

/** 시트 안 라디오 행 (60dp · 선택 시 primaryContainer). */
@Composable
fun SheetRadioRow(
    title: String,
    subtitle: String?,
    selected: Boolean,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = if (selected) scheme.primaryContainer else scheme.surfaceContainerLowest,
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = SettingsDimens.ValueRowHeight)
                .padding(horizontal = SettingsDimens.RowPaddingHorizontal, vertical = SettingsDimens.RowPaddingVertical),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SettingsDimens.ContentGap),
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                modifier = Modifier.padding(2.dp),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SettingsDimens.TitleGap)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                }
            }
        }
    }
}

/** 시트 안 작업 행 (56dp · 24dp 아이콘 @32 · 글자 @72). */
@Composable
fun SheetActionRow(
    icon: ImageVector,
    label: String,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 56.dp)
                .padding(horizontal = SettingsDimens.RowPaddingHorizontal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
