package app.kobuggi.hyuabot.util

import android.content.Context
import app.kobuggi.hyuabot.R

class Subway {
    companion object {
        fun getSubwayStationName(context: Context, name: String) : String {
            val terminalStation = hashMapOf(
                "인천" to R.string.incheon,
                "신인천" to R.string.incheon,
                "오이도" to R.string.oido,
                "청량리" to R.string.cheongnyeongli,
                "왕십리" to R.string.wangsimni,
                "죽전" to R.string.jukjeon,
                "고색" to R.string.gosaek,
                "안산" to R.string.ansan,
                "당고개" to R.string.danggogae,
                "노원" to R.string.nowon,
                "한성대" to R.string.hansung_univ,
                "사당" to R.string.sadang,
                "금정" to R.string.geumjeong,
            )
            return if (terminalStation.containsKey(name)) {
                context.getString(terminalStation[name]!!)
            } else {
                name
            }
        }
    }
}