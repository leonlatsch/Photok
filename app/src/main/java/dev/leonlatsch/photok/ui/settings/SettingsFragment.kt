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

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.paging.Config
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.settings.Config
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.android.synthetic.main.preference_layout_template.*
import javax.inject.Inject

/**
 * Preference Fragment. Loads preferences from xml resource.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var config: Config
    var galeryColumns = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        galeryColumns = config.getIntFromString(
                Config.GALLERY_ADVANCED_GALLERY_COLUMNS,
                Config.GALLERY_ADVANCED_GALLERY_COLUMNS_DEFAULT
            )

        val dialogGaleryCloumns =
            findPreference("gallery^advanced.galleryColumns") as Preference?

        dialogGaleryCloumns?.setOnPreferenceClickListener {
            showDialog()
            true;
        }


        settingsToolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_galleryFragment)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }

    fun showDialog() {
        val galeryPreference = PreferenceManager.getDefaultSharedPreferences(context)

        var defaultItem = -1
        if (galeryColumns == 4){
            defaultItem = 0
        }else if (galeryColumns == 5){
            defaultItem = 1
        }else if (galeryColumns == 6){
            defaultItem = 2
        }

        val listItems = arrayOf("4", "5", "6")
        val mBuilder = AlertDialog.Builder(activity)
        mBuilder.setTitle(getString(R.string.settings_gallery_columns_option))
        mBuilder.setSingleChoiceItems(listItems, defaultItem) { dialogInterface, i ->
            galeryPreference.edit().putString("gallery^advanced.galleryColumns",listItems[i]).apply()
            dialogInterface.dismiss()
        }
        mBuilder.setNeutralButton(getString(R.string.settings_gallery_columns_cancel)) { dialog, which ->
            dialog.cancel()
        }

        val mDialog = mBuilder.create()
        mDialog.show()


    }
}