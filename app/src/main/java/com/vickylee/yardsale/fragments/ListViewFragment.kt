package com.vickylee.yardsale.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.vickylee.yardsale.R
import com.vickylee.yardsale.adapters.BuyerListAdapter
import com.vickylee.yardsale.data.Item
import com.vickylee.yardsale.data.OnItemClickListener
import com.vickylee.yardsale.data.UserRepository
import com.vickylee.yardsale.databinding.FragmentListViewBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

class ListViewFragment : DialogFragment(R.layout.fragment_list_view), OnItemClickListener {

    //region Properties
    val TAG = this.toString()
    private var _binding: FragmentListViewBinding? = null
    private val binding get() = _binding!!
    lateinit var userRepository: UserRepository
    lateinit var itemArrayList: ArrayList<Item>
    var itemAdapter: BuyerListAdapter? = null
    //endregion

    //region Lifecycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "TEST - onCreate() is executing")

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

        binding.floatingActionButton.setOnClickListener {
            // navigate to AddItemFragment
            val action = ListViewFragmentDirections.actionListViewFragmentToAddItemFragment()
            findNavController().navigate(action)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "TEST - onStart() is executing")
        itemArrayList = ArrayList()
        userRepository.getAllItemsInSellerAccount()

        // recycler view
        itemAdapter = BuyerListAdapter(this.requireContext(), itemArrayList, this)
        binding.rvItems.layoutManager = LinearLayoutManager(this.requireContext())
        binding.rvItems.adapter = itemAdapter
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() is executing now... ")
        userRepository.getAllItemsInSellerAccount()

        userRepository.allItemsInUserAccount.observe(this, Observer { itemList ->
            Log.d(TAG, "onResume: Size - ${itemList.size}")
            itemArrayList.clear()

            if (itemList != null) {
                for (item in itemList) {
                    itemArrayList.add(Item(itemName = item.itemName, itemDescription = item.itemDescription, itemPrice = item.itemPrice, isItemAvailable = item.isItemAvailable, creationTimestamp = item.creationTimestamp))
                    Log.d(TAG, "onResume: $item")
                    itemAdapter?.notifyDataSetChanged()
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() is executing now... ")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClicked(item: Item) {
        Toast.makeText(context, "${item.itemName} selected", Toast.LENGTH_SHORT).show()
    }
    //endregion

}