package com.vickylee.yardsale.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import com.vickylee.yardsale.R
import com.vickylee.yardsale.data.UserRepository
import com.vickylee.yardsale.databinding.FragmentEditItemBinding

class EditItemFragment : Fragment() {
    //region Properties
    val TAG = this.toString()
    private var _binding: FragmentEditItemBinding? = null
    private val binding get() = _binding!!
    lateinit var userRepository: UserRepository
    private lateinit var prefs: SharedPreferences
    private var imageUri: Uri? = null
    private var imageUrl: String = ""

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
        // setup view binding
        _binding = FragmentEditItemBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var userID = prefs.getString("USER_DOC_ID", "NA")
        var itemID = prefs.getString("ITEM_ID", "NA")
        var sellerID = prefs.getString("SELLER_ID", "NA")
        Log.d(TAG.toString(), "onViewCreated: $userID")
        if (userID != null) {
            if (sellerID != null) {
                if (itemID != null) {
                    userRepository.getItemDetails(userID, itemID)
                    userRepository.item.observe(viewLifecycleOwner, Observer {
                        if (it != null) {
                            binding.edtItemName.setText(it.itemName)
                            binding.edtItemDescription.setText(it.itemDescription)
                            binding.edtItemPrice.setText(it.itemPrice.toString())
                            val imageView = binding.ivItemPic
                            imageUrl = it.itemPic
                            Picasso.with(context).load(imageUrl).into(imageView)

                        }

                    })
                }
            }


            binding.btnSaveItem.setOnClickListener {
                val itemName = binding.edtItemName.text.toString()
                val itemDescription = binding.edtItemDescription.text.toString()
                val itemPrice = binding.edtItemPrice.text.toString().toDouble()
                if (validateUserInputData()) {
                    if (itemID != null) {
                        if (imageUri != null) {
                            userRepository.updateItem(userID, itemID, itemName, itemDescription, itemPrice, imageUri.toString())
                        }
                        else {
                            userRepository.updateItem(userID, itemID, itemName, itemDescription, itemPrice, imageUrl)
                        }

                    }
                    val action = EditItemFragmentDirections.actionEditItemFragmentToListViewFragment()
                    findNavController().navigate(action)
                }
            }
        }

        binding.imgBtnPhotoGallery.setOnClickListener {
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
    }

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

    private fun hasExternalStoragePermission(): Boolean {
        // returns true of the External storage permission is granted, and false otherwise
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

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
            binding.ivItemPic.setImageURI(imageUri)
        }
        else {
            imageUri = null
        }

    }
}