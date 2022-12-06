package com.vickylee.yardsale

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vickylee.yardsale.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    //region Properties
    private lateinit var binding: ActivitySignInBinding

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Sign up button
        binding.btnSignUp.setOnClickListener {
            // navigate to sign up screen
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

    }
}