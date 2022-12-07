package com.vickylee.yardsale

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
    }

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

    //region Helper functions
    private fun showAlertBoxForLogout() {
        val builder = AlertDialog.Builder(this@MainActivity2)
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