package com.vickylee.yardsale

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vickylee.yardsale.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    //region Properties
    val TAG = this@SignInActivity.toString()
    private lateinit var binding: ActivitySignInBinding
    //endregion

    //region Android Lifecycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setup view binding
        this.binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // sign in button
        binding.btnSignIn.setOnClickListener {
            // TODO: check user type -> Seller or Buyer
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // sign up button
        binding.btnSignUp.setOnClickListener {
            // navigate to sign up screen
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
    //endregion
}