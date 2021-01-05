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

package dev.leonlatsch.photok.ui.share

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.settings.Config
import dev.leonlatsch.photok.ui.MainActivity
import dev.leonlatsch.photok.ui.StartActivity
import dev.leonlatsch.photok.ui.components.BaseActivity
import javax.inject.Inject

/**
 * Activity to receive share data.
 *
 * @since 1.2.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class ReceiveShareActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.type != null) {
            val success = storeData()
            if (success) {
                val nextIntent = if (encryptionManager.isReady) {
                    Intent(this, MainActivity::class.java)
                } else {
                    Intent(this, StartActivity::class.java)
                }
                startActivity(nextIntent)
            }
        }
        finish()
    }

    private fun storeData(): Boolean {
        return when (intent.action) {
            Intent.ACTION_SEND -> {
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                if (uri != null) {
                    sharedData.add(uri)
                    true
                } else {
                    false
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val uris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                if (uris != null) {
                    sharedData.addAll(uris)
                    true
                } else {
                    false
                }
            }
            else -> false
        }
    }

    @Inject
    lateinit var encryptionManager: EncryptionManager

    @Inject
    override lateinit var config: Config

    companion object {
        var sharedData = arrayListOf<Uri>()
    }
}