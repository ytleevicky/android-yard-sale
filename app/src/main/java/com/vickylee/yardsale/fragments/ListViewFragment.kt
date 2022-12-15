package com.vickylee.yardsale.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vickylee.yardsale.LoadingDialog
import com.vickylee.yardsale.R
import com.vickylee.yardsale.adapters.BuyerListAdapter
import com.vickylee.yardsale.data.Item
import com.vickylee.yardsale.data.OnItemClickListener
import com.vickylee.yardsale.data.UserRepository
import com.vickylee.yardsale.databinding.FragmentListViewBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ListViewFragment : DialogFragment(R.layout.fragment_list_view), OnItemClickListener {

    //region Properties
    val TAG = this.toString()
    private var _binding: FragmentListViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: SharedPreferences
    lateinit var userRepository: UserRepository
    lateinit var itemArrayList: ArrayList<Item>
    lateinit var userType: String
    var itemAdapter: BuyerListAdapter? = null
    //endregion

    //region Lifecycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "TEST - onCreate() is executing")
        prefs = requireContext().getSharedPreferences("YARD_SALE_PREFS",
            AppCompatActivity.MODE_PRIVATE
        )
        userRepository = UserRepository((requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "TEST - onCreateView() is executing")

        // setup view binding
        _binding = FragmentListViewBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "TEST - onViewCreated() is executing")
        var userID = prefs.getString("USER_DOC_ID", "NA")
        userType = prefs.getString("USER_TYPE", "NA").toString()

        // SHow add button to seller and allow seller to delete item on swipe
        if (userType == "Seller") {
            binding.floatingActionButton.visibility = View.VISIBLE
            binding.floatingActionButton.setOnClickListener {
                // navigate to AddItemFragment
                val action = ListViewFragmentDirections.actionListViewFragmentToAddItemFragment()
                findNavController().navigate(action)
            }
        }
        // For nuyer hide add button
        else {
            binding.floatingActionButton.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "TEST - onStart() is executing")

        Log.d(TAG, "onStart: $userType")
//        if (userType == "Seller") {
//            userRepository.getAllItemsInSellerAccount()
//        }
//        else {
//            userRepository.getAllSellerItems()
//        }
        itemArrayList = ArrayList()
        // recycler view
        itemAdapter = BuyerListAdapter(this.requireContext(), itemArrayList, this)
        binding.rvItems.layoutManager = LinearLayoutManager(this.requireContext())
        binding.rvItems.adapter = itemAdapter



    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() is executing now... ")
        Log.d(TAG, "onResume: after adding")
        if (userType == "Seller") {
            userRepository.getAllItemsInSellerAccount()
            //itemArrayList.clear()
            userRepository.allItemsInUserAccount.observe(this, Observer { itemList ->
                Log.d(TAG, "onResume: Size - ${itemList.size}")
                //itemArrayList.clear()

                if (itemList != null) {
                    itemArrayList.clear()
                    for (item in itemList) {
                        itemArrayList.add(Item(sellerID = item.sellerID, itemID= item.itemID, itemName = item.itemName, itemDescription = item.itemDescription, itemPrice = item.itemPrice, isItemAvailable = item.isItemAvailable, creationTimestamp = item.creationTimestamp, itemPic = item.itemPic))
                        Log.d(TAG, "onResume: $item")
                    }
                    itemAdapter?.notifyDataSetChanged()
                }
                else {
                    itemArrayList.clear()
                }
            })
        }
        else {
            userRepository.getAllSellerItems()
            userRepository.allItemsForBuyer.observe(this, Observer { itemList ->
                if (itemList != null) {
                    itemArrayList.clear()
                    for (item in itemList) {
                        itemArrayList.add(Item(sellerID = item.sellerID, itemID= item.itemID, itemName = item.itemName, itemDescription = item.itemDescription, itemPrice = item.itemPrice, isItemAvailable = item.isItemAvailable, creationTimestamp = item.creationTimestamp, itemPic = item.itemPic))

                    }
                    itemAdapter?.notifyDataSetChanged()
                }
            })
        }


    }

    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() is executing now... ")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Navigate to item details screen
    override fun onItemClicked(item: Item, position: Int) {
        prefs.edit().putString("ITEM_ID", item.itemID).apply()
        prefs.edit().putString("SELLER_ID", item.sellerID).apply()
        val action = ListViewFragmentDirections.actionListViewFragmentToItemDetailsFragment()
        findNavController().navigate(action)
    }

    //endregion

}