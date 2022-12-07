package com.vickylee.yardsale

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)
    }
    //endregion


    //region Top menu - Logout button
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    //endregion

    //region Helper functions
    private fun logout() {
        // clear value stored in sharedPrefs
        val prefs = applicationContext.getSharedPreferences("YARD_SALE_PREFS", MODE_PRIVATE)
        prefs.edit().putString("USER_DOC_ID", "").apply()
        prefs.edit().putString("USER_EMAIL", "").apply()
        prefs.edit().putString("USER_PASSWORD", "").apply()
        prefs.edit().putString("USER_TYPE", "").apply()
        prefs.edit().putString("USER_NAME", "").apply()

        // navigate to sign in screen
        goToSignIn()
    }

    private fun goToSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
    //endregion

}