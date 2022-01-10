package app.kobuggi.hyuabot.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.adapter.ShuttleTimetableCardListAdapter
import app.kobuggi.hyuabot.model.ShuttleItem
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ShuttleTimetableFragment(private val timetable: List<ShuttleItem>) : Fragment() {
    private val now = LocalTime.now()
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shuttle_timetable, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var scrollToIndex = -1
        for(i in timetable.indices){
            if(now.isBefore(LocalTime.parse(timetable[i].time, formatter))){
                scrollToIndex = i
                break
            }
        }

        val shuttleTimetableCardListAdapter = ShuttleTimetableCardListAdapter(timetable, activity as Context)
        val shuttleCardListview = view.findViewById<RecyclerView>(R.id.shuttle_timetable)
        shuttleCardListview.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        shuttleCardListview.adapter = shuttleTimetableCardListAdapter
        if(scrollToIndex >=0){
            shuttleCardListview.scrollToPosition(scrollToIndex)
        }
    }
}