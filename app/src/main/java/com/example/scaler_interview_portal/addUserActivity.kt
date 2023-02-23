package com.example.scaler_interview_portal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.scaler_interview_portal.databinding.ActivityAddUserBinding
import com.example.scaler_interview_portal.databinding.ActivityMeetingBinding

class addUserActivity : AppCompatActivity() {
    //binding for accesing UI
    private lateinit var binding : ActivityAddUserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //TEXT INPUTS
        val usernameInputL = binding.textInputUsername
        val emailInputL = binding.textInputEmail
        binding.tvCreateInterview.setOnClickListener {
            val username = usernameInputL.editText?.text.toString()
            val email = emailInputL.editText?.text.toString()
            if(constants.userAddValidation(this,username,email)){
                try {
                    //ADDING USER TO DATABASE
                    FirebaseLinkage.userAdd(username,email,this)
                }catch (e : Exception){
                    //SHOWING ERROR IF ANY
                    Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show()

                }
            }else{
                Toast.makeText(this,"Some error ",Toast.LENGTH_SHORT).show()
            }
        }


    }
}