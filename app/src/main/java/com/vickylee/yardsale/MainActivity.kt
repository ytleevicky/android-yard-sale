package com.vickylee.yardsale

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.vickylee.yardsale.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    //region Properties
    val TAG = this@MainActivity.toString()
    private lateinit var binding: ActivityMainBinding
    //endregion

    //region Android Lifecycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setup view binding
        this.binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // setup bottom navigation
        val bottomNavigationView = binding.bottomNavigationView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)
    }
    //endregion
}