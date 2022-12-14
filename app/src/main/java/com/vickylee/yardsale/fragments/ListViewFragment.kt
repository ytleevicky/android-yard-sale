package com.vickylee.yardsale.fragments

import android.content.SharedPreferences
import android.os.Bundle
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
import com.vickylee.yardsale.R
import com.vickylee.yardsale.adapters.BuyerListAdapter
import com.vickylee.yardsale.data.Item
import com.vickylee.yardsale.data.OnItemClickListener
import com.vickylee.yardsale.data.UserRepository
import com.vickylee.yardsale.databinding.FragmentListViewBinding
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
        if (userType == "Seller") {
            binding.floatingActionButton.visibility = View.VISIBLE
            binding.floatingActionButton.setOnClickListener {
                // navigate to AddItemFragment
                val action = ListViewFragmentDirections.actionListViewFragmentToAddItemFragment()
                findNavController().navigate(action)
            }
            val simpleCallback: ItemTouchHelper.SimpleCallback =
                object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        return false
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        if (direction == ItemTouchHelper.LEFT) {
                            //delete the current item from DB
                            Log.d(TAG,"onSwiped: trying to delete item : " + viewHolder.adapterPosition)
                            if (userID != null) {
                                deleteItem(userID, viewHolder.adapterPosition)
                            }
                        }
                    }
                }

            val helper = ItemTouchHelper(simpleCallback)
            helper.attachToRecyclerView(binding.rvItems)
        }
        else {
            binding.floatingActionButton.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "TEST - onStart() is executing")
        itemArrayList = ArrayList()
        Log.d(TAG, "onStart: $userType")
        if (userType == "Seller") {
            userRepository.getAllItemsInSellerAccount()
        }
        else {
            userRepository.getAllSellerItems()
        }


        // recycler view
        itemAdapter = BuyerListAdapter(this.requireContext(), itemArrayList, this)
        binding.rvItems.layoutManager = LinearLayoutManager(this.requireContext())
        binding.rvItems.adapter = itemAdapter

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() is executing now... ")
        if (userType == "Seller") {
            userRepository.getAllItemsInSellerAccount()
            userRepository.allItemsInUserAccount.observe(this, Observer { itemList ->
                Log.d(TAG, "onResume: Size - ${itemList.size}")
                itemArrayList.clear()

                if (itemList != null) {
                    for (item in itemList) {
                        itemArrayList.add(Item(sellerID = item.sellerID, itemID= item.itemID, itemName = item.itemName, itemDescription = item.itemDescription, itemPrice = item.itemPrice, isItemAvailable = item.isItemAvailable, creationTimestamp = item.creationTimestamp, itemPic = item.itemPic))
                        Log.d(TAG, "onResume: $item")
                        itemAdapter?.notifyDataSetChanged()
                    }
                }
            })
        }
        else {
            userRepository.getAllSellerItems()
            userRepository.allItemsForBuyer.observe(this, Observer { itemList ->
                itemArrayList.clear()

                if (itemList != null) {
                    for (item in itemList) {
                        itemArrayList.add(Item(sellerID = item.sellerID, itemID= item.itemID, itemName = item.itemName, itemDescription = item.itemDescription, itemPrice = item.itemPrice, isItemAvailable = item.isItemAvailable, creationTimestamp = item.creationTimestamp, itemPic = item.itemPic))
                        itemAdapter?.notifyDataSetChanged()
                    }
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

    override fun onItemClicked(item: Item, position: Int) {
        Log.d(TAG, "onItemClicked: ${item}")
        prefs.edit().putString("ITEM_ID", item.itemID).apply()
        prefs.edit().putString("SELLER_ID", item.sellerID).apply()
        val action = ListViewFragmentDirections.actionListViewFragmentToItemDetailsFragment()
//        val action = if (userType == "Seller") {
//            ListViewFragmentDirections.actionListViewFragmentToItemDetailsFragment(item)
//        } else {
//            ListViewFragmentDirections.actionListViewFragmentToItemDetailsFragment(item)
//        }

        findNavController().navigate(action)
    }

    private fun deleteItem(userId: String, position: Int) {
        Log.d(TAG, "deleteItem: Trying to delete item at position $position")

        //ask for confirmation
        val confirmDialog = AlertDialog.Builder(requireContext())
        confirmDialog.setTitle("Delete")
        confirmDialog.setMessage("Are you sure you want to delete this item?")
        confirmDialog.setNegativeButton("Cancel") { dialogInterface, i ->
            itemAdapter?.notifyDataSetChanged()
            dialogInterface.dismiss()
        }
        confirmDialog.setPositiveButton("Yes") { dialogInterface, i ->
            Log.d(TAG, "deleteItem: ${userRepository.allItemsInUserAccount.value!!.get(position).itemID}")
            //delete from database
            lifecycleScope.launch {
                userRepository.deleteItem(userId, userRepository.allItemsInUserAccount.value!!.get(position).itemID)
                itemArrayList!!.removeAt(position)
                itemAdapter?.notifyDataSetChanged()
            }
        }
        confirmDialog.show()
    }
    //endregion

}