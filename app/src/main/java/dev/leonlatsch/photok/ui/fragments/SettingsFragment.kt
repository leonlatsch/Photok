package dev.leonlatsch.photok.ui.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import dev.leonlatsch.photok.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}