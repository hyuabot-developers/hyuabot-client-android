package app.kobuggi.hyuabot.ui.setting

import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ui.common.compose.HyuabotTheme
import app.kobuggi.hyuabot.ui.common.compose.SettingsDimens
import app.kobuggi.hyuabot.ui.common.compose.SettingsSectionTitle
import app.kobuggi.hyuabot.ui.common.compose.SettingsScaffold
import app.kobuggi.hyuabot.ui.common.compose.IconTone
import app.kobuggi.hyuabot.ui.common.compose.SettingsRow
import app.kobuggi.hyuabot.ui.common.compose.TrailingIcon
import app.kobuggi.hyuabot.ui.common.compose.HyuabotIcons
import app.kobuggi.hyuabot.ui.common.compose.settingsItemShape
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LicensesFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { HyuabotTheme { LicensesContent(onExit = { findNavController().popBackStack() }) } }
        }
}

@Composable
private fun LicensesContent(onExit: () -> Unit) {
    var selected by remember { mutableStateOf<LibraryLicense?>(null) }
    BackHandler(enabled = selected != null) { selected = null }
    val current = selected
    if (current == null) {
        LicenseList(LicenseCatalog.libraries, onExit) { selected = it }
    } else {
        LicenseDetail(current) { selected = null }
    }
}

@Composable
private fun LicenseList(libraries: List<LibraryLicense>, onBack: () -> Unit, onOpen: (LibraryLicense) -> Unit) {
    val grouped = libraries.groupBy { it.kind }.toList()
    SettingsScaffold(stringResource(R.string.open_source_licenses), onBack) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().navigationBarsPadding(),
            contentPadding = PaddingValues(top = inner.calculateTopPadding(), bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(SettingsDimens.GroupGap),
        ) {
            item {
                Text(
                    stringResource(R.string.licenses_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = SettingsDimens.ScreenPadding).padding(bottom = 4.dp),
                )
            }
            grouped.forEach { (kind, items) ->
                item(key = "title-${kind.name}") {
                    Column(Modifier.padding(horizontal = SettingsDimens.ScreenPadding).padding(top = SettingsDimens.SectionGap - 2.dp)) {
                        SettingsSectionTitle(
                            if (kind == LicenseKind.Terms) stringResource(R.string.licenses_group_other) else kind.title,
                            trailing = stringResource(R.string.licenses_count_format, items.size),
                        )
                    }
                }
                items(items, key = { it.name }) { library ->
                    val index = items.indexOf(library)
                    Column(Modifier.padding(horizontal = SettingsDimens.ScreenPadding)) {
                        SettingsRow(
                            icon = HyuabotIcons.Licenses,
                            tone = IconTone.Neutral,
                            title = library.name,
                            subtitle = "v${library.version}",
                            shape = settingsItemShape(isFirst = false, isLast = index == items.lastIndex),
                            onClick = { onOpen(library) },
                            trailing = { TrailingIcon(HyuabotIcons.ChevronRight) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LicenseDetail(library: LibraryLicense, onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val text = LicenseCatalog.text(library)
    SettingsScaffold(library.name, onBack) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = SettingsDimens.ScreenPadding)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(SettingsDimens.GroupGap),
        ) {
            SettingsSectionTitle(library.kind.title, trailing = "v${library.version}")
            SettingsRow(
                icon = HyuabotIcons.OpenInNew,
                tone = IconTone.Primary,
                title = library.url.removePrefix("https://"),
                shape = settingsItemShape(isFirst = false, isLast = text == null),
                onClick = {
                    runCatching { CustomTabsIntent.Builder().build().launchUrl(context, library.url.toUri()) }
                },
                trailing = { TrailingIcon(HyuabotIcons.OpenInNew, stringResource(R.string.setting_open_in_browser)) },
            )
            if (text != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = settingsItemShape(isFirst = false, isLast = true),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                ) {
                    Text(
                        text,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
