package app.kobuggi.hyuabot

class ShuttleData {
    companion object StopInfo {
        val stopList = listOf(R.string.dormitory_o, R.string.shuttlecock_o, R.string.station,
            R.string.terminal, R.string.jungang_stn, R.string.shuttlecock_i)
        val stopHeadingMap = hashMapOf(
            R.string.dormitory_o to listOf(R.string.bound_station, R.string.bound_terminal, R.string.bound_jungang),
            R.string.shuttlecock_o to listOf(R.string.bound_station, R.string.bound_terminal, R.string.bound_jungang),
            R.string.station to listOf(R.string.bound_campus, R.string.bound_jungang, R.string.bound_terminal),
            R.string.terminal to listOf(R.string.bound_campus),
            R.string.jungang_stn to listOf(R.string.bound_campus),
            R.string.shuttlecock_i to listOf(R.string.bound_dormitory)
        )

        fun countOfStop() = stopHeadingMap.size
        fun getStopHeading(stop: Int) = stopHeadingMap[stop] ?: listOf()
    }
}