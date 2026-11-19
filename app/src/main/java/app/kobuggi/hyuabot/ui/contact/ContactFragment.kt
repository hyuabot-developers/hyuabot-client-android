package app.kobuggi.hyuabot.ui.contact

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ui.common.compose.HyuabotIcons
import app.kobuggi.hyuabot.ui.common.compose.HyuabotTheme
import app.kobuggi.hyuabot.ui.common.compose.SettingsSheet
import app.kobuggi.hyuabot.ui.common.compose.SheetActionRow
import app.kobuggi.hyuabot.ui.common.compose.settingsItemShape
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ContactFragment @Inject constructor() : Fragment() {
    private val viewModel by viewModels<ContactViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { HyuabotTheme { ContactContent() } }
        }

    @Composable
    private fun ContactContent() {
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        var sheetEntry by remember { mutableStateOf<ContactEntry?>(null) }
        val snackbar = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val copiedMessage = stringResource(R.string.contact_phone_copied)
        val copy: (ContactEntry) -> Unit = { entry ->
            copyPhone(entry)
            // Android 13+ 는 시스템이 클립보드 알림을 직접 보여 줘서 겹치지 않게 뺀다.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                scope.launch { snackbar.showSnackbar(copiedMessage) }
            }
        }
        val actions = ContactActions(
            onBack = { findNavController().popBackStack() },
            onCampus = { findNavController().navigate(R.id.settingFragment) },
            onQuery = viewModel::setQuery,
            onDepartment = viewModel::selectDepartment,
            onReset = viewModel::resetFilters,
            onRetry = viewModel::refresh,
            onCall = { entry ->
                AnalyticsManager.logSelect(AnalyticsItem.CONTACT_SELECT_ROW, type = AnalyticsContentType.LIST_ITEM, name = entry.name)
                dial(entry)
            },
            onMore = { sheetEntry = it },
            onCopy = copy,
            onShare = ::share,
        )
        Box {
            ContactScreen(state, actions)
            SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
            sheetEntry?.let { entry ->
                val dismiss = { sheetEntry = null }
                SettingsSheet(
                    title = entry.title,
                    overline = entry.department,
                    subtitle = entry.phone,
                    contentTopGap = 8.dp,
                    onDismiss = dismiss,
                ) {
                    val rows = listOf(
                        Triple(HyuabotIcons.Call, R.string.contact_action_call) { dial(entry) },
                        Triple(HyuabotIcons.Copy, R.string.contact_action_copy_phone) { copy(entry) },
                        Triple(HyuabotIcons.Share, R.string.contact_action_share) { share(entry) },
                    )
                    rows.forEachIndexed { index, (icon, label, action) ->
                        SheetActionRow(
                            icon = icon,
                            label = stringResource(label),
                            shape = settingsItemShape(isFirst = index == 0, isLast = index == rows.lastIndex),
                            onClick = { dismiss(); action() },
                        )
                    }
                }
            }
        }
    }

    private fun dial(entry: ContactEntry) {
        startActivity(Intent(Intent.ACTION_DIAL, "tel:${entry.phone}".toUri()))
    }

    private fun copyPhone(entry: ContactEntry) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(entry.name, entry.phone))
    }

    private fun share(entry: ContactEntry) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.contact_share_format, entry.name, entry.phone))
        }
        startActivity(Intent.createChooser(intent, getString(R.string.contact_action_share)))
    }
}
