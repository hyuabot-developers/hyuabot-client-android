package app.kobuggi.hyuabot.service.cafeteria

import androidx.annotation.StringRes
import app.kobuggi.hyuabot.R

data class CafeteriaInfo(
    val seq: Int,
    val campusId: Int,
    @StringRes val nameRes: Int,
    val aliases: List<String>,
)

object CafeteriaCatalog {
    const val CAMPUS_SEOUL = 1
    const val CAMPUS_ERICA = 2

    val all: List<CafeteriaInfo> = listOf(
        CafeteriaInfo(1, CAMPUS_SEOUL, R.string.widget_cafeteria_1, listOf("학생회관", "학관")),
        CafeteriaInfo(2, CAMPUS_SEOUL, R.string.widget_cafeteria_2, listOf("생활과학관", "생과대")),
        CafeteriaInfo(4, CAMPUS_SEOUL, R.string.widget_cafeteria_4, listOf("신소재공학관", "신소재")),
        CafeteriaInfo(6, CAMPUS_SEOUL, R.string.widget_cafeteria_6, listOf("제1생활관", "1생활관")),
        CafeteriaInfo(7, CAMPUS_SEOUL, R.string.widget_cafeteria_7, listOf("제2생활관", "2생활관")),
        CafeteriaInfo(8, CAMPUS_SEOUL, R.string.widget_cafeteria_8, listOf("행원파크", "행원")),
        CafeteriaInfo(11, CAMPUS_ERICA, R.string.widget_cafeteria_11, listOf("교직원식당", "교직원")),
        CafeteriaInfo(12, CAMPUS_ERICA, R.string.widget_cafeteria_12, listOf("학생식당", "학식당")),
        CafeteriaInfo(13, CAMPUS_ERICA, R.string.widget_cafeteria_13, listOf("창의인재원", "창의인재원식당")),
        CafeteriaInfo(14, CAMPUS_ERICA, R.string.widget_cafeteria_14, listOf("푸드코트")),
        CafeteriaInfo(15, CAMPUS_ERICA, R.string.widget_cafeteria_15, listOf("창업보육센터", "창보")),
    )

    fun bySeq(seq: Int): CafeteriaInfo? = all.firstOrNull { it.seq == seq }

    @StringRes
    fun nameRes(seq: Int): Int = bySeq(seq)?.nameRes ?: R.string.widget_cafeteria_1
}
