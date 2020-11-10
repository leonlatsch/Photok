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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.other.restartAppLifecycle
import dev.leonlatsch.photok.ui.components.Dialogs
import dev.leonlatsch.photok.ui.process.BackupBottomSheetDialogFragment
import kotlinx.android.synthetic.main.preference_layout_template.*

/**
 * Preference Fragment. Loads preferences from xml resource.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        val changePasswordPreference = preferenceManager.findPreference<Preference>(
            KEY_CHANGE_PASSWORD
        )
        changePasswordPreference?.setOnPreferenceClickListener {
            onChangePasswordClicked()
            true
        }
        val lockSafePreference = preferenceManager.findPreference<Preference>(KEY_LOCK)
        lockSafePreference?.setOnPreferenceClickListener {
            onLockSafe()
            true
        }
        val resetSafePreference = preferenceManager.findPreference<Preference>(KEY_RESET)
        resetSafePreference?.setOnPreferenceClickListener {
            onResetSafe()
            true
        }
        val backupPreference = preferenceManager.findPreference<Preference>(KEY_BACKUP)
        backupPreference?.setOnPreferenceClickListener {
            onBackup()
            true
        }
    }

    private fun onChangePasswordClicked() {
        val dialog = ChangePasswordDialog()
        dialog.show(
            requireActivity().supportFragmentManager,
            ChangePasswordDialog::class.qualifiedName
        )
    }

    private fun onLockSafe() {
        restartAppLifecycle(requireActivity())
    }

    private fun onResetSafe() {
        Dialogs.showConfirmDialog(
            requireContext(),
            getString(R.string.settings_reset_confirmation)
        ) { _, _ ->
            viewModel.resetComponents {
                onLockSafe()
            }
        }
    }

    private fun onBackup() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/zip"
        intent.putExtra(Intent.EXTRA_TITLE, "photok_backup_${System.currentTimeMillis()}.zip")
        startActivityForResult(Intent.createChooser(intent, "Select Backup File"), REQ_BACKUP)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_BACKUP && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            uri ?: return
            val dialog = BackupBottomSheetDialogFragment(uri)
            dialog.show(
                requireActivity().supportFragmentManager,
                BackupBottomSheetDialogFragment::class.qualifiedName
            )
        }
    }

    companion object {
        const val REQ_BACKUP = 42

        const val KEY_LOCK = "lock_safe"
        const val KEY_RESET = "reset_safe"
        const val KEY_CHANGE_PASSWORD = "change_password"
        const val KEY_BACKUP = "backup_safe"
    }
}