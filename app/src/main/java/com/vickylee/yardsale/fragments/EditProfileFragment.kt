package com.vickylee.yardsale.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.text.isDigitsOnly
import com.vickylee.yardsale.data.UserRepository
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.vickylee.yardsale.R
import com.vickylee.yardsale.databinding.FragmentEditProfileBinding


class EditProfileFragment : Fragment() {

    val TAG = this@EditProfileFragment
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: SharedPreferences
    private lateinit var userRepository : UserRepository
    private var imageUri: Uri? = null
    private var imageUrl: String = ""
    private lateinit var userID: String
    private lateinit var userType: String

    private lateinit var storageRef: StorageReference
    private lateinit var firebaseFirestore: FirebaseFirestore

    // Permission
    // request code
    private val REQUEST_PERMISSION_CODE = 1234

    // List of permissions that app requires
    private val REQUIRED_PERMISSIONS_LIST =
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepository = UserRepository(requireContext())

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        prefs = requireContext().getSharedPreferences("YARD_SALE_PREFS",
            AppCompatActivity.MODE_PRIVATE
        )
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userID = prefs.getString("USER_DOC_ID", "NA").toString()
        userType = prefs.getString("USER_TYPE", "NA").toString()

        // Get user details from DB
        if (userID != null) {
            getUserDetailsFromDB(userID)
        }

        // Pick profile picture from gallery
        binding.fabEditProfilePic.setOnClickListener {
            if (hasExternalStoragePermission()) {
                selectPhoto()
            }
            else {
                // TODO: Otherwise, request permissions
                requestPermissions(
                    REQUIRED_PERMISSIONS_LIST,
                    REQUEST_PERMISSION_CODE
                )
            }
        }

        // Save edited profile
        binding.btnSave.setOnClickListener {
            val phone = binding.edtPhone.text.toString()
            val address = binding.edtLocation.text.toString()
            validateData(userID, phone, address)
        }

    }

    // Get user details from DB
    private fun getUserDetailsFromDB(userID: String) {
        val userType = prefs.getString("USER_TYPE", "")
        userRepository.getUserDetailsFromDB(userID)
        userRepository.user.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                Log.d(TAG.toString(), "getUserDetailsFromDB: $it")
                val imageView = binding.ivProfilePic
                if (it.profilePic != "") {
                    imageUrl = it.profilePic
                    Picasso.with(context).load(it.profilePic).into(imageView)
                }
                else {
                    imageUrl = ""
                }
                binding.edtName.setText(it.name)
                binding.edtEmail.setText(it.email)
                binding.edtPhone.setText(it.phone)
                if (userType == "Seller") {
                    binding.edtLocation.visibility = View.VISIBLE
                    binding.edtLocation.setText(it.address)
                }

            }
        })
    }

    // Ask user for storage permission
    private fun hasExternalStoragePermission(): Boolean {
        // returns true of the External storage permission is granted, and false otherwise
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Select photo from gallery
    @SuppressLint("IntentReset")
    private fun selectPhoto() {
        val pickFromGallery = Intent(Intent.ACTION_PICK)
        pickFromGallery.type = "image/*"
        startActivityForResult(pickFromGallery, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            imageUri = data.data
            Log.d("TAG", "onActivityResult: imageUri: $imageUri")
            binding.ivProfilePic.setImageURI(imageUri)
        }
        else {
            imageUri = null
        }

    }

    // Validate user input
    private fun validateData(userID: String, phone: String, address: String) {
        var validData = false

        // Validate phone number
        if (phone.isEmpty()) {
            binding.edtPhone.error = "Phone number can not be empty"
            validData = false
        }
        else if (phone.length < 10) {
            binding.edtPhone.error = "Phone number must be 10 digit"
            validData = false
        }
        else {
            validData = true
        }

        // Validate address for seller
        if (userType == "Seller") {
            if (address.isEmpty()) {
                binding.edtLocation.error = "Address can not be empty"
                validData = false
            }
            else if (address.isDigitsOnly()) {
                binding.edtLocation.error = "Address can not be digits only"
                validData = false
            }
            else {
                validData = true
            }
        }

        if (validData) {
            validateImage(userID, phone, address)
        }
    }

    // Validate image
    private fun validateImage(userID: String, phone: String, address: String) {
        if (userID != null) {
            if (imageUri != null) {
                storeProfilePic(userID, imageUri!!, phone, address)
            }
            else if (imageUrl != ""){
                updateUserDetailsInDB(userID, phone, address, imageUrl)
            }
            else {
                updateUserDetailsInDB(userID, phone, address, "")
            }
        }
    }

    // Store profile picture to firebase storage
    private fun storeProfilePic(userID: String, imageUri: Uri, phone: String, address: String) {
        storageRef = FirebaseStorage.getInstance().reference.child("Images")
        firebaseFirestore = FirebaseFirestore.getInstance()
        storageRef = storageRef.child(System.currentTimeMillis().toString())
        if (imageUri != null) {
            storageRef.putFile(imageUri).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        updateUserDetailsInDB(userID, phone, address, uri.toString())
                    }
                }

            }
        }
    }

    // Update data to DB
    private fun updateUserDetailsInDB(userID: String, phone: String, address: String, imageUrl: String) {
        Log.d("TAG", "onViewCreated: Starting2")
        userRepository.updateUserDetails(userID, phone, address, imageUrl)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}