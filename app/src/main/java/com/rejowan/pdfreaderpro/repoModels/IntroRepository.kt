package com.rejowan.pdfreaderpro.repoModels

import android.content.Context
import android.content.SharedPreferences

class IntroRepository(context: Context) {

    companion object {
        private const val PREF_NAME = "first_time"
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, 0)

    fun setFirstTimeLaunch(isFirstTime: Boolean) {
        sharedPreferences.edit().putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime).apply()
    }

    fun isFirstTimeLaunch(): Boolean {
        return sharedPreferences.getBoolean(IS_FIRST_TIME_LAUNCH, true)
    }
}
