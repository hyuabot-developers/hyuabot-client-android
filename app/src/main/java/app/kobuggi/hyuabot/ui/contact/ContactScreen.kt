package app.kobuggi.hyuabot.ui.contact

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ui.common.compose.CampusChip
import app.kobuggi.hyuabot.ui.common.compose.EmptyState
import app.kobuggi.hyuabot.ui.common.compose.HyuabotIcons
import app.kobuggi.hyuabot.ui.common.compose.SettingsDimens
import app.kobuggi.hyuabot.ui.common.compose.SettingsScaffold
import app.kobuggi.hyuabot.ui.common.compose.SettingsSectionTitle
import app.kobuggi.hyuabot.ui.common.compose.settingsItemShape

data class ContactActions(
    val onBack: () -> Unit,
    val onCampus: () -> Unit,
    val onQuery: (String) -> Unit,
    val onDepartment: (String?) -> Unit,
    val onReset: () -> Unit,
    val onRetry: () -> Unit,
    val onCall: (ContactEntry) -> Unit,
    val onMore: (ContactEntry) -> Unit,
    val onCopy: (ContactEntry) -> Unit,
    val onShare: (ContactEntry) -> Unit,
)

@Composable
fun ContactScreen(state: ContactUiState, actions: ContactActions) {
    val campusName = stringResource(if (state.campusID == 1) R.string.campus_seoul else R.string.campus_erica)
    val campusDescription = stringResource(R.string.contact_campus_description, campusName)
    SettingsScaffold(
        title = stringResource(R.string.campus_contact_title),
        onBack = actions.onBack,
        actions = {
            CampusChip(
                name = campusName,
                onClick = actions.onCampus,
                modifier = Modifier.semantics { contentDescription = campusDescription },
            )
        },
    ) { inner ->
        Column(modifier = Modifier.fillMaxSize().padding(inner)) {
            ContactSearchField(state.query, actions.onQuery)
            DepartmentChips(state, actions.onDepartment)
            val busy = state.sync is ContactSync.Checking || state.sync is ContactSync.Downloading
            val failedWithData = state.sync == ContactSync.Failed && state.hasEntries
            if (busy || failedWithData) StatusLine(state.sync)
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    state.sections.isNotEmpty() -> ContactList(state, busy || failedWithData, actions)
                    busy && !state.hasEntries -> ContactSkeleton(topGap = 12.dp)
                    state.filtering -> EmptyState(
                        icon = HyuabotIcons.SearchOff,
                        title = stringResource(
                            R.string.contact_empty_title,
                            state.query.trim().ifEmpty {
                                state.department?.takeIf { it != ContactDirectory.OTHER } ?: stringResource(R.string.contact_department_other)
                            },
                        ),
                        description = stringResource(R.string.contact_empty_description),
                        actionLabel = stringResource(R.string.contact_empty_reset),
                        actionIcon = HyuabotIcons.Refresh,
                        onAction = actions.onReset,
                    )
                    !state.hasEntries && state.sync == ContactSync.Failed -> EmptyState(
                        icon = HyuabotIcons.Refresh,
                        title = stringResource(R.string.contact_load_failed_title),
                        description = stringResource(R.string.contact_load_failed_description),
                        actionLabel = stringResource(R.string.contact_retry),
                        actionIcon = HyuabotIcons.Refresh,
                        onAction = actions.onRetry,
                    )
                }
            }
        }
    }
}

/** 검색 바 (56 · 모서리 28 · 아이콘 24 @32 · 글자 @72 · 검색 중 2dp 테두리). */
@Composable
private fun ContactSearchField(query: String, onQuery: (String) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val focus = LocalFocusManager.current
    val shape = RoundedCornerShape(28.dp)
    val searching = query.isNotBlank()
    Row(
        modifier = Modifier
            .padding(horizontal = SettingsDimens.ScreenPadding)
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .background(scheme.surfaceContainerHighest)
            .then(if (searching) Modifier.border(2.dp, scheme.primary, shape) else Modifier)
            .padding(start = 16.dp, end = if (query.isNotEmpty()) 4.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(HyuabotIcons.Search, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        BasicTextField(
            value = query,
            onValueChange = onQuery,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = scheme.onSurface),
            cursorBrush = SolidColor(scheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            stringResource(R.string.contact_search_guide),
                            style = MaterialTheme.typography.bodyLarge,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                    inner()
                }
            },
        )
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQuery("") }) {
                Icon(HyuabotIcons.Close, contentDescription = stringResource(R.string.contact_search_clear), tint = scheme.onSurfaceVariant)
            }
        }
    }
}

