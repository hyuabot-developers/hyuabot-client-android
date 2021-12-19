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

class ShuttleTimetableFragment(private val timetable: List<ShuttleItem>) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shuttle_timetable, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val shuttleTimetableCardListAdapter = ShuttleTimetableCardListAdapter(timetable, activity as Context)
        val shuttleCardListview = view.findViewById<RecyclerView>(R.id.shuttle_timetable)
        shuttleCardListview.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        shuttleCardListview.adapter = shuttleTimetableCardListAdapter
    }
}