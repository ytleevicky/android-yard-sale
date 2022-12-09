package com.vickylee.yardsale

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.vickylee.yardsale.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    //region Properties
    val TAG = this@MainActivity.toString()
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
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

        // setup back button navigation
        this.navController = navHostFragment.navController
        //appBarConfiguration = AppBarConfiguration(navController.graph)
        appBarConfiguration = AppBarConfiguration(bottomNavigationView.menu)

        setupActionBarWithNavController(navController, appBarConfiguration)
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
                showAlertBoxForLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    //endregion

    //region Add Back button
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
    //endregion

    //region Helper functions
    private fun showAlertBoxForLogout() {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setMessage("Are you sure you want to logout?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                logout()
            }
            .setNegativeButton("No") { dialog, id ->
                // Dismiss the dialog
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun logout() {
        var rememberUsernameAndPassword = false
        if (intent != null) {
            rememberUsernameAndPassword = intent.getBooleanExtra("EXTRA_REMEMBER_ME", false)
        }

        // clear value stored in sharedPrefs
        val prefs = applicationContext.getSharedPreferences("YARD_SALE_PREFS", MODE_PRIVATE)
        prefs.edit().putString("USER_DOC_ID", "").apply()
        prefs.edit().putString("USER_TYPE", "").apply()
        prefs.edit().putString("USER_NAME", "").apply()

        if (!rememberUsernameAndPassword) {
            prefs.edit().putString("USER_EMAIL", "").apply()
            prefs.edit().putString("USER_PASSWORD", "").apply()
        }

        //supportFragmentManager.beginTransaction().remove(ListViewFragment).commit()

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