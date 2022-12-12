package com.vickylee.yardsale.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.vickylee.yardsale.R
import com.vickylee.yardsale.adapters.BuyerListAdapter
import com.vickylee.yardsale.data.Item
import com.vickylee.yardsale.data.OnItemClickListener
import com.vickylee.yardsale.data.UserRepository
import com.vickylee.yardsale.databinding.FragmentAddFavoriteBinding

class AddFavoriteFragment : Fragment(R.layout.fragment_add_favorite), OnItemClickListener {

    //region Properties
    val TAG = this.toString()
    private var _binding: FragmentAddFavoriteBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: SharedPreferences
    lateinit var userRepository: UserRepository
    lateinit var favItemArrayList: ArrayList<Item>
    var itemAdapter: BuyerListAdapter? = null
    //endregion

    //region Lifecycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs =
            requireContext().getSharedPreferences("YARD_SALE_PREFS", AppCompatActivity.MODE_PRIVATE)

        userRepository = UserRepository(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAddFavoriteBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

        favItemArrayList = ArrayList()
        userRepository.getAllSellerItems()

        itemAdapter = BuyerListAdapter(this.requireContext(), favItemArrayList, this)
        binding.rvFavItems.layoutManager = LinearLayoutManager(this.requireContext())
        binding.rvFavItems.adapter = itemAdapter
    }

    override fun onResume() {
        super.onResume()

        userRepository.getAllSellerItems()
        userRepository.allItemsForBuyer.observe(this, Observer { itemList ->
            favItemArrayList.clear()

            val userFavItems = prefs.getStringSet("USER_FAV_ITEMS", null)

            if (itemList != null && userFavItems != null && userFavItems.size > 0) {
                for (item in itemList) {
                    if (userFavItems.contains(item.itemID)) {
                        favItemArrayList.add(
                            Item(
                                sellerID = item.sellerID,
                                itemID = item.itemID,
                                itemName = item.itemName,
                                itemDescription = item.itemDescription,
                                itemPrice = item.itemPrice,
                                isItemAvailable = item.isItemAvailable,
                                creationTimestamp = item.creationTimestamp,
                                itemPic = item.itemPic
                            )
                        )
                        Log.d(TAG, "onResume: $item")
                        itemAdapter?.notifyDataSetChanged()
                    }
                }
            }

        })
    }

    //endregion

    override fun onItemClicked(item: Item, position: Int) {
        val action = AddFavoriteFragmentDirections.actionAddFavoriteFragmentToItemDetailsFragment(item)
        findNavController().navigate(action)
    }

}


