package com.vickylee.yardsale.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vickylee.yardsale.data.UserRepository
import com.vickylee.yardsale.databinding.FragmentItemDetailsBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.vickylee.yardsale.LoadingDialog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ItemDetailsFragment : Fragment() {

    val TAG = this.toString()
    private var _binding: FragmentItemDetailsBinding? = null
    private val binding get() = _binding!!
    lateinit var userRepository: UserRepository
    private lateinit var prefs: SharedPreferences
    private lateinit var userType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepository = UserRepository(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        prefs = requireContext().getSharedPreferences(
            "YARD_SALE_PREFS",
            AppCompatActivity.MODE_PRIVATE
        )
        _binding = FragmentItemDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var userID = prefs.getString("USER_DOC_ID", "NA")
        var itemID = prefs.getString("ITEM_ID", "NA")
        var sellerID = prefs.getString("SELLER_ID", "NA")
        userType = prefs.getString("USER_TYPE", "NA").toString()

        // Get item details fom DB
        if (sellerID != null) {
            if (itemID != null) {
                getItemDetails(sellerID, itemID)

                // Show seller info only to buyer
                if (userType == "Buyer") {
                    binding.tvSellerInfo.visibility = View.VISIBLE
                    binding.ivPhone.visibility = View.VISIBLE
                    binding.tvPhone.visibility = View.VISIBLE
                    binding.ivLocation.visibility = View.VISIBLE
                    binding.tvLocation.visibility = View.VISIBLE
                    userRepository.getUserDetailsFromDB(sellerID)
                    userRepository.user.observe(viewLifecycleOwner, Observer {
                        if (it != null) {
                            binding.tvPhone.text = it.phone
                            binding.tvLocation.text = it.address
                        }
                    })

                    // Allow buyer to call the seller
                    binding.tvPhone.setOnClickListener {
                        // get phone number
                        val phoneNumberFromUI = binding.tvPhone.text.toString()

                        //ask for confirmation
                        val confirmDialog = AlertDialog.Builder(requireContext())
                        confirmDialog.setTitle("Confirm")
                        confirmDialog.setMessage("Are you sure you want to call this seller?")
                        confirmDialog.setNegativeButton("Cancel") { dialogInterface, i ->
                            dialogInterface.dismiss()
                        }
                        confirmDialog.setPositiveButton("Call") { dialogInterface, i ->
                            val phoneNumAsUri: Uri = Uri.parse("tel: $phoneNumberFromUI")
                            val intent = Intent(Intent.ACTION_DIAL, phoneNumAsUri)
                            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                                startActivity(intent)
                            }
                            else {
                                Toast.makeText(context, "Couldn't call this number", Toast.LENGTH_SHORT).show()
                            }
                        }
                        confirmDialog.show()
                    }
                }

                // Add item to favorites for buyer
                binding.btnAddFav.setOnClickListener {
                    if (itemID != null) {
                        userRepository.addItemToFavorites(itemID)
                    }
                    Toast.makeText(requireContext(), "Added to favorite.", Toast.LENGTH_SHORT).show()
                }

                // Remove from favorite for buyer
                binding.btnRemoveFav.setOnClickListener {
                    if (itemID != null) {
                        userRepository.removeItemFromFavorites(itemID)
                    }
                    Toast.makeText(requireContext(), "Removed from favorite.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    // Get item details from DB
    private fun getItemDetails(sellerID: String, itemID: String) {
        userRepository.getItemDetails(sellerID, itemID)
        userRepository.item.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                binding.tvItemName.setText(it.itemName)
                binding.tvItemDetails.setText(it.itemDescription)
                binding.tvItemPrice.setText(it.itemPrice.toString())
                val imageView = binding.ivItemPic
                Picasso.with(context).load(it.itemPic).into(imageView)

                // If the user is seller then show mark sold button
                if (userType == "Seller") {
                    binding.btnItemStatus.visibility = View.VISIBLE
                    binding.fabEditItem.visibility = View.VISIBLE
                    binding.fabDeleteItem.visibility = View.VISIBLE
                    var itemStatus = it.isItemAvailable
                    if (itemStatus) {
                        binding.btnItemStatus.setText("Mark Sold")
                    } else {
                        binding.btnItemStatus.setText("Mark Available")
                    }

                    // Allow seller to mark item as sold
                    binding.btnItemStatus.setOnClickListener {
                        itemStatus = !itemStatus

                        if (sellerID != null) {
                            userRepository.updateItemAvailability(sellerID, itemID, itemStatus)
                        }
                        val action =
                            ItemDetailsFragmentDirections.actionItemDetailsFragmentToListViewFragment()
                        findNavController().navigate(action)
                    }

                    // Go to edit item screen
                    binding.fabEditItem.setOnClickListener {
                        val action =
                            ItemDetailsFragmentDirections.actionItemDetailsFragmentToEditItemFragment()
                        findNavController().navigate(action)
                    }

                    binding.fabDeleteItem.setOnClickListener {
                        //ask for confirmation
                        val confirmDialog = AlertDialog.Builder(requireContext())
                        confirmDialog.setTitle("Delete")
                        confirmDialog.setMessage("Are you sure you want to delete this item?")
                        confirmDialog.setNegativeButton("Cancel") { dialogInterface, i ->
                            dialogInterface.dismiss()
                        }
                        confirmDialog.setPositiveButton("Yes") { dialogInterface, i ->
                            displayLoadingDialog(1500)
                            userRepository.deleteItem(sellerID, itemID)
                            GlobalScope.async {
                                delay(1500)
                            }
                            val action =
                                ItemDetailsFragmentDirections.actionItemDetailsFragmentToListViewFragment()
                            findNavController().navigate(action)
                        }
                        confirmDialog.show()

                    }
                }
                else {
                    val userFavItemList = prefs.getStringSet("USER_FAV_ITEMS", null)

                    // user currently has no favorite items
                    if (userFavItemList == null || userFavItemList.isEmpty()) {
                        binding.btnAddFav.visibility = View.VISIBLE
                        binding.btnRemoveFav.visibility = View.GONE

                    }
                    else if (userFavItemList!!.size > 0) {
                        if (userFavItemList.contains(itemID)) {
                            binding.btnAddFav.visibility = View.GONE
                            binding.btnRemoveFav.visibility = View.VISIBLE
                        } else {
                            binding.btnAddFav.visibility = View.VISIBLE
                            binding.btnRemoveFav.visibility = View.GONE
                        }
                    }
                }
            }
        })
    }

    fun displayLoadingDialog(duration: Long) {
        val loading = LoadingDialog(requireActivity())
        loading.startLoading("Deleting Item")
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                loading.isDimiss()
            }
        }, duration)
    }
}