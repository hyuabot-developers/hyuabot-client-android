package app.kobuggi.hyuabot.ui.common

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

/** 앱 안 Custom Tabs로 링크를 열고, 안 되면 기본 브라우저로 연다. */
fun Context.openCustomTab(url: String) {
    runCatching { CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(this, url.toUri()) }
        .onFailure { runCatching { startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }
}
