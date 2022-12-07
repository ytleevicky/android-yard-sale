package com.vickylee.yardsale

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.auth.FirebaseAuth
import com.vickylee.yardsale.data.UserRepository
import com.vickylee.yardsale.databinding.ActivitySignInBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity() {

    //region Properties
    val TAG = this@SignInActivity.toString()
    private lateinit var binding: ActivitySignInBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var userRepository: UserRepository
    //endregion

    //region Android Lifecycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setup view binding
        this.binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // sharedPrefs
        val prefs = applicationContext.getSharedPreferences("YARD_SALE_PREFS", MODE_PRIVATE)

        // fireBase Auth
        mAuth = FirebaseAuth.getInstance()
        userRepository = UserRepository(applicationContext)

        // sign in button
        binding.btnSignIn.setOnClickListener {
            if (validateUserInputData()) {
                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()
                signIn(email, password)

            } else {
                Toast.makeText(this, "Please provide correct inputs", Toast.LENGTH_SHORT).show()
            }
        }

        // sign up button
        binding.btnSignUp.setOnClickListener {
            // navigate to sign up screen
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
    //endregion


    //region Helper functions
    private fun validateUserInputData(): Boolean {
        var validateDataResult = true
        var email = ""
        var password = ""

        // email
        if (binding.etEmail.text.toString().isEmpty()) {
            binding.etEmail.error = "Email cannot be empty"
            validateDataResult = false
        } else {
            email = binding.etEmail.text.toString()
        }

        // password
        if (binding.etPassword.text.toString().isEmpty()) {
            binding.etPassword.error = "Password cannot be empty"
            validateDataResult = false
        } else {
            password = binding.etPassword.text.toString()
        }

        return validateDataResult
    }

    private fun signIn(email: String, password: String) {

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "SignIn successful")

                    // user's doc id, user name & user role will be saved to sharedPrefs
                    userRepository.searchUserWithEmail(email)

                    // user's email & password will be saved to sharedPrefs
                    saveToPrefs(email, password)

                    // TODO: check user type -> Seller or Buyer

                    // Go to Main Activity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                    Toast.makeText(
                        this@SignInActivity,
                        "Login Success! Welcome!",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    Log.e(TAG, "SignIn: Failed", task.exception)
                    Toast.makeText(
                        this@SignInActivity,
                        "Authentication Failed. Please try again!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveToPrefs(email: String, password: String) {
        val prefs = applicationContext.getSharedPreferences("YARD_SALE_PREFS", MODE_PRIVATE)
        prefs.edit().putString("USER_EMAIL", email).apply()
        prefs.edit().putString("USER_PASSWORD", password).apply()
    }
    //endregion

}