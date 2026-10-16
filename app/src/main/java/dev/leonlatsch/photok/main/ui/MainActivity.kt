/*
 *   Copyright 2020–2026 Leon Latsch
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

package dev.leonlatsch.photok.main.ui

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import coil.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.gallery.ui.navigation.GetStartTab
import dev.leonlatsch.photok.main.ui.navigation.AppNavHost
import dev.leonlatsch.photok.other.extensions.getBaseApplication
import dev.leonlatsch.photok.other.extensions.launchLifecycleAwareJob
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.domain.ScreenshotOverride
import dev.leonlatsch.photok.settings.ui.compose.LocalConfig
import dev.leonlatsch.photok.transcoding.compose.LocalEncryptedImageLoader
import dev.leonlatsch.photok.transcoding.di.EncryptedImageLoader
import dev.leonlatsch.photok.ui.systembars.ProvideSystemBarsController
import dev.leonlatsch.photok.ui.theme.AppTheme
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * The main Activity.
 * Hosts the Compose navigation.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var config: Config

    @Inject
    lateinit var getStartTab: GetStartTab

    @Inject
    lateinit var screenshotOverride: ScreenshotOverride

    @EncryptedImageLoader
    @Inject
    lateinit var encryptedImageLoader: ImageLoader

    var onOrientationChanged: (Int) -> Unit = {} // Init empty

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )

        val hasSession = getBaseApplication()?.sessionRepository?.get() != null
        super.onCreate(savedInstanceState.takeIf { hasSession })

        launchLifecycleAwareJob {
            combine(
                config.valuesFlow.map { config.securityAllowScreenshots },
                screenshotOverride.allowed,
            ) { allowedInSettings, allowedByOverride ->
                true
            }
                .distinctUntilChanged()
                .collect { allowScreenshots ->
                    if (allowScreenshots) {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    } else {
                        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }
        }

        setContent {
            AppTheme {
                ProvideSystemBarsController(window) {
                    CompositionLocalProvider(
                        LocalConfig provides config,
                        LocalEncryptedImageLoader provides encryptedImageLoader,
                    ) {
                        AppNavHost(startTab = getStartTab::invoke)
                    }
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        dispatchIntent()
    }

    private fun dispatchIntent() {
        when (intent.action) {
            Intent.ACTION_SEND -> intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { uri ->
                viewModel.addUriToSharedUriStore(uri)
            }

            Intent.ACTION_SEND_MULTIPLE ->
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.forEach { uri ->
                    viewModel.addUriToSharedUriStore(uri)
                }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        onOrientationChanged(newConfig.orientation)
    }
}