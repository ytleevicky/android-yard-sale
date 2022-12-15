package com.vickylee.yardsale.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.vickylee.yardsale.R
import com.vickylee.yardsale.data.UserRepository
import com.vickylee.yardsale.databinding.FragmentProfileBinding
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import com.vickylee.yardsale.SignInActivity

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    val TAG = this@ProfileFragment
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: SharedPreferences
    private lateinit var userRepository : UserRepository
    private lateinit var mAuth: FirebaseAuth

    //region Lifecycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepository = UserRepository(requireContext())
        mAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        prefs = requireContext().getSharedPreferences("YARD_SALE_PREFS",
            AppCompatActivity.MODE_PRIVATE
        )
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var userID = prefs.getString("USER_DOC_ID", "NA")

        // Get user details from database
        if (userID != null) {
            getUserDetailsFromDB(userID)
        }

        // Start edit profile fragment
        binding.btnEditProfile.setOnClickListener {
            // navigate to edit profile fragment
            val action = ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment()
            findNavController().navigate(action)
        }

        // Change password
        binding.tvChangePassword.setOnClickListener {
            if (userID != null) {
                changePassword(userID)
            }
        }
    }

    private fun changePassword(userID: String) {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.change_password, null)
        val currentPassword = dialogView.findViewById<EditText>(R.id.edt_cur_pwd)
        val newPassword = dialogView.findViewById<EditText>(R.id.edt_new_pwd)
        val confirmPassword = dialogView.findViewById<EditText>(R.id.edt_cnf_pwd)
        val passwordFromPrefs = prefs.getString("USER_PASSWORD", "NA")

        // Check if user inputs current password correctly
        currentPassword.doOnTextChanged { text, start, before, count ->
            if (currentPassword.text.toString() != passwordFromPrefs) {
                currentPassword.setError("Current password didn't match")
            }
        }

        // Check if new and confirm password matches
        confirmPassword.doOnTextChanged { text, start, before, count ->
            if (newPassword.text.toString() != confirmPassword.text.toString()) {
                confirmPassword.setError("Password didn't match with new password")
            }
        }

        // Show dialog
        val editDialog = AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Update") { dialog, which ->
                if (newPassword.text.toString() == confirmPassword.text.toString()) {
                    userRepository.changePassword(userID, newPassword.text.toString())
                    var user = mAuth.currentUser

                    user?.updatePassword(newPassword.text.toString())?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Password changed successfully!", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Toast.makeText(context, "Couldn't change password, try again!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    val intent = Intent(requireActivity(), SignInActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                else {
                    Toast.makeText(context, "Couldn't change password, try again!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .create()
        editDialog.show()
    }

    // Get user details from db
    private fun getUserDetailsFromDB(userID: String) {
        userRepository.getUserDetailsFromDB(userID)
        userRepository.user.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                val imageView = binding.ivProfilePic
                if (it.profilePic != "") {
                    Picasso.with(context).load(it.profilePic).into(imageView)
                }
                binding.tvName.text = it.name
                binding.tvEmail.text = it.email
                if (it.phone != "") {
                    binding.tvPhone.text = it.phone
                }
                else {
                    binding.ivPhone.visibility = View.GONE
                    binding.tvPhone.visibility = View.GONE
                }
                if (it.address != "") {
                    binding.tvLocation.text = it.address
                }
                else {
                    binding.ivLocation.visibility = View.GONE
                    binding.tvLocation.visibility = View.GONE
                }

            }
        })
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    //endregion
}