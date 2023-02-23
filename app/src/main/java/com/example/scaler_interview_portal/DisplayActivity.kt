package com.example.scaler_interview_portal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaler_interview_portal.databinding.ActivityDisplayBinding
import com.example.scaler_interview_portal.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.yarolegovich.lovelydialog.LovelyStandardDialog
import java.text.SimpleDateFormat

class DisplayActivity : AppCompatActivity() , UserAdapter.xyz {
    //BINDING FOR ACCESSING VIEWS
    private  lateinit  var binding  : ActivityDisplayBinding
    //USER LISTS
    private lateinit var selectedUserList: MutableList<User>
    private var selectedUserId: ArrayList<String> = arrayListOf()
    //ADAPTER FOR RECYCLER VIEW TO DISPLAY USERS IN THE MEETING
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //INTENT FOR ACCESSING VARIABLES FROM DIFFERENT ACTIVITIES
        val intent  = intent
        //DETAILS FROM THE MEETING ACTIVITY
        val meetingId = intent.getStringExtra("MEETING_ID")
        val meetingName = intent.getStringExtra("MEETING_NAME")
        val meetingStartTime = intent.getStringExtra("MEETING_START_TIME")
        val meetingEndTime = intent.getStringExtra("MEETING_END_TIME")
        Toast.makeText(this,meetingName.toString(),Toast.LENGTH_SHORT).show()
        binding.tvMeetingRoomNameDisplay.text = meetingName
        //PARSING DATES FOR PROPER USAGE
        val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy, hh.mm aa")
        val convertedStartDate = simpleDateFormat.format(meetingStartTime!!.toLong() * 1000L)
        binding.timestampStartDisplay.text = "Start : $convertedStartDate"
        val convertedEndDate = simpleDateFormat.format(meetingEndTime!!.toLong() * 1000L)
        binding.timeStampEndDisplay.text = "End : $convertedEndDate"
        fetchUserList(meetingId!!)
        binding.editMeetingButton.setOnClickListener {
            //PASSING VARIABLES OF THE MEET
            val intent = Intent(this, meetingActivity::class.java)
            intent.putExtra("EDIT", true)
            intent.putExtra("MEETING_ID", meetingId)
            intent.putExtra("MEETING_NAME", meetingName)
            intent.putExtra("MEETING_START_TIME", meetingStartTime)
            intent.putExtra("MEETING_END_TIME", meetingEndTime)
            intent.putStringArrayListExtra("USER_LIST_ID", selectedUserId)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        binding.deleteMeetingButton.setOnClickListener {
            LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.VERTICAL)
                .setTopColorRes(R.color.black)
                .setButtonsColorRes(R.color.darkRed)
                .setIcon(R.drawable.ic_baseline_delete_forever_24)
                .setTitle(R.string.delete_title)
                .setMessage(R.string.delete_heading)
                .setPositiveButton(
                    R.string.ok,
                    View.OnClickListener {
                        // If network connectivity is present,

                            meetingId.let { meetingId ->
                                // Delete the meeting from firebase reference.
                                FirebaseLinkage.deleteMeeting(
                                    applicationContext,
                                    meetingId, selectedUserList, meetingStartTime
                                )
                            }
                            Toast.makeText(
                                applicationContext,
                                "Meeting successfully deleted!",
                                Toast.LENGTH_SHORT
                            ).show()
                            Handler(Looper.getMainLooper()).postDelayed({
                                // Once deletion is successful, close the activity after 0.5 second.
                                val mainActivityIntent =
                                    Intent(applicationContext, MainActivity::class.java)
                                mainActivityIntent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(mainActivityIntent)
                                finishAndRemoveTask()
                            }, 500)


                    })
                .show()
        }

    }
    //FUNCTION FOR FETCHING USERS LISTS TO DISPLAY IN THE MEETING
    private fun fetchUserList(meetingID: String) {
        val reference = Firebase.database.reference
        reference.child(getString(R.string.meeting)).child(meetingID).child("users")
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {

                            // Traversing in the meetings, extracting the data, and inserting the users in a list.
                            val usersList: MutableList<User> = mutableListOf()
                            for (dataSnapshot in snapshot.children) {
                                val id = dataSnapshot.child("UID").value.toString()
                                val name = dataSnapshot.child("name").value.toString()
                                val currentUser =
                                    User(id, name, "")
                                usersList.add(currentUser)
                                selectedUserId.add(id)
                            }

                            selectedUserList = usersList


                            // After the list is fetched from the server, we set up in the adapter.
                            // The recycler adapter displays the whole list, as card item format.
                            adapter =
                                UserAdapter(applicationContext, selectedUserList,this@DisplayActivity)
                            binding.usersInDisplayList.adapter = adapter
                            binding.usersInDisplayList.layoutManager =
                                LinearLayoutManager(applicationContext)


                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

    }

    override fun onclick(name: String) {
        Toast.makeText(this,"Name : $name", Toast.LENGTH_SHORT).show()
    }
}