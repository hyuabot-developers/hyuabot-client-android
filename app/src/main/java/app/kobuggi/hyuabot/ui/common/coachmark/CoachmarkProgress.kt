package app.kobuggi.hyuabot.ui.common.coachmark

/** ViewModel-owned progress; never retains targets or their Activity. */
class CoachmarkProgress {
    var stepIndex: Int = 0
        internal set

    private val shownSteps = mutableSetOf<Int>()

    internal fun recordStepShown(index: Int): Boolean = shownSteps.add(index)
}
