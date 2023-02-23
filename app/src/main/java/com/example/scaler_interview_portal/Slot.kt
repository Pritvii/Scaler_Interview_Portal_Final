package com.sayantanbanerjee.interviewcreationportal.data

/**
 * Generalized structure of a meeting slot. UNIX based timestamps are used.
 *
 * @param startStamp: Start date & time.
 * @param endStamp: End date & time.
 */

data class Slot(
        val startStamp : String,
        val endStamp : String
)
