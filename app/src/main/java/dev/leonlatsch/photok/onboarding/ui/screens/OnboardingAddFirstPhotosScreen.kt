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

package dev.leonlatsch.photok.onboarding.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.core.R
import dev.leonlatsch.photok.gallery.ui.importing.ImportBottomSheetDialogFragment
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.repositories.ImportSource
import dev.leonlatsch.photok.navigation.LocalNavigator
import dev.leonlatsch.photok.onboarding.ui.OnboardingRoute
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingNexButtonBar
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingPageSubtitle
import dev.leonlatsch.photok.other.extensions.launchAndIgnoreTimer
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.pro.navigation.ProRoute
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens

@Composable
fun OnboardingAddFirstPhotosScreen() {
    val navigator = LocalNavigator.current
    val nextRoute = if (BuildConfig.PLAY) ProRoute.OnboardingOffer else OnboardingRoute.Done
    val activity = LocalActivity.current as? FragmentActivity
    val lifecycleOwner = LocalLifecycleOwner.current

    val pickFilesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isEmpty() || activity == null) return@rememberLauncherForActivityResult

        ImportBottomSheetDialogFragment.newInstance(
            uris = uris,
            importSource = ImportSource.InApp,
        ).show(activity.supportFragmentManager)
    }

    DisposableEffect(activity, lifecycleOwner) {
        val fragmentManager = activity?.supportFragmentManager
        fragmentManager?.setFragmentResultListener(
            ImportBottomSheetDialogFragment.REQUEST_KEY_IMPORT_FINISHED,
            lifecycleOwner,
        ) { _, _ ->
            navigator.navigate(nextRoute)
        }

        onDispose {
            fragmentManager?.clearFragmentResultListener(ImportBottomSheetDialogFragment.REQUEST_KEY_IMPORT_FINISHED)
        }
    }

    Scaffold(
        bottomBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.navigationBarsPadding()
            ) {
                TextButton(
                    onClick = {
                        navigator.navigate(nextRoute)
                    }
                ) {
                    Text("Skip")
                }

                OnboardingNexButtonBar(text = "Import photos") {
                    pickFilesLauncher.launchAndIgnoreTimer(
                        input = PhotoType.entries.map { it.mimeType }.toTypedArray(),
                        activity = activity,
                    )
                }
            }
        }
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
            modifier = Modifier
                .padding(contentPadding)
                .padding(Dimens.spacingLarge)
        ) {
            val images = listOf(
                R.drawable.onboarding_placeholder_photo_1,
                R.drawable.onboarding_placeholder_photo_2,
                R.drawable.onboarding_placeholder_photo_3,
                R.drawable.onboarding_placeholder_photo_4,
                R.drawable.onboarding_placeholder_photo_5,
                R.drawable.onboarding_placeholder_photo_6,
                R.drawable.onboarding_placeholder_photo_7,
                R.drawable.onboarding_placeholder_photo_8,
            )

            ImagesRows(images)

            OnboardingPageSubtitle(
                text = "Encrypted on import. You can add more anytime."
            )
        }
    }
}

@Composable
private fun ImagesRows(
    images: List<Int>,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingXSmall)
    ) {
        images.chunked(4).forEach { chunk ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXSmall),
                modifier = modifier,
            ) {
                for (image in chunk) {
                    Image(
                        painter = painterResource(image),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .weight(1f)
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun OnboardingAddFirstPhotosScreenPreview() {
    AppTheme {
        OnboardingAddFirstPhotosScreen()
    }
}
