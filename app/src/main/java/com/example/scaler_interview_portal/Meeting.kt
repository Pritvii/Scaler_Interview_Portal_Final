package com.sayantanbanerjee.interviewcreationportal.data

/**
 * Generalized structure of a meeting.
 *
 * @param id: Unique ID of the meeting.
 * @param name: Name of the meeting.
 * @param slot: Slot (startStamp and endStamp) of the meeting.
 */

data class Meeting(
        val id : String,
        val name : String,
        val slot: Slot,
)
