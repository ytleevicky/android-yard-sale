package com.vickylee.yardsale.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vickylee.yardsale.R
import com.vickylee.yardsale.data.Item
import com.vickylee.yardsale.data.UserRepository
import com.vickylee.yardsale.databinding.FragmentAddItemBinding
import java.lang.Exception

class AddItemFragment : Fragment(R.layout.fragment_add_item) {

    //region Properties
    val TAG = this.toString()
    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!
    lateinit var userRepository: UserRepository

    // Permission
    // request code
    private val REQUEST_PERMISSION_CODE = 1234

    // List of permissions that app requires
    private val REQUIRED_PERMISSIONS_LIST =
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)

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

        binding.imgBtnCamera.setOnClickListener {
            // permission
            if (allPermissionsGranted() == true) {
                Log.d(TAG, "onViewCreated:  Application has required permission, continuing...")
                // If the user has provided all permissions, then start the camera preview

                // Navigate to Camera Preview Fragment
                val action =
                    AddItemFragmentDirections.actionAddItemFragmentToCameraPreviewFragment()
                findNavController().navigate(action)
            } else {
                // TODO: Otherwise, request permissions
                requestPermissions(
                    REQUIRED_PERMISSIONS_LIST,
                    REQUEST_PERMISSION_CODE
                )
                showAlertBoxToEducateUser()
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
    fun allPermissionsGranted(): Boolean {
        if (hasCameraPermission() == true && hasExternalStoragePermission() == true) {
            Log.d(TAG, "checkPermissions: User granted all required permissions")
            return true
        } else {
            Log.d(TAG, "checkPermissions: User is missing some or all of the required permissions")
            return false
        }
    }

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

    private fun showAlertBoxToEducateUser() {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("To take picture, please go to app setting and allow this app to use your camera.")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, id ->
            }

        val alert = builder.create()
        alert.show()
    }
    //endregion
}