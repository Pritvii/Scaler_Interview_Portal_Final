package com.example.scaler_interview_portal

import android.content.Context
import android.util.Patterns
import android.widget.Toast

class constants {
    // THESE ARE THE CONSTANTS FOR THE ENTIRE APP
    companion object{
        //FUNCTION FOR VALIDATING ARE THE USER DETAILS VALID
        fun userAddValidation(context : Context, username : String, email : String) : Boolean{
            //CHECKING FOR EMPTY USERNAME
            if(username == "") {
                Toast.makeText(context, "Username empty Write proper username", Toast.LENGTH_LONG)
                    .show()
                return false
            }
            //CHECKING FOR EMPTY EMAIL
            if(email == "") {
                Toast.makeText(context, "email empty Write proper email", Toast.LENGTH_LONG)
                    .show()
                return false
            }
            //CHECKING FOR EMAIL PROPER FORMAT
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(context, "Improper Email format", Toast.LENGTH_LONG)
                    .show()
                return false
            }

            return true

        }
    }
}