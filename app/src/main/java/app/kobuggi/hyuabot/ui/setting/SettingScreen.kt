package app.kobuggi.hyuabot.ui.setting

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.service.preferences.StartScreen
import app.kobuggi.hyuabot.ui.common.compose.HyuabotIcons
import app.kobuggi.hyuabot.ui.common.compose.IconBadge
import app.kobuggi.hyuabot.ui.common.compose.IconTone
import app.kobuggi.hyuabot.ui.common.compose.SettingsDimens
import app.kobuggi.hyuabot.ui.common.compose.SettingsGroup
import app.kobuggi.hyuabot.ui.common.compose.SettingsRow
import app.kobuggi.hyuabot.ui.common.compose.SettingsScaffold
import app.kobuggi.hyuabot.ui.common.compose.TrailingIcon
import app.kobuggi.hyuabot.ui.common.compose.settingsItemShape

data class SettingState(
    val campusID: Int,
    val theme: String?,
    val startScreen: String,
    val languageName: String,
    val analytics: Boolean,
    val versionName: String,
    /** Android 13+ 는 시스템 앱 언어 화면으로 보낸다. */
    val systemLanguageSettings: Boolean,
)

data class SettingActions(
    val onBack: () -> Unit,
    val onCampus: (Int) -> Unit,
    val onTheme: (String?) -> Unit,
    val onStartScreen: (String) -> Unit,
    val onLanguage: () -> Unit,
    val onAnalytics: (Boolean) -> Unit,
    val onPrivacy: () -> Unit,
    val onHelp: () -> Unit,
    val onResetCoachmarks: () -> Unit,
    val onLicenses: () -> Unit,
    val onVersionLongPress: () -> Unit,
)

