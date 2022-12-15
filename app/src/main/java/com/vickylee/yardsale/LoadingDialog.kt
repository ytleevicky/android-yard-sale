package com.vickylee.yardsale

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.widget.ProgressBar
import android.widget.TextView

class LoadingDialog(val mActivity: Activity) {
    private lateinit var isDialog: AlertDialog

    @SuppressLint("MissingInflatedId")
    fun startLoading(text: String) {
        // set view
        val inflater = mActivity.layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_item, null)
        val dialogText = dialogView.findViewById<TextView>(R.id.tvLoaderText)
        dialogText.setText(text)
        // set dialog
        val builder = AlertDialog.Builder(mActivity)
        builder.setView(dialogView)
        builder.setCancelable(false)
        isDialog = builder.create()
        isDialog.show()
    }

    fun isDimiss() {
        isDialog.dismiss()
    }

}