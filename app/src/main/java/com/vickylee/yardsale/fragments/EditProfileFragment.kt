package com.vickylee.yardsale.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.vickylee.yardsale.data.UserRepository
import androidx.lifecycle.Observer
import com.vickylee.yardsale.databinding.FragmentEditProfileBinding


class EditProfileFragment : Fragment() {

    val TAG = this@EditProfileFragment
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: SharedPreferences
    private lateinit var userRepository : UserRepository
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
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var userID = prefs.getString("USER_DOC_ID", "NA")
        Log.d(TAG.toString(), "onViewCreated: $userID")
        if (userID != null) {
            getUserDetailsFromDB(userID)
        }
    }

    private fun getUserDetailsFromDB(userID: String) {
        userRepository.getUserDetailsFromDB(userID)
        userRepository.user.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                Log.d(TAG.toString(), "getUserDetailsFromDB: $it")
                binding.edtName.setText(it.name)
                binding.edtEmail.setText(it.email)
                binding.edtPhone.setText(it.phone)
                binding.edtLocation.setText(it.address)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}