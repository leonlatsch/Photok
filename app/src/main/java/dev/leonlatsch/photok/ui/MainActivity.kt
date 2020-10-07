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

package dev.leonlatsch.photok.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.ActivityMainBinding
import dev.leonlatsch.photok.other.hide
import dev.leonlatsch.photok.other.show
import dev.leonlatsch.photok.settings.Config
import dev.leonlatsch.photok.ui.components.BindableActivity
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

/**
 * The main Activity.
 * Holds all fragments and initializes toolbar, menu, etc.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class MainActivity : BindableActivity<ActivityMainBinding>(R.layout.activity_main) {

    @Inject
    override lateinit var config: Config

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(mainToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        mainNavHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.galleryFragment -> mainAppBarLayout.show()
                    else -> mainAppBarLayout.hide()
                }
            }
    }

    /**
     * Starts the action mode on [mainToolbar].
     */
    fun startActionMode(callback: ActionMode.Callback): ActionMode? =
        startSupportActionMode(callback)

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menuMainItemSettings -> {
            mainNavHostFragment.findNavController()
                .navigate(R.id.action_galleryFragment_to_settingsFragment)
            true
        }
        R.id.menuMainItemAbout -> {
            mainNavHostFragment.findNavController()
                .navigate(R.id.action_galleryFragment_to_aboutFragment)
            true
        }
        else -> false
    }

    override fun bind(binding: ActivityMainBinding) {
        super.bind(binding)
        binding.context = this
    }
}