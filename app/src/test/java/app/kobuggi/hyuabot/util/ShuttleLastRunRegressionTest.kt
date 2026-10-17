package app.kobuggi.hyuabot.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ShuttleLastRunRegressionTest {
    private data class Row(val seq: Int)

    @Test
    fun matchesPreviousInlineExpression() {
        for (size in listOf(0, 1, 3, 8)) {
            val list = (1..size).map { Row(it * 10) }
            val expected = setOfNotNull(list.lastOrNull()?.seq)
            assertEquals("size=$size", expected, LastRunJudge.shuttleLastRunSeqs(list) { it.seq })
        }
    }
}
