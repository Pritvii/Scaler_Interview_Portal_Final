package com.example.scaler_interview_portal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scaler_interview_portal.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sayantanbanerjee.interviewcreationportal.data.Meeting
import com.sayantanbanerjee.interviewcreationportal.data.Slot

class MainActivity : AppCompatActivity() , MeetingAdapter.wxyz{
    private lateinit var binding : ActivityMainBinding
    private lateinit var adapter: MeetingAdapter
    private lateinit var recyclerList: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recyclerList = binding.rvMainMeeting as RecyclerView


        binding.tvAddUserMain.setOnClickListener {
            val intent = Intent(this,addUserActivity::class.java)
            startActivity(intent)
        }
        binding.tvCreateInterview.setOnClickListener {
            val intent = Intent(this,meetingActivity::class.java)
            startActivity(intent)
        }

    }

    private fun fetchMeetingList() {
        // Fetching the list from the firebase database
        val reference = Firebase.database.reference
        reference.child(getString(R.string.meeting)).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        // Traversing in the meetings, extracting the data, and inserting in a list.
                        val meetingRooms: MutableList<Meeting> = mutableListOf()
                        for (dataSnapshot in snapshot.children) {
                            val id = dataSnapshot.child("id").value.toString()
                            val name = dataSnapshot.child("name").value.toString()
                            val startTimestamp =
                                dataSnapshot.child("slot").child("startStamp").value.toString()
                            val endTimestamp =
                                dataSnapshot.child("slot").child("endStamp").value.toString()
                            val currentMeetingRoom =
                                Meeting(id, name, Slot(startTimestamp, endTimestamp))
                            meetingRooms.add(currentMeetingRoom)
                        }

                        // After the list is fetched from the server, we set up in the adapter.
                        // The recycler adapter displays the whole list, as card item format.
                        adapter = MeetingAdapter(applicationContext, meetingRooms,this@MainActivity)
                        recyclerList.adapter = adapter
                        recyclerList.layoutManager = LinearLayoutManager(applicationContext)


                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun onResume() {
        super.onResume()
        fetchMeetingList()
    }

    override fun onclick(meeting : Meeting , startTime : String , endTime : String) {
        val intent  = Intent(this,DisplayActivity::class.java)
        intent.putExtra("EDIT", true)
        intent.putExtra("MEETING_ID", meeting.id)
        intent.putExtra("MEETING_NAME", meeting.name)
        intent.putExtra("MEETING_START_TIME", meeting.slot.startStamp)
        intent.putExtra("MEETING_END_TIME", meeting.slot.endStamp)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}