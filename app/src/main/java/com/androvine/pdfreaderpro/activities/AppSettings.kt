package com.androvine.pdfreaderpro.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import com.androvine.pdfreaderpro.R
import com.androvine.pdfreaderpro.constants.Constants
import com.androvine.pdfreaderpro.database.FavoriteDBHelper
import com.androvine.pdfreaderpro.database.RecentDBHelper
import com.androvine.pdfreaderpro.databinding.ActivityAppSettingsBinding
import com.androvine.pdfreaderpro.databinding.DialogFavoriteRemoveAllFilesBinding
import com.androvine.pdfreaderpro.databinding.DialogRecentRemoveAllFilesBinding
import com.androvine.pdfreaderpro.databinding.DialogRecentRemoveFilesBinding

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


            val darkThemePref =
                findPreference<androidx.preference.ListPreference>("pref_dark_theme")
            darkThemePref?.setOnPreferenceChangeListener { _, newValue ->
                when (newValue as String) {
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


            val clearFavoritesPref =
                findPreference<androidx.preference.Preference>("pref_clear_favorite")
            val clearRecentPref =
                findPreference<androidx.preference.Preference>("pref_clear_recent")

            clearFavoritesPref?.setOnPreferenceClickListener {
                showClearFavoritesDialog()
                true
            }

            clearRecentPref?.setOnPreferenceClickListener {
                showClearRecentDialog()
                true
            }
        }

        private fun showClearRecentDialog() {

            val dialog = Dialog(requireContext())
            val dialogBinding: DialogRecentRemoveAllFilesBinding =
                DialogRecentRemoveAllFilesBinding.inflate(
                    LayoutInflater.from(context)
                )
            dialog.setContentView(dialogBinding.root)
            dialog.setCancelable(true)
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window!!.setLayout(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
            )

            val recentDBHelper = RecentDBHelper(requireContext())

            dialogBinding.cancel.setOnClickListener {
                dialog.dismiss()
            }

            dialogBinding.remove.setOnClickListener {
                dialog.dismiss()
                recentDBHelper.deleteAllRecentItem()
            }

            dialog.show()

        }

        private fun showClearFavoritesDialog() {
            val dialog = Dialog(requireContext())
            val dialogBinding: DialogFavoriteRemoveAllFilesBinding =
                DialogFavoriteRemoveAllFilesBinding.inflate(
                    LayoutInflater.from(context)
                )
            dialog.setContentView(dialogBinding.root)
            dialog.setCancelable(true)
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window!!.setLayout(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
            )

            val favoriteDBHelper = FavoriteDBHelper(requireContext())

            dialogBinding.cancel.setOnClickListener {
                dialog.dismiss()
            }

            dialogBinding.remove.setOnClickListener {
                dialog.dismiss()
                favoriteDBHelper.deleteAllFavorite()
            }

            dialog.show()

        }


    }

}