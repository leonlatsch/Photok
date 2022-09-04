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

package dev.leonlatsch.photok.recoverymenu

import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.ActivityRecoveryMenuBinding
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.uicomponnets.bindings.BindableActivity
import javax.inject.Inject

@AndroidEntryPoint
class RecoveryMenuActivity :
    BindableActivity<ActivityRecoveryMenuBinding>(R.layout.activity_recovery_menu) {

    @Inject
    override lateinit var config: Config

    @Inject
    lateinit var navigator: RecoveryMenuNavigator

    private val viewModel: RecoveryMenuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigationEvent.observe(this) {
            navigator.navigate(it, this)
        }
    }

    override fun bind(binding: ActivityRecoveryMenuBinding) {
        super.bind(binding)
        binding.viewModel = viewModel
    }
}