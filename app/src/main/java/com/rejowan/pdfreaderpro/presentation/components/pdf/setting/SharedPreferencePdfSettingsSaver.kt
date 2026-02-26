package com.bhuvaneshw.pdf.setting

import android.content.Context
import android.content.SharedPreferences

/**
 * A [PdfSettingsSaver] implementation that uses [SharedPreferences] to store settings.
 *
 * @param sharedPreferences The [SharedPreferences] instance to use.
 */
class SharedPreferencePdfSettingsSaver(
    private val sharedPreferences: SharedPreferences
) : PdfSettingsSaver {

    private val editor = sharedPreferences.edit()

    /**
     * Creates a new instance of [SharedPreferencePdfSettingsSaver].
     *
     * @param context The context to use.
     * @param name The name of the shared preferences file.
     * @param mode The operating mode.
     */
    constructor(context: Context, name: String, mode: Int = Context.MODE_PRIVATE) : this(
        context.getSharedPreferences(name, mode)
    )

    override fun save(key: String, value: String) {
        editor.putString(key, value)
    }

    override fun save(key: String, value: Float) {
        editor.putFloat(key, value)
    }

    override fun save(key: String, value: Int) {
        editor.putInt(key, value)
    }

    override fun save(key: String, value: Boolean) {
        editor.putBoolean(key, value)
    }

    override fun apply() {
        editor.apply()
    }

    override fun getString(key: String, default: String): String {
        return sharedPreferences.getString(key, default) ?: default
    }

    override fun getFloat(key: String, default: Float): Float {
        return sharedPreferences.getFloat(key, default)
    }

    override fun getInt(key: String, default: Int): Int {
        return sharedPreferences.getInt(key, default)
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    override fun remove(key: String) {
        editor.remove(key)
    }

    override fun clearAll() {
        editor.clear()
    }

}
