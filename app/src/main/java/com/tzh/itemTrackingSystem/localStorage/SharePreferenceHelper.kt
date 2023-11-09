package com.tzh.itemTrackingSystem.localStorage

import android.content.Context
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharePreferenceHelper(context: Context) {
//    val sharedPref = context.getSharedPreferences(context.packageName + LOCAL_KEY, Context.MODE_PRIVATE)
//    private var prefsEditor: SharedPreferences.Editor = sharedPref.edit()

    companion object {
        const val NAME_KEY = "NAME_KEY"
        const val LOCAL_KEY = "SHAREPREFERENCE_KEY"
        const val PREVIOUS_DEVICE_ADDRESS = "PREVIOUS_DEVICE_ADDRESS"
    }

    private var mBluetoothAddress by context.sharePreference(
        PREVIOUS_DEVICE_ADDRESS,
        ""
    )


    //    fun getName(): String {
//        return sharedPref.getString(NAME_KEY, "") ?: ""
//    }
//
//    fun saveName(name: String) {
//        prefsEditor.putString(NAME_KEY, name)
//        prefsEditor.apply()
//    }
//
    fun getPreviousDeviceAddress(): String? {
        return mBluetoothAddress
    }

    fun saveDeviceAddress(address: String?) {
        mBluetoothAddress = address
    }
}

inline fun <reified T> Context.sharePreference(key: String, defaultValue: T) =
    SharePreferenceDelegate(this, key, defaultValue)

class SharePreferenceDelegate<T>(
    val context: Context, private val key: String, private val defaultValue: T
) : ReadWriteProperty<Any?, T?> {

    private val sharePreferenceHelper by lazy {
        context.getSharedPreferences(context.packageName + SharePreferenceHelper.LOCAL_KEY, Context.MODE_PRIVATE)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return when (defaultValue) {
            is Int? -> {
                sharePreferenceHelper.getInt(key, 0) as T?
            }

            Int -> {
                sharePreferenceHelper.getInt(key, 0) as T
            }

            is Long? -> {
                sharePreferenceHelper.getLong(key, 0) as T?
            }

            Long -> {
                sharePreferenceHelper.getLong(key, 0) as T
            }

            is String? -> {
                sharePreferenceHelper.getString(key, null) as T?
            }

            String -> {
                sharePreferenceHelper.getString(key, null) as T
            }

            is Boolean? -> {
                sharePreferenceHelper.getBoolean(key, false) as T?
            }

            Boolean -> {
                sharePreferenceHelper.getBoolean(key, false) as T
            }

            else -> {
                throw IllegalArgumentException("Unsupported type")
            }
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        with(sharePreferenceHelper.edit()) {
            when (value) {
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
                // Add more types as needed (Boolean, Float, etc.)
                else -> throw IllegalArgumentException("Unsupported type")
            }
            apply()
        }
    }

}
