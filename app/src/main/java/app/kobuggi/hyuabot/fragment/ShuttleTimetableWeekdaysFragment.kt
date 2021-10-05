package app.kobuggi.hyuabot.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.kobuggi.hyuabot.R

class ShuttleTimetableWeekdaysFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shuttle_timetable_weekdays, container, false)
    }
}