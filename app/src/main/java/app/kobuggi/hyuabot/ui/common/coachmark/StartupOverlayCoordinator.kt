package app.kobuggi.hyuabot.ui.common.coachmark

enum class StartupOverlay { COACHMARK, LANGUAGE, BIRTHDAY, REVIEW, NONE }

fun chooseStartupOverlay(
    coachmarkEligible: Boolean,
    languageEligible: Boolean,
    birthdayEligible: Boolean,
    reviewEligible: Boolean,
): StartupOverlay = when {
    coachmarkEligible -> StartupOverlay.COACHMARK
    languageEligible -> StartupOverlay.LANGUAGE
    birthdayEligible -> StartupOverlay.BIRTHDAY
    reviewEligible -> StartupOverlay.REVIEW
    else -> StartupOverlay.NONE
}
