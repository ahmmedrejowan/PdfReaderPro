package com.rejowan.pdfreaderpro.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private val Context.passwordDataStore: DataStore<Preferences> by preferencesDataStore(name = "pdf_passwords")

/**
 * Secure storage for PDF passwords using Android Keystore encryption.
 */
class PasswordStorage(private val context: Context) {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val keyAlias = "pdf_password_key"

    init {
        // Create encryption key if it doesn't exist
        if (!keyStore.containsAlias(keyAlias)) {
            generateKey()
        }
    }

    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        val spec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey {
        return (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
    }

    /**
     * Encrypt password using Android Keystore.
     */
    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Combine IV and encrypted data
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    /**
     * Decrypt password using Android Keystore.
     */
    private fun decrypt(encryptedText: String): String {
        val combined = Base64.decode(encryptedText, Base64.DEFAULT)

        // Extract IV (GCM uses 12 bytes)
        val iv = combined.copyOfRange(0, 12)
        val encryptedBytes = combined.copyOfRange(12, combined.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * Generate a unique key for storing password based on file path.
     */
    private fun getKeyForPath(path: String): Preferences.Key<String> {
        // Use hash of path as key to avoid special characters
        val hash = path.hashCode().toString()
        return stringPreferencesKey("pwd_$hash")
    }

    /**
     * Save password for a PDF file.
     */
    suspend fun savePassword(pdfPath: String, password: String) {
        val encryptedPassword = encrypt(password)
        context.passwordDataStore.edit { preferences ->
            preferences[getKeyForPath(pdfPath)] = encryptedPassword
        }
    }

    /**
     * Get saved password for a PDF file.
     */
    suspend fun getPassword(pdfPath: String): String? {
        return try {
            val encryptedPassword = context.passwordDataStore.data
                .map { preferences -> preferences[getKeyForPath(pdfPath)] }
                .first()

            encryptedPassword?.let { decrypt(it) }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if password is saved for a PDF file.
     */
    suspend fun hasPassword(pdfPath: String): Boolean {
        return context.passwordDataStore.data
            .map { preferences -> preferences[getKeyForPath(pdfPath)] != null }
            .first()
    }

    /**
     * Remove saved password for a PDF file.
     */
    suspend fun removePassword(pdfPath: String) {
        context.passwordDataStore.edit { preferences ->
            preferences.remove(getKeyForPath(pdfPath))
        }
    }

    /**
     * Clear all saved passwords.
     */
    suspend fun clearAll() {
        context.passwordDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
