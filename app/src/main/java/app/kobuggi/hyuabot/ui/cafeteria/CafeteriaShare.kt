package app.kobuggi.hyuabot.ui.cafeteria

import android.content.Context
import android.content.Intent
import app.kobuggi.hyuabot.CafeteriaPageQuery
import app.kobuggi.hyuabot.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun shareCafeteriaMenus(
    context: Context,
    date: LocalDateTime?,
    mealType: String,
    cafeteriaList: List<CafeteriaPageQuery.Cafeterium>,
) {
    val mealTypeLabel = mealTypeLabel(context, mealType)
    val mealTypeQuery = mealTypeQuery(mealType)
    val entries = cafeteriaList.mapNotNull { cafeteria ->
        val menus = cafeteria.menus
            .filter { it.type.contains(mealTypeQuery) }
            .distinctBy { it.food }
        if (menus.isEmpty()) return@mapNotNull null

        buildString {
            append(cafeteriaName(context, cafeteria.seq))
            menus.forEach { menu ->
                append("\n- ")
                append(menu.food.toShareMenuName())
                val price = menu.price.toSharePrice()
                if (price.isNotBlank()) {
                    append(" / ")
                    append(price)
                }
            }
        }
    }
    if (entries.isEmpty()) return

    val dateText = (date ?: LocalDateTime.now()).format(DateTimeFormatter.ofPattern("yyyy.MM.dd."))
    val text = context.getString(
        R.string.cafeteria_share_format,
        dateText,
        mealTypeLabel,
        entries.joinToString("\n\n"),
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.cafeteria_share_title)))
}

fun mealTypeLabel(context: Context, type: String): String = when (type) {
    "breakfast" -> context.getString(R.string.cafeteria_tab_breakfast)
    "lunch" -> context.getString(R.string.cafeteria_tab_lunch)
    "dinner" -> context.getString(R.string.cafeteria_tab_dinner)
    else -> ""
}

fun mealTypeQuery(type: String): String = when (type) {
    "breakfast" -> "조식"
    "lunch" -> "중식"
    "dinner" -> "석식"
    else -> type
}

private fun cafeteriaName(context: Context, cafeteriaID: Int): String =
    when (cafeteriaID) {
        1 -> context.getString(R.string.cafeteria_1)
        2 -> context.getString(R.string.cafeteria_2)
        4 -> context.getString(R.string.cafeteria_4)
        6 -> context.getString(R.string.cafeteria_6)
        7 -> context.getString(R.string.cafeteria_7)
        8 -> context.getString(R.string.cafeteria_8)
        11 -> context.getString(R.string.cafeteria_11)
        12 -> context.getString(R.string.cafeteria_12)
        13 -> context.getString(R.string.cafeteria_13)
        14 -> context.getString(R.string.cafeteria_14)
        15 -> context.getString(R.string.cafeteria_15)
        else -> context.getString(R.string.cafeteria_1)
    }

private fun String.toShareMenuName(): String =
    withoutMenuTag()
        .replace("\"", "")
        .replace("“", "")
        .replace("”", "")
        .trim()
        .split(Regex("""\s+"""))
        .firstOrNull()
        .orEmpty()

private fun String.withoutMenuTag(): String =
    replace(Regex("""^\s*\[[^\]]+]\s*"""), "")
        .replace(Regex("""^\s*<[^>]+>\s*"""), "")
        .replace(Regex("""^\s*[\w가-힣]+\)\s*"""), "")
        .trim()

private fun String.toSharePrice(): String {
    val price = trim().replace("\"", "")
    if (price.isBlank()) return ""
    return if (price.endsWith("원")) price else "${price}원"
}
