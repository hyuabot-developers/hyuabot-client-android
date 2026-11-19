package app.kobuggi.hyuabot.ui.commuter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ui.common.compose.HyuabotTheme
import app.kobuggi.hyuabot.ui.common.compose.SettingsScaffold
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** 통학버스. 앱 바와 뒤로만 있는 빈 화면 (설정·연락처와 같은 하위 화면 앱 바). */
@AndroidEntryPoint
class CommuterFragment @Inject constructor() : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { HyuabotTheme { CommuterScreen(onBack = { findNavController().popBackStack() }) } }
        }
}

@Composable
fun CommuterScreen(onBack: () -> Unit) {
    SettingsScaffold(stringResource(R.string.campus_commuter_title), onBack) { }
}
