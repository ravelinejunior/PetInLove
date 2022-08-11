package com.raveline.petinlove.domain.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.raveline.petinlove.data.model.UserModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


object SystemFunctions {

    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T) {
                removeObserver(this)
                observer.onChanged(t)
            }
        })
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun getExtensionFile(uri: Uri, context: Context): String {
        return MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(context.contentResolver.getType(uri)).toString()
    }

    private fun getProgressDrawable(context: Context): CircularProgressDrawable {
        return CircularProgressDrawable(context).apply {
            strokeWidth = 10f
            centerRadius = 50f
            start()
        }
    }

    fun ImageView.loadImage(uri: String?, circularProgressDrawable: CircularProgressDrawable) {
        val options = RequestOptions()
            .placeholder(circularProgressDrawable)
            .error(com.google.android.material.R.drawable.ic_mtrl_chip_close_circle)

        Glide.with(context).setDefaultRequestOptions(options)
            .load(uri)
            .centerCrop()
            .into(this)
    }

    fun replaceDotToComma(value: Double): String {
        val df = DecimalFormat("\$0.####")
        return df.format(value)
    }

    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> return true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return true
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) return true
        }

        return false
    }

    fun getLoggedUserFromPref(sharedPreferences: SharedPreferences): UserModel? {
        if (sharedPreferences.contains(USER_SAVED_SHARED_PREF_KEY)) {
            val userJson = sharedPreferences.getString(USER_SAVED_SHARED_PREF_KEY, null)
            val gson = Gson()
            return gson.fromJson(userJson, UserModel::class.java)
        } else return null
    }

    fun convertTimeStampToString(date: Date): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy 'às' hh:mm a", Locale.getDefault())
        return sdf.format(date)
    }

}