/** 부서 필터 칩 (칩 32 · 터치 48 · 간격 8 · 모서리 8 → 선택하면 16 + 체크). */
private const val ALL_KEY = "\u0000all"

@Composable
private fun DepartmentChips(state: ContactUiState, onDepartment: (String?) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val groupLabel = stringResource(R.string.contact_filter_group)
    val items: List<String?> = listOf(null) + state.departments
    LazyRow(
        modifier = Modifier.padding(top = 4.dp).semantics { contentDescription = groupLabel },
        contentPadding = PaddingValues(horizontal = SettingsDimens.ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(items, key = { it ?: ALL_KEY }) { department ->
            val selected = department == state.department
            val radius by animateDpAsState(if (selected) 16.dp else 8.dp, label = "chipRadius")
            val label = when (department) {
                null -> stringResource(R.string.contact_filter_all)
                ContactDirectory.OTHER -> stringResource(R.string.contact_department_other)
                else -> department
            }
            FilterChip(
                selected = selected,
                onClick = { onDepartment(department) },
                label = { Text(label, style = if (selected) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium) },
                leadingIcon = if (selected) {
                    { Icon(HyuabotIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null,
                shape = RoundedCornerShape(radius),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Transparent,
                    labelColor = scheme.onSurfaceVariant,
                    selectedContainerColor = scheme.primaryContainer,
                    selectedLabelColor = scheme.onPrimaryContainer,
                    selectedLeadingIconColor = scheme.onPrimaryContainer,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = scheme.outlineVariant,
                    selectedBorderColor = Color.Transparent,
                ),
            )
        }
    }
}

/** 칩 아래 상태 줄 (내용선 32 · 13/18). */
@Composable
private fun StatusLine(sync: ContactSync) {
    val scheme = MaterialTheme.colorScheme
    val text = when (sync) {
        ContactSync.Checking -> stringResource(R.string.contact_status_checking)
        is ContactSync.Downloading -> stringResource(R.string.contact_status_downloading, sync.version)
        else -> stringResource(R.string.contact_status_failed)
    }
    val failed = sync == ContactSync.Failed
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = if (failed) scheme.error else scheme.primary,
        modifier = Modifier
            .padding(start = 32.dp, end = 32.dp, top = 4.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContactList(state: ContactUiState, statusShown: Boolean, actions: ContactActions) {
    val scheme = MaterialTheme.colorScheme
    val highlight = colorResource(R.color.md_highlight)
    val onHighlight = colorResource(R.color.md_on_highlight)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = SettingsDimens.ScreenPadding,
            end = SettingsDimens.ScreenPadding,
            top = if (statusShown) 12.dp else 4.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(SettingsDimens.GroupGap),
    ) {
        state.sections.forEachIndexed { sectionIndex, section ->
            item(key = "dep-${section.department ?: ""}") {
                SettingsSectionTitle(
                    title = section.department ?: stringResource(R.string.contact_department_other),
                    trailing = pluralStringResource(R.plurals.contact_count, section.rows.size, section.rows.size),
                    modifier = Modifier.padding(top = if (sectionIndex == 0) 0.dp else SettingsDimens.SectionGap - SettingsDimens.GroupGap),
                )
            }
            itemsIndexed(section.rows, key = { _, row -> row.entry.id }) { index, row ->
                ContactRowItem(
                    row = row,
                    shape = settingsItemShape(isFirst = false, isLast = index == section.rows.lastIndex),
                    highlight = highlight,
                    onHighlight = onHighlight,
                    department = section.department,
                    actions = actions,
                )
            }
        }
        if (!state.filtering && !statusShown) {
            item(key = "footer") {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = SettingsDimens.SectionGap - SettingsDimens.GroupGap),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    state.version?.let {
                        Text(stringResource(R.string.contact_footer_info, it), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                    Text(stringResource(R.string.contact_footer_hint), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

/** 연락처 행 (최소 64 · 패딩 8/16 · 제목·번호 간격 4 · 통화 원 40). 탭 = 전화, 길게 = 작업 시트. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContactRowItem(
    row: ContactRow,
    shape: Shape,
    highlight: Color,
    onHighlight: Color,
    department: String?,
    actions: ContactActions,
) {
    val scheme = MaterialTheme.colorScheme
    val entry = row.entry
    val description = stringResource(R.string.contact_row_description, department ?: stringResource(R.string.contact_department_other), entry.title, entry.phone)
    val copyLabel = stringResource(R.string.contact_action_copy_phone)
    val shareLabel = stringResource(R.string.contact_action_share)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(scheme.surfaceContainerLowest)
            .combinedClickable(
                onClickLabel = stringResource(R.string.contact_action_call),
                onClick = { actions.onCall(entry) },
                onLongClick = { actions.onMore(entry) },
            )
            .semantics(mergeDescendants = true) {
                contentDescription = description
                customActions = listOf(
                    CustomAccessibilityAction(copyLabel) { actions.onCopy(entry); true },
                    CustomAccessibilityAction(shareLabel) { actions.onShare(entry); true },
                )
            }
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(highlighted(entry.title, row.titleMatch, highlight, onHighlight), style = MaterialTheme.typography.bodyLarge)
            Text(
                highlighted(entry.phone, row.phoneMatch, highlight, onHighlight),
                style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum", letterSpacing = 0.2.sp),
                color = scheme.onSurfaceVariant,
            )
        }
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(50)).background(scheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(HyuabotIcons.Call, contentDescription = null, tint = scheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
        }
    }
}

private fun highlighted(text: String, range: IntRange?, background: Color, foreground: Color): AnnotatedString =
    buildAnnotatedString {
        if (range == null) {
            append(text)
        } else {
            append(text.substring(0, range.first))
            withStyle(SpanStyle(background = background, color = foreground)) { append(text.substring(range.first, range.last + 1)) }
            append(text.substring(range.last + 1))
        }
    }

/** 처음 실행 스켈레톤: 부서 제목 + 연락처 행 모양 (세그먼트 목록과 같은 모양). */
@Composable
private fun ContactSkeleton(topGap: Dp) {
    val scheme = MaterialTheme.colorScheme
    val pulse by rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 1f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    val widths = listOf(0.7f, 0.55f, 0.8f, 0.6f, 0.75f, 0.5f)
    Column(
        modifier = Modifier
            .padding(start = SettingsDimens.ScreenPadding, end = SettingsDimens.ScreenPadding, top = topGap)
            .alpha(pulse)
            .clearSemantics(),
        verticalArrangement = Arrangement.spacedBy(SettingsDimens.GroupGap),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(settingsItemShape(isFirst = true, isLast = false))
                .background(scheme.surfaceContainerLowest)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.width(96.dp).height(14.dp).clip(RoundedCornerShape(7.dp)).background(scheme.surfaceContainerHighest))
        }
        widths.forEachIndexed { index, fraction ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(settingsItemShape(isFirst = false, isLast = index == widths.lastIndex))
                    .background(scheme.surfaceContainerLowest)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.fillMaxWidth(fraction).height(14.dp).clip(RoundedCornerShape(7.dp)).background(scheme.surfaceContainerHighest))
                    Box(Modifier.fillMaxWidth(0.4f).height(12.dp).clip(RoundedCornerShape(6.dp)).background(scheme.surfaceContainerHighest))
                }
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(50)).background(scheme.surfaceContainerHighest))
            }
        }
    }
}

private fun Modifier.clearSemantics(): Modifier = this.clearAndSetSemantics {}
