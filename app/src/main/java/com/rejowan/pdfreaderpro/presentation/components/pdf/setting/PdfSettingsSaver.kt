package com.bhuvaneshw.pdf.setting

/**
 * An interface for saving and retrieving PDF viewer settings.
 */
interface PdfSettingsSaver {
    /**
     * Saves a string value.
     *
     * @param key The key to save the value under.
     * @param value The string value to save.
     */
    fun save(key: String, value: String)

    /**
     * Saves a float value.
     *
     * @param key The key to save the value under.
     * @param value The float value to save.
     */
    fun save(key: String, value: Float)

    /**
     * Saves an integer value.
     *
     * @param key The key to save the value under.
     * @param value The integer value to save.
     */
    fun save(key: String, value: Int)

    /**
     * Saves a boolean value.
     *
     * @param key The key to save the value under.
     * @param value The boolean value to save.
     */
    fun save(key: String, value: Boolean)

    /**
     * Applies the changes to the settings.
     */
    fun apply()

    /**
     * Retrieves a string value.
     *
     * @param key The key of the value to retrieve.
     * @param default The default value to return if the key is not found.
     * @return The string value, or the default value if not found.
     */
    fun getString(key: String, default: String): String

    /**
     * Retrieves a float value.
     *
     * @param key The key of the value to retrieve.
     * @param default The default value to return if the key is not found.
     * @return The float value, or the default value if not found.
     */
    fun getFloat(key: String, default: Float): Float

    /**
     * Retrieves an integer value.
     *
     * @param key The key of the value to retrieve.
     * @param default The default value to return if the key is not found.
     * @return The integer value, or the default value if not found.
     */
    fun getInt(key: String, default: Int): Int

    /**
     * Retrieves a boolean value.
     *
     * @param key The key of the value to retrieve.
     * @param default The default value to return if the key is not found.
     * @return The boolean value, or the default value if not found.
     */
    fun getBoolean(key: String, default: Boolean): Boolean

    /**
     * Removes a value from the settings.
     *
     * @param key The key of the value to remove.
     */
    fun remove(key: String)

    /**
     * Clears all settings.
     */
    fun clearAll()
}
