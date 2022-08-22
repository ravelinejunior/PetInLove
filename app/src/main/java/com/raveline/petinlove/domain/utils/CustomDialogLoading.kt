package com.raveline.petinlove.domain.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import com.raveline.petinlove.R

class CustomDialogLoading {


    @SuppressLint("InflateParams")
    fun startLoading(activity: Activity) {
        val dialogBuilder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater

        dialogBuilder.setView(inflater.inflate(R.layout.custom_loading_dialog, null))
        dialogBuilder.setCancelable(false)

        mDialog = dialogBuilder.create()
        mDialog?.show()

    }

    fun dismissLoading() {
        mDialog?.dismiss()
    }

    companion object {
        private var mDialog: AlertDialog? = null
    }
}