package com.vickylee.yardsale.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vickylee.yardsale.R
import com.vickylee.yardsale.data.Item
import com.vickylee.yardsale.data.UserRepository
import com.vickylee.yardsale.databinding.FragmentAddItemBinding


class AddItemFragment : Fragment(R.layout.fragment_add_item) {

    //region Properties
    val TAG = this.toString()
    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!
    lateinit var userRepository: UserRepository

    var pickedPhoto: Uri? = null
    var pickedBitMap: Bitmap? = null    // for displaying the selected photo

    companion object {
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

        // Add button
        binding.btnAddItem.setOnClickListener {
            Log.d(TAG, "AddItemFragment - Add button is pressed")

            if (validateUserInputData()) {
                val itemName = binding.edtItemName.text.toString()
                val itemDescription = binding.edtItemDescription.text.toString()
                val itemPrice = binding.edtItemPrice.text.toString().toDouble()

                val newItem = Item(
                    itemName = itemName,
                    itemDescription = itemDescription,
                    itemPrice = itemPrice
                )
                userRepository.addItemToUserAccount(newItem)
                userRepository.uploadImage(pickedPhoto!!)

                Toast.makeText(context, "New item has been added to the list", Toast.LENGTH_SHORT)
                    .show()
                resetInputField()

                // navigate to AddItemFragment
                val action = AddItemFragmentDirections.actionAddItemFragmentToListViewFragment()
                findNavController().navigate(action)

            } else {
                Toast.makeText(context, "Please provide correct inputs", Toast.LENGTH_SHORT).show()
            }
        }

        // Camera button
        binding.imgBtnCamera.setOnClickListener {

            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                1000
            )

            if (hasCameraPermission()) {
                // Navigate to Camera Preview Fragment
                val action =
                    AddItemFragmentDirections.actionAddItemFragmentToCameraPreviewFragment()
                findNavController().navigate(action)

            } else {
                // To make sure the educate dialog will not appear when user see the permission request first time
                if (camera_cnt != 0) {
                    educateUserToAllowCameraPermission()
                }
                camera_cnt++
            }
        }

        // Photo Gallery button
        binding.imgBtnPhotoGallery.setOnClickListener {

            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )

            if (hasExternalStoragePermission()) {
                val pickPhoto = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(
                    pickPhoto,
                    2
                )     // change request code to 2 (indicate permission granted)
            } else {
                // To make sure the educate dialog will not appear when user see the permission request first time
                if (photoGallery_cnt != 0) {
                    educateUserToAllowPhotoAccess()
                }
                //photoGallery_cnt++
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

        return validateDataResult
    }

    private fun resetInputField() {
        binding.edtItemName.setText("")
        binding.edtItemDescription.setText("")
        binding.edtItemPrice.setText("")
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
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun educateUserToAllowCameraPermission() {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("In order to select photo from the photo gallery, please go to app setting and allow this app to access your photos and videos.")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, id ->
            }

        val alert = builder.create()
        alert.show()
    }

    private fun educateUserToAllowPhotoAccess() {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("In order to take photo, please go to app setting and allow this app to use your camera.")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, id ->
            }

        val alert = builder.create()
        alert.show()
    }

    //endregion
    //region Helper function - Photo Gallery
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // request code 2 (Photo access permission granted)
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            pickedPhoto = data.data

            if (pickedPhoto != null) {
                if (Build.VERSION.SDK_INT >= 28) {
                    val source =
                        ImageDecoder.createSource(requireContext().contentResolver, pickedPhoto!!)
                    pickedBitMap = ImageDecoder.decodeBitmap(source)
                    binding.imageView.setImageBitmap(pickedBitMap)
                } else {
                    pickedBitMap = MediaStore.Images.Media.getBitmap(
                        requireContext().contentResolver,
                        pickedPhoto
                    )
                    binding.imageView.setImageBitmap(pickedBitMap)
                }
            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    //endregion
}