package com.tzh.itemTrackingSystem.localStorage

import android.content.Context
import android.content.SharedPreferences

class SharePreferenceHelper(context: Context) {
    val sharedPref = context.getSharedPreferences(context.packageName + LOCAL_KEY, Context.MODE_PRIVATE)
    private var prefsEditor: SharedPreferences.Editor = sharedPref.edit()

    companion object {
        const val NAME_KEY = "NAME_KEY"
        const val LOCAL_KEY = "SHAREPREFERENCE_KEY"
        const val PREVIOUS_DEVICE_ADDRESS = "PREVIOUS_DEVICE_ADDRESS"
    }

    fun getName(): String {
        return sharedPref.getString(NAME_KEY, "") ?: ""
    }

    fun saveName(name: String) {
        prefsEditor.putString(NAME_KEY, name)
        prefsEditor.apply()
    }

    fun getPreviousDeviceAddress(): String? {
//        return sharedPref.getString(PREVIOUS_DEVICE_ADDRESS, null)
        return null
    }

    fun saveDeviceAddress(address: String?) {
        prefsEditor.putString(PREVIOUS_DEVICE_ADDRESS, address)
        prefsEditor.apply()
    }
}
