package com.example.scaler_interview_portal

/**
 * Generalized structure of a user slot. UNIX based timestamps are used.
 *
 * @param id: ID of the user.
 * @param startStamp: Start date & time.
 * @param endStamp: End date & time.
 */

data class UserSlot(
    val id : String,
    val startStamp : String,
    val endStamp : String
)
