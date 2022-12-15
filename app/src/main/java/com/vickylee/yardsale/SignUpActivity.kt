package com.vickylee.yardsale

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import com.google.android.material.tabs.TabLayout.TabGravity
import com.google.firebase.auth.FirebaseAuth
import com.vickylee.yardsale.data.User
import com.vickylee.yardsale.data.UserRepository
import com.vickylee.yardsale.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    val TAG = this@SignUpActivity.toString()
    private lateinit var binding: ActivitySignUpBinding
    lateinit var userRepository: UserRepository
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        userRepository = UserRepository(applicationContext)

        var userType = ""

        binding.rdbBuyer.setOnClickListener {
            userType = binding.rdbBuyer.text.toString()
            binding.edtUserAddress.visibility = View.GONE
        }

        binding.rdbSeller.setOnClickListener {
            userType = binding.rdbSeller.text.toString()
            binding.edtUserAddress.visibility = View.VISIBLE
        }

        binding.btnSignUp.setOnClickListener {
            validateData(userType)
        }
    }

    private fun validateData(userType: String) {
        var validData = false
        val name = binding.edtName.text.toString()
        val email = binding.edtEmail.text.toString()
        val password = binding.edtPwd.text.toString()
        val phone = binding.edtPhone.text.toString()
        val address = binding.edtUserAddress.text.toString()
        var userType = ""

        // Validate name
        if (name.isEmpty()) {
            binding.edtName.error = "Name can not be empty"
            validData = false
        } else {
            validData = true
        }

        // validate email
        if (email.isEmpty()) {
            binding.edtEmail.error = "Email Id can not be empty"
            validData = false
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmail.error = "Invalid email address"
            validData = false
        }
        else {
            validData = true
        }

        // Validate password
        if (password.isEmpty()) {
            binding.edtPwd.error = "Password can not be empty"
            validData = false
        }
        else if (password.length < 6) {
            binding.edtPwd.error = "Password must contain 6 characters"
            validData = false
        }
        else {
            validData = true
        }

        // Validate user type

        when (binding.rdgUserType.checkedRadioButtonId) {
            R.id.rdb_buyer -> {
                binding.tvUserTypeError.setText("")
                userType = binding.rdbBuyer.text.toString()

                validData = true
            }
            R.id.rdb_seller -> {
                binding.tvUserTypeError.setText("")
                userType = binding.rdbSeller.text.toString()
                validData = true
            }
            else -> {
                binding.tvUserTypeError.setText("You must choose a type")
                validData = false
            }
        }

        //validate phone number
        if (phone.isEmpty()) {
            binding.edtPhone.setError("Phone number can not be empty")
            validData = false
        }
        else if (phone.length < 10) {
            binding.edtPhone.setError("Phone number must be 10 digits")
            validData = false
        }
        else {
            validData = true
        }

        // Validate address for seller
        if (userType == "Seller") {
            if (address.isEmpty()) {
                binding.edtUserAddress.setError("Address can not be empty")
                validData = false
            }
            else if (address.isDigitsOnly()) {
                binding.edtUserAddress.setError("Address can not contain only digits")
                validData = false
            }
            else {
                validData = true
            }
        }

        if (validData) {
            signUp(name, email, password, phone, address, userType)
        }
    }

    // Sign up with email
    private fun signUp(name: String, email: String, password: String, phone: String, address : String, userType: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    addUserToDB(name, email, password, phone, address, userType)
                    saveToPrefs(name, email, password, userType)

                    if (userType == "Seller") {
                        goToSellerActivity(name)
                    } else {
                        goToBuyerActivity(name)
                    }

                } else {
                    Toast.makeText(this@SignUpActivity, "Failed to sign up", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun saveToPrefs(name: String, email: String, password: String, userType: String) {
        val prefs = applicationContext.getSharedPreferences("YARD_SALE_PREFS", MODE_PRIVATE)
        prefs.edit().putString("USER_NAME", name).apply()
        prefs.edit().putString("USER_EMAIL", email).apply()
        prefs.edit().putString("USER_PASSWORD", password).apply()
        prefs.edit().putString("USER_TYPE", userType).apply()
    }

    private fun addUserToDB(name: String, email: String, password: String, phone: String, address : String, userType: String) {
        userRepository.addUserToDB(
            User(
                name = name,
                email = email,
                phone = phone,
                address = address,
                password = password,
                userType = userType
            )
        )
    }

    private fun goToSellerActivity(currentUsername: String) {
        // Go to Main Activity (Seller)
        val sellerIntent = Intent(this, MainActivity::class.java)
        startActivity(sellerIntent)

        Toast.makeText(
            this@SignUpActivity,
            "Sign up Success! Hello $currentUsername!",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun goToBuyerActivity(currentUsername: String) {
        val buyerIntent = Intent(this, MainActivity2::class.java)
        startActivity(buyerIntent)
        finish()

        Toast.makeText(
            this@SignUpActivity,
            "Sign up Success! Hello $currentUsername!",
            Toast.LENGTH_SHORT
        ).show()
    }
}