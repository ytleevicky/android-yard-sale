package com.vickylee.yardsale.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vickylee.yardsale.data.UserRepository
import com.vickylee.yardsale.databinding.FragmentItemDetailsBinding
import androidx.lifecycle.Observer
import com.squareup.picasso.Picasso


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
        Log.d(TAG.toString(), "onViewCreated: $userID")
        if (userID != null) {
            if (itemID != null) {
                getItemDetails(userID, itemID)
                if (userType == "Buyer") {
                    binding.tvSellerInfo.visibility = View.VISIBLE
                    binding.ivPhone.visibility = View.VISIBLE
                    binding.tvPhone.visibility = View.VISIBLE
                    binding.ivLocation.visibility = View.VISIBLE
                    binding.tvLocation.visibility = View.VISIBLE

                    if (sellerID != null) {
                        userRepository.getUserDetailsFromDB(sellerID)
                    }
                    userRepository.user.observe(viewLifecycleOwner, Observer {
                        if (it != null) {
                            Log.d(TAG.toString(), "getUserDetailsFromDB: $it")
                            binding.tvPhone.text = it.phone
                            binding.tvLocation.text = it.address
                        }
                    })
                }

                binding.btnAddFav.setOnClickListener {
                    if (itemID != null) {
                        userRepository.addItemToFavorites(itemID)
                    }
                    Toast.makeText(requireContext(), "Added to favorite.", Toast.LENGTH_SHORT).show()
                }

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

    private fun getItemDetails(userID: String, itemID: String) {
        userRepository.getItemDetails(userID, itemID)
        userRepository.item.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                binding.tvItemName.setText(it.itemName)
                binding.tvItemDetails.setText(it.itemDescription)
                binding.tvItemPrice.setText(it.itemPrice.toString())
                val imageView = binding.ivItemPic
                Picasso.with(context).load(it.itemPic).into(imageView)
                if (userType == "Seller") {
                    binding.btnItemStatus.visibility = View.VISIBLE
                    binding.fabEditItem.visibility = View.VISIBLE
                    var itemStatus = it.isItemAvailable
                    if (itemStatus) {
                        binding.btnItemStatus.setText("Mark Sold")
                    } else {
                        binding.btnItemStatus.setText("Mark Available")
                    }
                    binding.btnItemStatus.setOnClickListener {
                        itemStatus = !itemStatus

                        if (userID != null) {
                            userRepository.updateItemAvailability(userID, itemID, itemStatus)
                        }
                        val action =
                            ItemDetailsFragmentDirections.actionItemDetailsFragmentToListViewFragment()
                        findNavController().navigate(action)
                    }

                    binding.fabEditItem.setOnClickListener {
                        val action =
                            ItemDetailsFragmentDirections.actionItemDetailsFragmentToEditItemFragment()
                        findNavController().navigate(action)
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
}