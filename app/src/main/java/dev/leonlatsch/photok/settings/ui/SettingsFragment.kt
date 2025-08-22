/*
 *   Copyright 2020-2022 Leon Latsch
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

package dev.leonlatsch.photok.settings.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.backup.domain.BackupStrategy
import dev.leonlatsch.photok.backup.ui.BackupBottomSheetDialogFragment
import dev.leonlatsch.photok.databinding.BindingConverters
import dev.leonlatsch.photok.other.extensions.launchAndIgnoreTimer
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.other.openUrl
import dev.leonlatsch.photok.other.setAppDesign
import dev.leonlatsch.photok.other.statusBarPadding
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.ui.changepassword.ChangePasswordDialog
import dev.leonlatsch.photok.settings.ui.checkpassword.CheckPasswordDialog
import dev.leonlatsch.photok.settings.ui.hideapp.ToggleAppVisibilityDialog
import dev.leonlatsch.photok.uicomponnets.Dialogs
import javax.inject.Inject

fun createBackupFilename(): String {
    return "photok_backup_${BindingConverters.millisToFormattedDateConverter(System.currentTimeMillis())}.zip"
}

/**
 * Preference Fragment. Loads preferences from xml resource.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel: SettingsViewModel by viewModels()
    private var toolbar: Toolbar? = null

    private val createBackupLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
            uri ?: return@registerForActivityResult
            BackupBottomSheetDialogFragment(uri, BackupStrategy.Name.Default).show(requireActivity().supportFragmentManager)
        }

    @Inject
    lateinit var config: Config

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.statusBarPadding()

        toolbar = view.findViewById(R.id.settingsToolbar)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        setupAppCategory()
        setupSecurityCategory()
        setupAdvancedCategory()
        setupOtherCategory()
    }


    private fun setupAppCategory() {
        addCallbackTo<ListPreference>(Config.SYSTEM_DESIGN) {
            setAppDesign(it as String)
        }
    }

    private fun setupSecurityCategory() {
        addActionTo(KEY_ACTION_CHANGE_PASSWORD) {
            ChangePasswordDialog().show(childFragmentManager)
        }

        addActionTo(KEY_ACTION_HIDE_APP) {
            ToggleAppVisibilityDialog().show(childFragmentManager)
        }

        configurePhoneDialPreference()
    }

    private fun setupAdvancedCategory() {
        addActionTo(KEY_ACTION_RESET) {
            CheckPasswordDialog {
                Dialogs.showConfirmDialog(
                    requireContext(),
                    getString(R.string.settings_advanced_reset_confirmation)
                ) { _, _ ->
                    viewModel.resetComponents()
                }
            }.show(childFragmentManager)
        }


        addActionTo(KEY_ACTION_BACKUP) {
            createBackupLauncher.launchAndIgnoreTimer(
                input = createBackupFilename(),
                activity = activity,
            )
        }
    }

    private fun setupOtherCategory() {
        val email = getString(R.string.settings_other_feedback_mail_emailaddress)
        val subject =
            "${getString(R.string.settings_other_feedback_mail_subject)} (App ${BuildConfig.VERSION_NAME} / Android ${Build.VERSION.RELEASE})"
        val text = getString(R.string.settings_other_feedback_mail_body)

        addActionTo(KEY_ACTION_FEEDBACK) {
            val emailIntent = Intent(
                Intent.ACTION_SENDTO,
                Uri.parse("mailto:$email?subject=$subject&body=$text")
            ).apply {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, text)
            }
            startActivity(
                Intent.createChooser(
                    emailIntent,
                    getString(R.string.settings_other_feedback_title)
                )
            )
        }

        addActionTo(KEY_ACTION_DONATE) {
            openUrl(getString(R.string.settings_other_donate_url))
        }

        addActionTo(KEY_ACTION_SOURCECODE) {
            openUrl(getString(R.string.settings_other_sourcecode_url))
        }

        addActionTo(KEY_ACTION_CREDITS) {
            findNavController().navigate(R.id.action_settingsFragment_to_creditsFragment)
        }

        addActionTo(KEY_ACTION_ABOUT) {
            findNavController().navigate(R.id.action_settingsFragment_to_aboutFragment)
        }
    }

    private fun configurePhoneDialPreference() {
        val dialPreference = findPreference<EditTextPreference>(Config.SECURITY_DIAL_LAUNCH_CODE)
        dialPreference?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            it.addTextChangedListener { editable ->
                if (editable?.length!! < 1) {
                    it.setText(0.toString())
                }
            }
        }
    }

    private fun addActionTo(preferenceId: String, action: () -> Unit) {
        preferenceManager
            .findPreference<Preference>(preferenceId)
            ?.setOnPreferenceClickListener {
                action()
                true
            }
    }

    private fun <T : Preference> addCallbackTo(preferenceId: String, action: (value: Any) -> Unit) {
        preferenceManager.findPreference<T>(preferenceId)
            ?.setOnPreferenceChangeListener { _, newValue ->
                action(newValue)
                true
            }
    }

    companion object {
        const val KEY_ACTION_RESET = "action_reset_safe"
        const val KEY_ACTION_CHANGE_PASSWORD = "action_change_password"
        const val KEY_ACTION_CHECK_PASSWORD = "action_check_password"
        const val KEY_ACTION_HIDE_APP = "action_hide_app"
        const val KEY_ACTION_BACKUP = "action_backup_safe"
        const val KEY_ACTION_FEEDBACK = "action_feedback"
        const val KEY_ACTION_DONATE = "action_donate"
        const val KEY_ACTION_SOURCECODE = "action_sourcecode"
        const val KEY_ACTION_CREDITS = "action_credits"
        const val KEY_ACTION_ABOUT = "action_about"
    }
}
