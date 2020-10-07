/*
 *   Copyright 2020 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package dev.leonlatsch.photok.ui.settings

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceFragmentCompat
import dev.leonlatsch.photok.R
import kotlinx.android.synthetic.main.preference_layout_template.*

/**
 * Preference Fragment. Loads preferences from xml resource.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsToolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_galleryFragment)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}