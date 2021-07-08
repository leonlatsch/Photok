/*
 *   Copyright 2020-2021 Leon Latsch
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
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.findNavController
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentAboutBinding
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.other.openUrl
import dev.leonlatsch.photok.ui.components.bindings.BindableFragment
import dev.leonlatsch.photok.ui.news.NewsDialog

/**
 * Fragment to display a info about the app and some links.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class AboutFragment : BindableFragment<FragmentAboutBinding>(R.layout.fragment_about) {

    val version = BuildConfig.VERSION_NAME

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        setToolbar(binding.aboutToolbar)
        binding.aboutToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_about, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuAboutNews -> NewsDialog().show(childFragmentManager)
        }
        return true
    }

    /**
     * Open the website in new activity.
     */
    fun openWebsite() {
        openUrl(requireContext(), getString(R.string.about_website_url))
    }

    /**
     * * Open the third party in new activity.
     */
    fun openThirdPartySoftware() {
        findNavController().navigate(R.id.action_aboutFragment_to_ossLicensesFragment)
    }

    /**
     * Open the privacy policy in new activity.
     */
    fun openPrivacyPolicy() {
        openUrl(requireContext(), getString(R.string.about_privacy_policy_url))
    }

    override fun bind(binding: FragmentAboutBinding) {
        super.bind(binding)
        binding.context = this
    }

}