package com.vickylee.yardsale.fragments

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.vickylee.yardsale.R
import com.vickylee.yardsale.data.UserRepository
import com.vickylee.yardsale.databinding.FragmentProfileBinding
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

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
        Log.d(TAG.toString(), "onViewCreated: $userID")
        if (userID != null) {
            getUserDetailsFromDB(userID)
        }

        binding.btnEditProfile.setOnClickListener {
            // navigate to edit profile fragment
            val action = ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment()
            findNavController().navigate(action)
        }

        binding.tvChangePassword.setOnClickListener {
            if (userID != null) {
                changePassword(userID)
            }
        }
    }

    private fun changePassword(userID: String) {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.change_password, null)
        var currentPassword = dialogView.findViewById<EditText>(R.id.edt_cur_pwd)
        userRepository.getUserDetailsFromDB(userID)
        userRepository.user.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                Log.d(TAG.toString(), "getUserDetailsFromDB: $it")
                currentPassword.setText(it.password)
            }
        })
        val editDialog = AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Update") { dialog, which ->
                val newPassword = dialogView.findViewById<EditText>(R.id.edt_new_pwd)
                val confirmPassword = dialogView.findViewById<EditText>(R.id.edt_cnf_pwd)

                if (newPassword.text.toString() == confirmPassword.text.toString()) {
                    Log.d("TAG", "changePassword: Password matched")
                    userRepository.changePassword(userID, newPassword.text.toString())
                    var user = mAuth.currentUser

                    user?.updatePassword(newPassword.text.toString())?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("TAG", "changePassword: Password updated")
                        }
                        else {
                            Log.d("TAG", "changePassword: password not updated")
                        }
                    }
                }
                else {
                    confirmPassword.setError("Password didn't match with new password")
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        editDialog.show()
    }

    private fun getUserDetailsFromDB(userID: String) {
        userRepository.getUserDetailsFromDB(userID)
        userRepository.user.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                Log.d(TAG.toString(), "getUserDetailsFromDB: $it")
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