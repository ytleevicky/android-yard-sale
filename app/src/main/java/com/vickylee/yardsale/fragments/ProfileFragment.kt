package com.vickylee.yardsale.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.vickylee.yardsale.R
import com.vickylee.yardsale.data.UserRepository
import com.vickylee.yardsale.databinding.FragmentProfileBinding
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    val TAG = this@ProfileFragment
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: SharedPreferences
    private lateinit var userRepository : UserRepository

    //region Lifecycle Methods
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
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var userID = prefs.getString("USER_DOC_ID", "NA")
        Log.d(TAG.toString(), "onViewCreated: $userID")
        if (userID != null) {
            getUserDetailsFromDB(userID)
        }

        binding.btnEditProfile.setOnClickListener {
            // navigate to edit profile fragment
            val action = ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment()
            findNavController().navigate(action)
        }
    }

    private fun getUserDetailsFromDB(userID: String) {
        userRepository.getUserDetailsFromDB(userID)
        userRepository.user.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                Log.d(TAG.toString(), "getUserDetailsFromDB: $it")
                binding.tvName.text = it.name
                binding.tvEmail.text = it.email
                binding.tvPhone.text = it.phone
                binding.tvLocation.text = it.address
            }
        })
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    //endregion
}