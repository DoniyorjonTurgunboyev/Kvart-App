package uz.gita.kvartarena.utils

import android.content.SharedPreferences
import java.lang.ClassCastException

fun <T> SharedPreferences.save(key: String, value: T) {
    edit().apply {
        when (value) {
            is String -> putString(key, value)
            is Float -> putFloat(key, value)
            is Int -> putInt(key, value)
            is Boolean -> putBoolean(key, value)
            is Long -> putLong(key, value)
            else -> throw ClassCastException()
        }
    }.apply()
}

fun <T> SharedPreferences.get(key: String, defValue: T): T {
    return when (defValue) {
        is String -> getString(key, defValue)
        is Float -> getFloat(key, defValue)
        is Int -> getInt(key, defValue)
        is Boolean -> getBoolean(key, defValue)
        is Long -> getLong(key, defValue)
        else -> throw ClassCastException()
    } as T
}