package app.kobuggi.hyuabot.ui.setting

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LanguageSettingDialogViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun setLocaleCodeUpdatesLocaleCode() {
        val viewModel = LanguageSettingDialogViewModel()

        viewModel.setLocaleCode("ko")

        assertEquals("ko", viewModel.localeCode.value)
    }
}
