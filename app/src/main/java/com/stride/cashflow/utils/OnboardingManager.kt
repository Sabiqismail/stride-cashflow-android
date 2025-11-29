package com.stride.cashflow.utils

import android.content.Context
import android.content.SharedPreferences

object OnboardingManager {
    private const val PREFS_NAME = "StrideAppPrefs"
    private const val KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun hasSeenOnboarding(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_HAS_SEEN_ONBOARDING, false)
    }

    fun setOnboardingSeen(context: Context, seen: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_HAS_SEEN_ONBOARDING, seen).apply()
    }
}
