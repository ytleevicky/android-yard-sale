package com.vickylee.yardsale.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vickylee.yardsale.R
import com.vickylee.yardsale.adapters.BuyerListAdapter
import com.vickylee.yardsale.data.Item
import com.vickylee.yardsale.data.UserRepository
import com.vickylee.yardsale.databinding.FragmentItemDetailsBinding
import androidx.lifecycle.Observer



class ItemDetailsFragment : Fragment() {

    val TAG = this.toString()
    private var _binding: FragmentItemDetailsBinding? = null
    private val binding get() = _binding!!
    lateinit var userRepository: UserRepository
    private lateinit var prefs: SharedPreferences
    lateinit var userType: String
    private val args:ItemDetailsFragmentArgs by navArgs()

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
        _binding = FragmentItemDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var userID = prefs.getString("USER_DOC_ID", "NA")
        userType = prefs.getString("USER_TYPE", "NA").toString()
        Log.d(TAG.toString(), "onViewCreated: $userID")

        binding.tvItemName.text = args.item.itemName
        binding.tvItemPrice.text = args.item.itemPrice.toString()
        binding.tvItemDetails.text = args.item.itemDescription

        if (userType == "Seller") {
            binding.btnItemStatus.visibility = View.VISIBLE
            binding.ivEdit.visibility = View.VISIBLE
            var itemStatus = args.item.isItemAvailable
            if (itemStatus) {
                binding.btnItemStatus.setText("Mark Sold")
            }
            else {
                binding.btnItemStatus.setText("Mark Available")
            }
            binding.btnItemStatus.setOnClickListener {
                itemStatus = !itemStatus
                Log.d(TAG, "onViewCreated: ${itemStatus} ${args.itemID}")
                if (userID != null) {
                    userRepository.updateItemAvailability(userID, args.itemID, itemStatus)
                }
                val action = ItemDetailsFragmentDirections.actionItemDetailsFragmentToListViewFragment()
                findNavController().navigate(action)
            }

            binding.ivEdit.setOnClickListener {
                val action = ItemDetailsFragmentDirections.actionItemDetailsFragmentToEditItemFragment(args.item, args.itemID)
                findNavController().navigate(action)
            }
        }
        else {
            binding.btnAddFav.visibility = View.VISIBLE
            binding.tvSellerInfo.visibility = View.VISIBLE
            binding.ivPhone.visibility = View.VISIBLE
            binding.tvPhone.visibility = View.VISIBLE
            binding.ivLocation.visibility = View.VISIBLE
            binding.tvLocation.visibility = View.VISIBLE
            userRepository.getUserDetailsFromDB(args.item.sellerID)
            userRepository.user.observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    Log.d(TAG.toString(), "getUserDetailsFromDB: $it")
                    binding.tvPhone.text = it.phone
                    binding.tvLocation.text = it.address
                }
            })

            //Todo:Add item to favorite list
        }

    }
}