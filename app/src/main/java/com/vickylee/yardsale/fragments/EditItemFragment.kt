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
import com.vickylee.yardsale.data.UserRepository
import com.vickylee.yardsale.databinding.FragmentEditItemBinding

class EditItemFragment : Fragment() {
    //region Properties
    val TAG = this.toString()
    private var _binding: FragmentEditItemBinding? = null
    private val binding get() = _binding!!
    lateinit var userRepository: UserRepository
    private lateinit var prefs: SharedPreferences
    private val args:EditItemFragmentArgs by navArgs()

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
        Log.d(TAG.toString(), "onViewCreated: $userID")
        if (userID != null) {
            binding.edtItemName.setText(args.item.itemName)
            binding.edtItemDescription.setText(args.item.itemDescription)
            binding.edtItemPrice.setText(args.item.itemPrice.toString())
            binding.btnSaveItem.setOnClickListener {
                userRepository.updateItem(userID, args.item.itemID, binding.edtItemName.text.toString(), binding.edtItemDescription.text.toString(), binding.edtItemPrice.text.toString().toDouble())
                val action = EditItemFragmentDirections.actionEditItemFragmentToListViewFragment()
                findNavController().navigate(action)
            }
        }
    }
}