@Composable
fun SettingScreen(state: SettingState, actions: SettingActions) {
    SettingsScaffold(stringResource(R.string.setting_title), actions.onBack) { inner ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(inner)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        CampusCard(state.campusID, actions.onCampus)
        SettingsGroup(
            title = stringResource(R.string.setting_section_display),
            modifier = Modifier.padding(top = SettingsDimens.SectionGap),
            items = listOf(
                { _ -> ThemeBlock(state.theme, actions.onTheme) },
                { shape -> StartScreenRow(state.startScreen, shape, actions.onStartScreen) },
                { shape ->
                    SettingsRow(
                        icon = HyuabotIcons.Language,
                        tone = IconTone.Primary,
                        title = stringResource(R.string.language_settings),
                        subtitle = state.languageName,
                        shape = shape,
                        onClick = actions.onLanguage,
                        trailing = {
                            if (state.systemLanguageSettings) {
                                TrailingIcon(HyuabotIcons.OpenInNew, stringResource(R.string.setting_open_in_system))
                            } else {
                                TrailingIcon(HyuabotIcons.ChevronRight)
                            }
                        },
                    )
                },
            ),
        )
        SettingsGroup(
            title = stringResource(R.string.setting_section_privacy),
            modifier = Modifier.padding(top = SettingsDimens.SectionGap),
            items = listOf(
                { shape ->
                    SettingsRow(
                        icon = HyuabotIcons.Analytics,
                        tone = IconTone.Green,
                        title = stringResource(R.string.analytics_settings),
                        subtitle = stringResource(R.string.setting_analytics_summary),
                        shape = shape,
                        onClick = { actions.onAnalytics(!state.analytics) },
                        role = Role.Switch,
                        trailing = {
                            Switch(
                                checked = state.analytics,
                                onCheckedChange = null,
                                thumbContent = if (state.analytics) {
                                    { Icon(HyuabotIcons.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                            )
                        },
                    )
                },
                { shape ->
                    SettingsRow(
                        icon = HyuabotIcons.Shield,
                        tone = IconTone.Green,
                        title = stringResource(R.string.privacy_policy),
                        shape = shape,
                        onClick = actions.onPrivacy,
                        trailing = { TrailingIcon(HyuabotIcons.OpenInNew, stringResource(R.string.setting_open_in_browser)) },
                    )
                },
            ),
        )
        SettingsGroup(
            title = stringResource(R.string.setting_section_help),
            modifier = Modifier.padding(top = SettingsDimens.SectionGap),
            items = listOf(
                { shape ->
                    SettingsRow(
                        icon = HyuabotIcons.Help,
                        tone = IconTone.Neutral,
                        title = stringResource(R.string.help),
                        shape = shape,
                        onClick = actions.onHelp,
                        trailing = { TrailingIcon(HyuabotIcons.OpenInNew, stringResource(R.string.setting_open_in_browser)) },
                    )
                },
                { shape ->
                    SettingsRow(
                        icon = HyuabotIcons.Restore,
                        tone = IconTone.Neutral,
                        title = stringResource(R.string.coachmark_reset_settings),
                        shape = shape,
                        onClick = actions.onResetCoachmarks,
                        trailing = {
                            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(
                                    stringResource(R.string.setting_coachmark_reset_action),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                )
                            }
                        },
                    )
                },
                { shape ->
                    SettingsRow(
                        icon = HyuabotIcons.Licenses,
                        tone = IconTone.Neutral,
                        title = stringResource(R.string.open_source_licenses),
                        shape = shape,
                        onClick = actions.onLicenses,
                        trailing = { TrailingIcon(HyuabotIcons.ChevronRight) },
                    )
                },
            ),
        )
        AppInfoFooter(state.versionName, actions.onVersionLongPress)
    }
    }
}

@Composable
private fun CampusCard(campusID: Int, onCampus: (Int) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp).fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = scheme.surfaceContainerLowest,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconBadge(HyuabotIcons.School, IconTone.Filled)
                Column(verticalArrangement = Arrangement.spacedBy(SettingsDimens.TitleGap)) {
                    Text(
                        stringResource(R.string.setting_campus_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                    Text(
                        stringResource(
                            R.string.setting_campus_title_format,
                            stringResource(if (campusID == 1) R.string.campus_seoul else R.string.campus_erica),
                        ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                CampusSegment(R.string.campus_seoul, selected = campusID == 1, isFirst = true, onClick = { onCampus(1) }, modifier = Modifier.weight(1f))
                CampusSegment(R.string.campus_erica, selected = campusID == 2, isFirst = false, onClick = { onCampus(2) }, modifier = Modifier.weight(1f))
            }
            Text(
                stringResource(R.string.setting_campus_applies),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CampusSegment(label: Int, selected: Boolean, isFirst: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    val outer = 24.dp
    val inner by animateDpAsState(if (selected) 24.dp else 8.dp, label = "inner")
    val shape = if (isFirst) {
        RoundedCornerShape(topStart = outer, bottomStart = outer, topEnd = inner, bottomEnd = inner)
    } else {
        RoundedCornerShape(topStart = inner, bottomStart = inner, topEnd = outer, bottomEnd = outer)
    }
    val finalShape = if (selected) RoundedCornerShape(24.dp) else shape
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp).semantics { this.role = Role.RadioButton; this.selected = selected },
        shape = finalShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) scheme.primary else scheme.surfaceContainerHighest,
            contentColor = if (selected) scheme.onPrimary else scheme.onSurface,
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
    ) {
        if (selected) {
            Icon(HyuabotIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
        }
        Text(
            stringResource(label),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun ThemeBlock(theme: String?, onTheme: (String?) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val options = listOf<Pair<String?, Int>>(
        null to R.string.theme_system,
        "light" to R.string.theme_light,
        "dark" to R.string.theme_dark,
    )
    val currentName = stringResource(options.first { it.first == theme }.second)
    Surface(shape = settingsItemShape(false, false), color = scheme.surfaceContainerLowest, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconBadge(HyuabotIcons.Theme, IconTone.Primary)
                Column(verticalArrangement = Arrangement.spacedBy(SettingsDimens.TitleGap)) {
                    Text(stringResource(R.string.theme_settings), style = MaterialTheme.typography.bodyLarge)
                    Text(currentName, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                }
            }
            Row(modifier = Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                options.forEach { (value, label) ->
                    ThemeTile(value, stringResource(label), selected = value == theme, onClick = { onTheme(value) }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ThemeTile(value: String?, label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    val light = Color(0xFFFFFFFF)
    val dark = Color(0xFF1D2026)
    val screenModifier = when (value) {
        "light" -> Modifier.background(light)
        "dark" -> Modifier.background(dark)
        else -> Modifier.background(Brush.linearGradient(0.5f to light, 0.5f to dark))
    }
    val bar = if (value == "dark") Color(0xFFA5C8FF) else Color(0xFF0E4A84)
    val line = if (value == "dark") Color(0xFF44474E) else Color(0xFFC4C6D0)
    val outer by animateDpAsState(if (selected) 24.dp else 16.dp, label = "tileOuter")
    val innerRadius by animateDpAsState(if (selected) 18.dp else 10.dp, label = "tileInner")
    Column(
        modifier = modifier.selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f / 0.72f)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(outer))
                .background(scheme.surfaceContainerHighest)
                .then(
                    if (selected) Modifier.androidBorder(scheme.primary, 2.dp, outer)
                    else Modifier.androidBorder(scheme.outlineVariant, 1.dp, outer),
                )
                .padding(6.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(innerRadius))
                    .then(screenModifier)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(Modifier.fillMaxWidth(0.6f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(bar))
                Box(Modifier.fillMaxWidth(0.85f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(line))
                Box(Modifier.fillMaxWidth(0.5f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(line))
            }
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(24.dp)
                    .clip(RoundedCornerShape(50))
                    .background(scheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(HyuabotIcons.Check, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(16.dp))
            }
        }
        }
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) scheme.primary else scheme.onSurfaceVariant,
            fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
        )
    }
}

private fun Modifier.androidBorder(color: Color, width: androidx.compose.ui.unit.Dp, radius: androidx.compose.ui.unit.Dp): Modifier =
    this.then(Modifier.border(BorderStroke(width, color), RoundedCornerShape(radius)))

@Composable
private fun StartScreenRow(current: String, shape: androidx.compose.ui.graphics.Shape, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        StartScreen.SHUTTLE to R.string.shuttle_bus,
        StartScreen.BUS to R.string.tabbar_bus,
        StartScreen.SUBWAY to R.string.subway,
        StartScreen.CAFETERIA to R.string.cafeteria,
    )
    val currentLabel = stringResource(options.firstOrNull { it.first == current }?.second ?: R.string.shuttle_bus)
    Box {
        SettingsRow(
            icon = HyuabotIcons.Home,
            tone = IconTone.Primary,
            title = stringResource(R.string.setting_start_screen),
            subtitle = currentLabel,
            shape = shape,
            onClick = { expanded = true },
            trailing = { TrailingIcon(HyuabotIcons.UnfoldMore) },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.width(200.dp),
        ) {
            options.forEach { (key, label) ->
                val selected = key == current
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(label),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else null,
                        )
                    },
                    leadingIcon = {
                        Box(Modifier.size(24.dp)) {
                            if (selected) Icon(HyuabotIcons.Check, contentDescription = null)
                        }
                    },
                    onClick = { expanded = false; onSelect(key) },
                    colors = androidx.compose.material3.MenuDefaults.itemColors(),
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .then(if (selected) Modifier.background(MaterialTheme.colorScheme.primaryContainer) else Modifier),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppInfoFooter(version: String, onLongPress: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
            .combinedClickable(onClick = {}, onLongClick = onLongPress),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleSmall)
        Text(
            stringResource(R.string.setting_version_format, version),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.setting_developer_footer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
