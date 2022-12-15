package com.vickylee.yardsale.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.vickylee.yardsale.LoadingDialog
import com.vickylee.yardsale.R
import com.vickylee.yardsale.data.UserRepository
import com.vickylee.yardsale.databinding.FragmentAddItemBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddItemFragment : Fragment(R.layout.fragment_add_item) {

    //region Properties
    val TAG = this.toString()
    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!
    lateinit var userRepository: UserRepository
    private var imageUri: Uri? = null

    private lateinit var storageRef: StorageReference
    private lateinit var firebaseFirestore: FirebaseFirestore

    companion object {
        // To track number of the permission request shown to user
        var camera_cnt = 0
        var photoGallery_cnt = 0
    }

    //endregion

    //region Lifecycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "AddItemFragment - onCreate() is executing")

        userRepository = UserRepository(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "AddItemFragment - onCreateView() is executing")

        // setup view binding
        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "AddItemFragment - onViewCreated() is executing")

        binding.btnAddItem.setOnClickListener {
            if (validateUserInputData()) {
                val itemName = binding.edtItemName.text.toString()
                val itemDescription = binding.edtItemDescription.text.toString()
                val itemPrice = binding.edtItemPrice.text.toString().toDouble()
                storeItemImage(itemName, itemDescription, itemPrice, imageUri!!)
                displayLoadingDialog(1500)

                GlobalScope.async {
                    delay(1500)
                }
                Toast.makeText(context, "New item has been added to the list", Toast.LENGTH_SHORT)
                    .show()
                resetInputField()


            } else {
                Toast.makeText(context, "Please provide correct inputs", Toast.LENGTH_SHORT).show()
            }
        }

//        binding.imgBtnCamera.setOnClickListener {
//
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(Manifest.permission.CAMERA),
//                1
//            )
//
//            if (hasCameraPermission()) {
//                // Navigate to Camera Preview Fragment
//                val action =
//                    AddItemFragmentDirections.actionAddItemFragmentToCameraPreviewFragment()
//                findNavController().navigate(action)
//            } else {
//                if (camera_cnt > 1) {
//                    educateUserToAllowCameraPermission()
//                }
//                camera_cnt++
//            }
//        }

        binding.imgBtnPhotoGallery.setOnClickListener {

            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                2
            )

            if (hasExternalStoragePermission()) {
                selectPhoto()
            } else {
                if (photoGallery_cnt > 1) {
                    educateUserToAllowPhotoAccessPermission()
                }
                photoGallery_cnt++
            }
        }
    }

    private fun storeItemImage(itemName: String, itemDescription: String, itemPrice: Double, imageUri: Uri) {
        storageRef = FirebaseStorage.getInstance().reference.child("Images")
        firebaseFirestore = FirebaseFirestore.getInstance()
        storageRef = storageRef.child(System.currentTimeMillis().toString())

        if (imageUri != null) {
            storageRef.putFile(imageUri).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        userRepository.addItemToUserAccount(itemName, itemDescription, itemPrice, uri.toString())
                    }
                }

            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "AddItemFragment: onPause() is executing now... ")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    //endregion

    //region Helper function
    private fun validateUserInputData(): Boolean {
        var validateDataResult = true
        var itemName = ""
        var itemDescription = ""
        var itemPrice = ""

        // item name
        if (binding.edtItemName.text.toString().isNullOrEmpty()) {
            Log.d(TAG, "pressed. edt item name is empty")
            binding.edtItemName.error = "Item name cannot be empty"
            validateDataResult = false
        } else {
            itemName = binding.edtItemName.text.toString()
        }

        // item Description
        if (binding.edtItemDescription.text.toString().isEmpty()) {
            binding.edtItemDescription.error = "Item description cannot be empty"
            validateDataResult = false
        } else {
            itemDescription = binding.edtItemDescription.text.toString()
        }

        // item Price
        if (binding.edtItemPrice.text.toString().isEmpty()) {
            binding.edtItemPrice.error = "Item price cannot be empty"
            validateDataResult = false
        } else {
            itemPrice = binding.edtItemPrice.text.toString()
        }

        // item image
        if (imageUri == null) {
            binding.tvErrorMsg.text = "Please provide an image"
            validateDataResult = false
        } else {
            binding.tvErrorMsg.text = ""
        }

        return validateDataResult
    }

    private fun resetInputField() {
        binding.edtItemName.setText("")
        binding.edtItemDescription.setText("")
        binding.edtItemPrice.setText("")
        binding.tvErrorMsg.setText("")
        binding.ivItemPic.setImageURI(null)
    }
    //endregion

    //region Helper function - Permission
    private fun hasCameraPermission(): Boolean {
        // returns true of the Camera permission is granted, and false otherwise
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasExternalStoragePermission(): Boolean {
        // returns true of the External storage permission is granted, and false otherwise
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun educateUserToAllowCameraPermission() {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("In order to take photo, please go to app setting and allow this app to use your camera.")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, id ->
            }

        val alert = builder.create()
        alert.show()
    }

    private fun educateUserToAllowPhotoAccessPermission() {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("In order to select photo from the photo gallery, please go to app setting and allow this app to access your photos and videos.")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, id ->
            }

        val alert = builder.create()
        alert.show()
    }
    //endregion

    fun displayLoadingDialog(duration: Long) {
        val loading = LoadingDialog(requireActivity())
        loading.startLoading("Adding item")
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                loading.isDimiss()
            }
        }, duration)
    }

    //region Helper Function - Image View
    @SuppressLint("IntentReset")
    private fun selectPhoto() {
        val pickFromGallery = Intent(Intent.ACTION_PICK)
        pickFromGallery.type = "image/*"
        startActivityForResult(pickFromGallery, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imageUri = data?.data
        Log.d("TAG", "onActivityResult: imageUri: $imageUri")
        binding.ivItemPic.setImageURI(data?.data)
        binding.tvErrorMsg.text = ""
    }
    //endregion
}