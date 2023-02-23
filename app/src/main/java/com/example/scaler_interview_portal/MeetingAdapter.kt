package com.example.scaler_interview_portal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ValueEventListener
import com.sayantanbanerjee.interviewcreationportal.data.Meeting
import java.text.SimpleDateFormat

class MeetingAdapter(private val context: Context, private val meetings: List<Meeting>,
                     val listener: wxyz
):RecyclerView.Adapter<MeetingAdapter.ViewHolder>() {
    class ViewHolder(view : View):RecyclerView.ViewHolder(view) {
        val meetName = view.findViewById<TextView>(R.id.tv_rv_main_item)!!
        val meetStartTime = view.findViewById<TextView>(R.id.tv_rv_time_start)!!
        val meetEndTime = view.findViewById<TextView>(R.id.tv_rv_time_end)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.rv_main_item, parent, false)
        return ViewHolder(view)
        }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var meetingCurr = meetings[position]
        val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy, hh.mm aa")
        holder.meetName.text = meetingCurr.name.toString()
        val convertedStartDate = simpleDateFormat.format(meetingCurr.slot.startStamp.toLong() * 1000L)
        holder.meetStartTime.text =convertedStartDate.toString()
        val convertedEndDate = simpleDateFormat.format(meetingCurr.slot.endStamp.toLong() * 1000L)
        holder.meetEndTime.text = convertedEndDate.toString()

        holder.itemView.setOnClickListener{
            listener.onclick(meetingCurr,convertedStartDate.toString(),convertedEndDate.toString())

        }
    }

    override fun getItemCount(): Int {
        return meetings.size
    }
    interface wxyz {
        fun onclick(meeting : Meeting , startTime : String , endTime : String)
    }
}