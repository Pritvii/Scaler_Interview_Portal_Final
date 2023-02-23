package com.example.scaler_interview_portal

import android.content.Context
import android.widget.Toast
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sayantanbanerjee.interviewcreationportal.data.Meeting
import com.sayantanbanerjee.interviewcreationportal.data.Slot
//Firebase Usage Functions
class FirebaseLinkage {
    companion object {
        fun userAdd(username: String, mail: String, context: Context) {
            val realTimeDatabaseReference = Firebase.database.reference
            val userId =System.currentTimeMillis().toString()
            val userDataObj = User(userId, username, mail)
            //User added in realtime database
            realTimeDatabaseReference.child("users").child(userId).setValue(userDataObj)
        }

        // To upload a new meeting to firebase realtime database
        fun uploadMeetingToFirebase(
            context: Context,
            meetingName: String,
            selectedUserList: List<User>,
            timestampStart: String,
            timestampEnd: String
        ) {
            val reference = Firebase.database.reference
            // create the Meeting class
            val meetingId = System.currentTimeMillis().toString()
            val meeting = Meeting(meetingId, meetingName, Slot(timestampStart, timestampEnd))
            // Upload meeting class to firebase
            reference.child(context.getString(R.string.meeting)).child(meetingId).setValue(meeting)
            for (users in selectedUserList) {
                reference.child(context.getString(R.string.meeting)).child(meetingId)
                    .child(context.getString(R.string.users)).child(users.userId).child("UID")
                    .setValue(users.userId)
                reference.child(context.getString(R.string.meeting)).child(meetingId)
                    .child(context.getString(R.string.users)).child(users.userId).child("name")
                    .setValue(users.username)
                val slot: Slot = Slot(timestampStart, timestampEnd)
                reference.child(context.getString(R.string.users)).child(users.userId)
                    .child(context.getString(R.string.slots)).child(timestampStart).setValue(slot)
            }
            // Display Toast to user
            Toast.makeText(
                context, context.getString(R.string.meeting_uploaded_to_firebase), Toast.LENGTH_LONG
            ).show()
        }

        fun deleteMeetingWhileUpdating(
            context: Context,
            meetingId: String,
            initialSelectedUsers: ArrayList<String>,
            startTimestamp: String
        ) {
            // remove the meeting details
            val reference = Firebase.database.reference
            reference.child(context.getString(R.string.meeting)).child(meetingId).removeValue()
            // remove the time-slots of the users associated with the meeting
            for (userId in initialSelectedUsers) {
                reference.child(context.getString(R.string.users)).child(userId)
                    .child(context.getString(R.string.slots)).child(startTimestamp).removeValue()
            }
        }
        fun deleteMeeting(
            context: Context,
            meetingId: String,
            selectedUserList: List<User>,
            startTimestamp: String
        ) {
            // remove the meeting details
            val reference = Firebase.database.reference
            reference.child(context.getString(R.string.meeting)).child(meetingId).removeValue()
            // remove the time-slots of the users associated with the meeting
            for (users in selectedUserList) {
                reference.child(context.getString(R.string.users)).child(users.userId)
                    .child(context.getString(R.string.slots)).child(startTimestamp).removeValue()
            }
        }
    }
}