package com.vickylee.yardsale.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
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
import com.vickylee.yardsale.data.UserRepository
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.vickylee.yardsale.databinding.FragmentEditProfileBinding


class EditProfileFragment : Fragment() {

    val TAG = this@EditProfileFragment
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: SharedPreferences
    private lateinit var userRepository : UserRepository

    private lateinit var launcher: ActivityResultLauncher<Intent>

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
        var userID = prefs.getString("USER_DOC_ID", "NA")
        Log.d(TAG.toString(), "onViewCreated: $userID")
        if (userID != null) {
            getUserDetailsFromDB(userID)
        }

        binding.tvPickImage.setOnClickListener {
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

        binding.btnSave.setOnClickListener {
            val phone = binding.edtPhone.text.toString()
            val address = binding.edtLocation.text.toString()
            if (userID != null) {
                updateUserDetailsInDB(userID, phone, address)
            }
            val action = EditProfileFragmentDirections.actionEditProfileFragmentToProfileFragment()
            findNavController().navigate(action)
        }

    }

    @SuppressLint("IntentReset")
    private fun selectPhoto() {
        val pickFromGallery = Intent(Intent.ACTION_PICK)
        pickFromGallery.type = "image/*"
        startActivityForResult(pickFromGallery, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        binding.ivProfilePic.setImageURI(data?.data)
    }

    private fun updateUserDetailsInDB(userID: String, phone: String, address: String) {
        userRepository.updateUserDetails(userID, phone, address)
    }

    private fun getUserDetailsFromDB(userID: String) {
        val userType = prefs.getString("USER_TYPE", "")
        userRepository.getUserDetailsFromDB(userID)
        userRepository.user.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                Log.d(TAG.toString(), "getUserDetailsFromDB: $it")
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

    private fun hasExternalStoragePermission(): Boolean {
        // returns true of the External storage permission is granted, and false otherwise
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}