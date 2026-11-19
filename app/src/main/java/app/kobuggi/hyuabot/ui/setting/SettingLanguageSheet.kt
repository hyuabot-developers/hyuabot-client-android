package app.kobuggi.hyuabot.ui.setting

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ui.common.compose.SettingsSheet
import app.kobuggi.hyuabot.ui.common.compose.SheetRadioRow
import app.kobuggi.hyuabot.ui.common.compose.settingsItemShape

/** Android 12 이하에서만 쓰는 앱 안 언어 시트. 13+는 시스템 앱 언어 화면으로 보낸다. */
enum class AppLanguage(val code: String, val tag: String, val native: String, val localized: Int, val analyticsName: String) {
    Korean("ko", "ko-KR", "한국어", R.string.language_korean, "korean"),
    English("en", "en-US", "English", R.string.language_english, "english"),
    Japanese("ja", "ja-JP", "日本語", R.string.language_japanese, "japanese"),
    Chinese("zh", "zh-CN", "中文", R.string.language_chinese, "chinese"),
}

@Composable
fun LanguageSheet(currentCode: String, onSelect: (AppLanguage) -> Unit, onDismiss: () -> Unit) {
    SettingsSheet(
        title = stringResource(R.string.language_settings),
        hint = stringResource(R.string.setting_language_sheet_hint),
        onDismiss = onDismiss,
    ) {
        val all = AppLanguage.entries
        all.forEachIndexed { index, language ->
            SheetRadioRow(
                title = language.native,
                subtitle = stringResource(language.localized),
                selected = language.code == currentCode,
                shape = settingsItemShape(isFirst = index == 0, isLast = index == all.lastIndex),
                onClick = { onSelect(language) },
            )
        }
    }
}
