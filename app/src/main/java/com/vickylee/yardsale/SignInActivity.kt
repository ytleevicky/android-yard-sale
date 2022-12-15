package com.vickylee.yardsale

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.auth.FirebaseAuth
import com.vickylee.yardsale.data.UserRepository
import com.vickylee.yardsale.databinding.ActivitySignInBinding
import kotlinx.coroutines.*
import java.lang.Runnable
import java.time.Duration

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

        if (binding.swtRemember.isChecked) {
            if (prefs.contains("USER_EMAIL")) {
                binding.etEmail.setText(prefs.getString("USER_EMAIL", ""))
            }
            if (prefs.contains("USER_PASSWORD")) {
                binding.etPassword.setText(prefs.getString("USER_PASSWORD", ""))
            }
        }

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

    //region Actions
    private fun signIn(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "SignIn successful")

                    // user's doc id, user name & user role will be saved to sharedPrefs
                    userRepository.searchUserWithEmail(email)

                    // user's email & password will be saved to sharedPrefs
                    saveToPrefs(email, password)

                    // Dispaly Loading Dialog on UI
                    displayLoadingDialog(1500)

                    GlobalScope.async {
                        delay(1500)

                        // check user type
                        val prefs =
                            applicationContext.getSharedPreferences("YARD_SALE_PREFS", MODE_PRIVATE)

                        val currentUserType = prefs.getString("USER_TYPE", "")!!
                        val currentUserName = prefs.getString("USER_NAME", "")!!
                        Log.e("TEST", "Current User Type: $currentUserType")

                        if (currentUserType == "Seller") {
                            goToSellerActivity(currentUserName)

                        } else {
                            goToBuyerActivity(currentUserName)
                        }
                    }
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

    suspend fun goToSellerActivity(currentUsername: String) {
        // Go to Main Activity (Seller)
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("EXTRA_REMEMBER_ME", binding.swtRemember.isChecked)
        startActivity(intent)

        runOnUiThread {
            Toast.makeText(
                this@SignInActivity,
                "Login Success! Hello $currentUsername!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    suspend fun goToBuyerActivity(currentUsername: String) {
        // Go to Main Activity (Seller)
        val intent = Intent(this, MainActivity2::class.java)
        intent.putExtra("EXTRA_REMEMBER_ME", binding.swtRemember.isChecked)
        startActivity(intent)

        runOnUiThread {
            Toast.makeText(
                this@SignInActivity,
                "Login Success! Hello $currentUsername!",
                Toast.LENGTH_SHORT
            ).show()
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

    private fun saveToPrefs(email: String, password: String) {
        val prefs = applicationContext.getSharedPreferences("YARD_SALE_PREFS", MODE_PRIVATE)
        prefs.edit().putString("USER_EMAIL", email).apply()
        prefs.edit().putString("USER_PASSWORD", password).apply()
        prefs.edit().putStringSet("USER_FAV_ITEMS", null).apply()
    }

    fun displayLoadingDialog(duration: Long) {
        val loading = LoadingDialog(this)
        loading.startLoading("Signing In")
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                loading.isDimiss()
            }
        }, duration)
    }
    //endregion

}