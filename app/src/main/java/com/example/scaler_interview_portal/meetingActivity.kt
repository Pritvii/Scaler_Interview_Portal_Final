package com.example.scaler_interview_portal

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import  javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaler_interview_portal.databinding.ActivityMainBinding
import com.example.scaler_interview_portal.databinding.ActivityMeetingBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.yarolegovich.lovelydialog.LovelyChoiceDialog
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class meetingActivity : AppCompatActivity() , DatePickerDialog.OnDateSetListener ,
    TimePickerDialog.OnTimeSetListener,UserAdapter.xyz{
    private lateinit var binding : ActivityMeetingBinding
    private lateinit var adapter: UserAdapter


    var startTimeClicked = false
    var yearChosen = -1
    var monthChosen = -1
    var dayChosen = -1
    var startHour = -1
    var startMinute = -1
    var endHour = -1
    var endMinute = -1
    var meetingId = ""
    var timeStampStart = ""
    var timeStampEnd = ""
    var initialStartTimestamp = ""
    var initialEndTimestamp = ""

    var userTimeSlots: MutableList<UserSlot> = mutableListOf()
    var selectedUserList: MutableList<User> = mutableListOf()
    var isUpdate = false
    var initialSelectedUsers: List<User> = emptyList()

    private lateinit var initialSelectedId: ArrayList<String>




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeetingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent

        val isEditingVersion = intent.getBooleanExtra("EDIT", false)

        if(isEditingVersion){
            isUpdate = true
            meetingId = intent.getStringExtra("MEETING_ID").toString()
            val meetingName = intent.getStringExtra("MEETING_NAME")
            val meetingStartTime = intent.getStringExtra("MEETING_START_TIME")
            val meetingEndTime = intent.getStringExtra("MEETING_END_TIME")
            initialSelectedId = intent.getStringArrayListExtra("USER_LIST_ID")!!
            updateViewModify(meetingName!!, meetingStartTime!!, meetingEndTime!!)

        }

        binding.chooseDatePicker.setOnClickListener {
            val calendarDate: Calendar = Calendar.getInstance()
            val datePickerDialog =
                DatePickerDialog(
                    this,
                    this,
                    calendarDate.get(Calendar.YEAR),
                    calendarDate.get(Calendar.MONTH),
                    calendarDate.get(Calendar.DAY_OF_MONTH)
                )
            datePickerDialog.show()
        }

        binding.chooseStartTimePicker.setOnClickListener {
            if (dayChosen == -1) {
                Toast.makeText(
                    this,
                    "First Select Date before selecting start time!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                startTimeClicked = true
                val calendarStartTime: Calendar = Calendar.getInstance()
                val timePickerDialog = TimePickerDialog(
                    this,
                    this,
                    calendarStartTime.get(Calendar.HOUR),
                    calendarStartTime.get(Calendar.MINUTE),
                    true
                )
                timePickerDialog.show()
            }
        }

        binding.chooseEndTimePicker.setOnClickListener {
            if (startHour == -1) {
                Toast.makeText(
                    this,
                    "Choose Start Time before choosing End Time!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                startTimeClicked = false
                val calendarEndTime: Calendar = Calendar.getInstance()
                val timePickerDialog = TimePickerDialog(
                    this,
                    this,
                    calendarEndTime.get(Calendar.HOUR),
                    calendarEndTime.get(Calendar.MINUTE),
                    true
                )
                timePickerDialog.show()
            }
        }

        binding.addUserToMeetingButton.setOnClickListener {
            fetchUsersList()
        }

        binding.addMeetingButton.setOnClickListener {
            if (validationOfFields()) {
                    // first convert the date and time fields to EPOCH
                    parseDateTimeToTimeStamp()
                    // If network connectivity present, first check if any of the contact gets collided with the given timestamp.
                    if (!collisionCheck(selectedUserList)) {
                        // If no collision present, you are good to go.
                        // If it is update, delete the previous stored meeting info,
                        if (isUpdate) {
                            FirebaseLinkage.deleteMeetingWhileUpdating(
                                this,
                                meetingId,
                                initialSelectedId,
                                initialStartTimestamp
                            )
                        }
                        // Add the new info to the server
                        val nameOfMeeting = binding?.nameOfMeeting.editText?.text.toString()
                        FirebaseLinkage.uploadMeetingToFirebase(
                            this,
                            nameOfMeeting,
                            selectedUserList,
                            timeStampStart,
                            timeStampEnd
                        )

                        Handler(Looper.getMainLooper()).postDelayed({
                            // close the activity after 0.5 second
                            val mainActivityIntent =
                                Intent(applicationContext, MainActivity::class.java)
                            mainActivityIntent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(mainActivityIntent)
                            finishAndRemoveTask()
                        }, 500)
                    } else {
                        Toast.makeText(this, getString(R.string.collision_found), Toast.LENGTH_LONG)
                            .show()
                    }
            }
        }
    }

    private fun updateViewModify(name: String, meetingStartTime: String, meetingEndTime: String) {
        binding.addMeetingButton.text = getString(R.string.UPDATE_MEETING)
        binding.insertOrUpdateDisplay.text = getString(R.string.UPDATE_A_MEETING)
        binding.nameOfMeeting.editText?.setText(name)
        initialStartTimestamp = meetingStartTime
        initialEndTimestamp = meetingEndTime
        val dayFormat = SimpleDateFormat("dd")
        dayChosen = dayFormat.format(meetingStartTime.toLong() * 1000L).toInt()
        val monthFormat = SimpleDateFormat("MM")
        monthChosen = monthFormat.format(meetingStartTime.toLong() * 1000L).toInt()
        val yearFormat = SimpleDateFormat("yyyy")
        yearChosen = yearFormat.format(meetingStartTime.toLong() * 1000L).toInt()
        val hourFormat = SimpleDateFormat("HH")
        startHour = hourFormat.format(meetingStartTime.toLong() * 1000L).toInt()
        endHour = hourFormat.format(meetingEndTime.toLong() * 1000L).toInt()
        val minuteFormat = SimpleDateFormat("mm")
        startMinute = minuteFormat.format(meetingStartTime.toLong() * 1000L).toInt()
        endMinute = minuteFormat.format(meetingEndTime.toLong() * 1000L).toInt()
        val dateText: String = "DATE : $dayChosen  /  $monthChosen  /  $yearChosen"
        binding.chooseDatePicker.text = dateText
        val startTimeText: String = "$startHour : $startMinute"
        binding.chooseStartTimePicker.text = "START : $startTimeText"
        val endTimeText: String = "$endHour : $endMinute"
        binding.chooseEndTimePicker.text = "END : $endTimeText"
        fetchInitialUserList(meetingId)
    }

    private fun fetchInitialUserList(meetingID: String) {
        userTimeSlots.clear()
        val reference = Firebase.database.reference
        reference.child(getString(R.string.meeting)).child(meetingID).child("users")
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {

                            // Traversing in the meetings, extracting the data, and inserting in a list.
                            val usersList: MutableList<User> = mutableListOf()
                            for (dataSnapshot in snapshot.children) {
                                val id = dataSnapshot.child("UID").value.toString()
                                val name = dataSnapshot.child("name").value.toString()
                                val currentUser =
                                    User(id, name, "")
                                usersList.add(currentUser)

                                // Extracting the time-slots also of the users.
                                for (timeSnapshot in dataSnapshot.child("slots").children) {
                                    val start = timeSnapshot.child("startStamp").value.toString()
                                    val end = timeSnapshot.child("endStamp").value.toString()
                                    val userSlot = UserSlot(id, start, end)
                                    userTimeSlots.add(userSlot)
                                }
                            }

                            // After the list is fetched from the server, we set up in the adapter.
                            // The recycler adapter displays the whole list, as card item format.
                            selectedUserList = usersList
                            initialSelectedUsers = usersList
                            adapter = UserAdapter(applicationContext, selectedUserList,this@meetingActivity)
                            binding.usersInInterview.adapter = adapter
                            binding.usersInInterview.layoutManager = LinearLayoutManager(applicationContext)

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

    }


    private fun fetchUsersList() {
        userTimeSlots.clear()
        val reference = Firebase.database.reference
        reference.child(getString(R.string.users)).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        // Traversing in the meetings, extracting the data, and inserting in a list.
                        val usersList: MutableList<User> = mutableListOf()
                        val usersNameList: MutableList<String> = mutableListOf()
                        for (dataSnapshot in snapshot.children) {
                            val id = dataSnapshot.child("userId").value.toString()
                            val name = dataSnapshot.child("username").value.toString()
                            val email = dataSnapshot.child("email").value.toString()
                            val currentUser =
                                User(id, name, email)
                            usersList.add(currentUser)
                            usersNameList.add(name)

                            for (timeSnapshot in dataSnapshot.child("slots").children) {
                                val start = timeSnapshot.child("startStamp").value.toString()
                                val end = timeSnapshot.child("endStamp").value.toString()
                                val userSlot = UserSlot(id, start, end)
                                userTimeSlots.add(userSlot)
                            }
                        }

                        // Displaying the checkbox dialog based on the fetched list
                        dialog(usersNameList, usersList)

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun validationOfFields(): Boolean {

        // Name field kept as blank verification
        val nameOfMeeting = binding?.nameOfMeeting.editText?.text.toString()
        if (nameOfMeeting == "") {
            Toast.makeText(
                this,
                "Choose Name!",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        // Field kept blank Verification
        if (yearChosen == -1 || startHour == -1 || endHour == -1) {
            Toast.makeText(
                this,
                "Recheck all Date and Time fields!",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        // Start Date cannot be lesser than Current Date Verification
        val calenderCurrent = Calendar.getInstance()
        val currentYear = calenderCurrent.get(Calendar.YEAR)
        var currentMonth = calenderCurrent.get(Calendar.MONTH)
        currentMonth++
        val currentDay = calenderCurrent.get(Calendar.DAY_OF_MONTH)
        if (yearChosen < currentYear || (yearChosen == currentYear && monthChosen < currentMonth) || (yearChosen == currentYear && monthChosen == currentMonth && dayChosen < currentDay)) {
            Toast.makeText(
                this,
                "Start Date cannot be lesser than Current Date!",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        // Start Time cannot be lesser than Current Time Verification
        val hourCurrent = calenderCurrent.get(Calendar.HOUR_OF_DAY)
        val minuteCurrent = calenderCurrent.get(Calendar.MINUTE)
        if (yearChosen == currentYear && monthChosen == currentMonth && dayChosen == currentDay) {
            if (startHour < hourCurrent || (hourCurrent == startHour && startMinute < minuteCurrent)) {
                Toast.makeText(
                    this,
                    "Start Time cannot be lesser than Current Time!",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        }

        // Selected user list to the meeting from the whole user list is less than 2 verification
        if (selectedUserList.size <= 1) {
            Toast.makeText(
                this,
                "Minimum participants allowed is Two!",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        // all verifications are perfect.
        return true
    }

    private fun parseDateTimeToTimeStamp() {

        val dateTimeStart =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDateTime.of(yearChosen, monthChosen, dayChosen, startHour, startMinute, 0)
            } else {
                TODO("VERSION.SDK_INT < O")
            }
        timeStampStart =
            dateTimeStart.atZone(ZoneOffset.ofHoursMinutes(5, 30)).toEpochSecond().toString()

        val dateTimeEnd =
            LocalDateTime.of(yearChosen, monthChosen, dayChosen, endHour, endMinute, 0)
        timeStampEnd =
            dateTimeEnd.atZone(ZoneOffset.ofHoursMinutes(5, 30)).toEpochSecond().toString()

    }

    private fun collisionCheck(selectedUserList: MutableList<User>): Boolean {

        // first we build a set for all the selected users list
        val setsOfChosenID = mutableSetOf<String>()
        for (users in selectedUserList) {
            setsOfChosenID.add(users.userId)
        }

        // next we build a set for all the users who were already present in the meeting beforehand
        // (applied for update case only, not for insert)
        val setsOfAlreadyRegisteredID = mutableSetOf<String>()
        if (isUpdate) {
            for (userId in initialSelectedId) {
                setsOfAlreadyRegisteredID.add(userId)
            }
        }

        // extracting the meeting start and end timestamp
        val meetingStart = timeStampStart.toLong()
        val meetingEnd = timeStampEnd.toLong()

        val userActualTimeSlotsToCheck: MutableList<UserSlot> = mutableListOf()

        // we extract the time slots of all the people ->
        // except the time slots for people who were already in the interview,
        // and their time slots are in the range [initial start time , initial end tme]
        for (timeSlots in userTimeSlots) {
            if (isUpdate) {
                if (setsOfAlreadyRegisteredID.contains(timeSlots.id)) {
                    val slotStart = timeSlots.startStamp
                    val slotEnd = timeSlots.endStamp
                    if (slotStart == initialStartTimestamp && slotEnd == initialEndTimestamp) {
                        continue
                    } else {
                        userActualTimeSlotsToCheck.add(timeSlots)
                    }
                } else {
                    userActualTimeSlotsToCheck.add(timeSlots)
                }
            } else {
                userActualTimeSlotsToCheck.add(timeSlots)
            }
        }

        // now in the selected time slots, check if the current meeting time collides with any one of them.
        for (timeSlots in userActualTimeSlotsToCheck) {
            if (setsOfChosenID.contains(timeSlots.id)) {
                val slotStart = timeSlots.startStamp.toLong()
                val slotEnd = timeSlots.endStamp.toLong()
                if (!(meetingEnd < slotStart || meetingStart > slotEnd)) {
                    // collision found
                    return true
                }
            }
        }

        // no collision found
        return false
    }

    private fun dialog(usersNameList: MutableList<String>, usersList: List<User>) {
        LovelyChoiceDialog(this)
            .setTopColorRes(R.color.darkRed)
            .setTitle(R.string.selectContact)
            .setIcon(R.drawable.ic_baseline_person_add_alt_1_24)
            .setItemsMultiChoice(
                usersNameList
            ) { positions, items ->
                selectedUserList.clear()
                for (pos in positions) {
                    selectedUserList.add(usersList.get(pos))
                }

                // Based on the chosen list, the adapter of the recycler view is updated.
                adapter =
                    UserAdapter(applicationContext, selectedUserList,this)
                binding?.usersInInterview.adapter= adapter
                binding?.usersInInterview.layoutManager = LinearLayoutManager(applicationContext)

                Toast.makeText(
                    this,
                    "Contact List Updated",
                    Toast.LENGTH_LONG
                )
                    .show()
            }
            .setConfirmButtonText(R.string.confirm)
            .show()

    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, date: Int) {
        val calenderCurrent = Calendar.getInstance()
        val currentYear = calenderCurrent.get(Calendar.YEAR)
        val currentMonth = calenderCurrent.get(Calendar.MONTH)
        val currentDay = calenderCurrent.get(Calendar.DAY_OF_MONTH)

        // Start Date cannot be lesser than Current Date Verification
        if (year < currentYear || (year == currentYear && month < currentMonth) || (year == currentYear && month == currentMonth && date < currentDay)) {
            Toast.makeText(
                this,
                "Start Date cannot be lesser than Current Date!",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        yearChosen = year
        monthChosen = month + 1
        dayChosen = date

        val dateText: String = "DATE : $date  /  $monthChosen  /  $year"
        binding?.chooseDatePicker.text = dateText
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val timeText: String = "$hourOfDay : $minute"
        if (startTimeClicked) {
            if (endHour != -1) {
                // Start Time cannot be lesser than End Time verification
                if (endHour < hourOfDay || (endHour == hourOfDay && endMinute <= minute)) {
                    Toast.makeText(
                        this,
                        "Start Time cannot be more than End Time!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    startHour = hourOfDay
                    startMinute = minute
                    binding?.chooseStartTimePicker.text = "START : $timeText"
                }
            } else {
                val calenderCurrent = Calendar.getInstance()
                val currentYear = calenderCurrent.get(Calendar.YEAR)
                var currentMonth = calenderCurrent.get(Calendar.MONTH)
                currentMonth++
                val currentDay = calenderCurrent.get(Calendar.DAY_OF_MONTH)
                val hourCurrent = calenderCurrent.get(Calendar.HOUR_OF_DAY)
                val minuteCurrent = calenderCurrent.get(Calendar.MINUTE)

                // Start Time cannot be lesser than Current Time verification
                if (yearChosen == currentYear && monthChosen == currentMonth && dayChosen == currentDay) {
                    if (hourOfDay < hourCurrent || (hourCurrent == hourOfDay && minute < minuteCurrent)) {
                        Toast.makeText(
                            this,
                            "Start Time cannot be lesser than Current Time!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        startHour = hourOfDay
                        startMinute = minute
                        binding?.chooseStartTimePicker.text = "START : $timeText"
                    }
                } else {
                    startHour = hourOfDay
                    startMinute = minute
                    binding?.chooseStartTimePicker?.text= "START : $timeText"
                }
            }

        } else {
            // Start Time cannot be greater than End Time verification
            if (startHour > hourOfDay || (startHour == hourOfDay && startMinute >= minute)) {
                Toast.makeText(
                    this,
                    "Start Time cannot be greater than End Time!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                endHour = hourOfDay
                endMinute = minute
               binding?.chooseEndTimePicker.text = "END : $timeText"
            }

        }

    }

    override fun onclick(name: String) {
        Toast.makeText(this,name,Toast.LENGTH_SHORT).show()
    }
}