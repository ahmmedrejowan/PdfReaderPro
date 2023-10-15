package com.androvine.pdfreaderpro.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import com.androvine.pdfreaderpro.R
import com.androvine.pdfreaderpro.constants.Constants
import com.androvine.pdfreaderpro.databinding.ActivityAppSettingsBinding

@Suppress("DEPRECATION")
class AppSettings : AppCompatActivity() {

    private val binding: ActivityAppSettingsBinding by lazy {
        ActivityAppSettingsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SettingFragment()).commit()


    }

    class SettingFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.my_prefs, rootKey)

            setUpListeners()
        }

        private fun setUpListeners() {

            val appVersionPref = findPreference<androidx.preference.Preference>("pref_version")
            val contactUsPref = findPreference<androidx.preference.Preference>("pref_contact")
            val rateUsPref = findPreference<androidx.preference.Preference>("pref_rate")
            val privacyPolicyPref = findPreference<androidx.preference.Preference>("pref_privacy")
            val createdByPref = findPreference<androidx.preference.Preference>("pref_creator")

            appVersionPref?.summary = "Version " + Constants.appVersion
            createdByPref?.summary = "" + Constants.companyName

            contactUsPref?.setOnPreferenceClickListener {
                val email = Intent(Intent.ACTION_SEND)
                email.putExtra(Intent.EXTRA_EMAIL, arrayOf(Constants.contactEmail))
                email.putExtra(Intent.EXTRA_SUBJECT, "Feedback")
                email.putExtra(Intent.EXTRA_TEXT, "Dear " + Constants.companyName + ",")
                email.type = "message/rfc822"
                startActivity(Intent.createChooser(email, "Choose an Email client :"))
                true
            }

            rateUsPref?.setOnPreferenceClickListener {
                val appPackageName = requireContext().packageName
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            android.net.Uri.parse("market://details?id=$appPackageName")
                        )
                    )
                } catch (anfe: android.content.ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                        )
                    )
                }
                true
            }

            privacyPolicyPref?.setOnPreferenceClickListener {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    android.net.Uri.parse(Constants.privacyPolicyUrl)
                )
                startActivity(browserIntent)
                true
            }


            val darkThemePref = findPreference<androidx.preference.ListPreference>("pref_dark_theme")
            darkThemePref?.setOnPreferenceChangeListener { preference, newValue ->
                val themeMode = newValue as String
                when (themeMode) {
                    "light_theme" -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                    "dark_theme" -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                    else -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                }
                requireActivity().recreate()
                true
            }

        }


    }

}