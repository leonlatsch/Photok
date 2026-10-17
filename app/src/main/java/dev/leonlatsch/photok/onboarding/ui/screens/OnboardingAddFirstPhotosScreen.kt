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
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.NavKey
import dev.leonlatsch.photok.core.R
import dev.leonlatsch.photok.gallery.ui.importing.ImportBottomSheetDialogFragment
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.repositories.ImportSource
import dev.leonlatsch.photok.navigation.LocalNavigator
import dev.leonlatsch.photok.onboarding.ui.AddFirstPhotosViewModel
import dev.leonlatsch.photok.onboarding.ui.OnboardingCardWindowInsets
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingNexButtonBar
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingPageSubtitle
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingPageTitle
import dev.leonlatsch.photok.other.extensions.launchAndIgnoreTimer
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun OnboardingAddFirstPhotosScreen() {
    val viewModel: AddFirstPhotosViewModel = hiltViewModel()

    OnboardingAddFirstPhotosContent(
        nextRoute = viewModel::nextRoute
    )
}

@Composable
fun OnboardingAddFirstPhotosContent(
    nextRoute: () -> NavKey,
) {

    val navigator = LocalNavigator.current
    val activity = LocalActivity.current as? FragmentActivity
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

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
            scope.launch {
                delay(300.milliseconds)
                navigator.navigate(nextRoute())
            }
        }

        onDispose {
            fragmentManager?.clearFragmentResultListener(ImportBottomSheetDialogFragment.REQUEST_KEY_IMPORT_FINISHED)
        }
    }

    Scaffold(
        contentWindowInsets = OnboardingCardWindowInsets,
        bottomBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.navigationBarsPadding()
            ) {
                TextButton(
                    onClick = {
                        navigator.navigate(nextRoute())
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
                .verticalScroll(rememberScrollState())
                .padding(Dimens.spacingLarge)
        ) {
            OnboardingPageTitle(text = "Import photos")

            OnboardingPageSubtitle(
                text = "Your photos are encrypted as soon as you import them. You can add more at any time."
            )

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

            EncryptingPhotoGrid(images)
        }
    }
}

private class EncryptingPhotoGridState(val photoCount: Int) {
    var appeared by mutableIntStateOf(0)
    var locked by mutableStateOf(emptySet<Int>())
}

@Composable
private fun EncryptingPhotoGrid(
    images: List<Int>,
    modifier: Modifier = Modifier,
) {
    val state = remember(images) { EncryptingPhotoGridState(images.size) }

    LaunchedEffect(state) {
        for (count in 1..state.photoCount) {
            delay(70.milliseconds)
            state.appeared = count
        }
        while (true) {
            delay(900.milliseconds)
            for (index in (0 until state.photoCount).shuffled()) {
                delay(Random.nextLong(120, 320).milliseconds)
                state.locked += index
            }
            delay(2.seconds)
            state.locked = emptySet()
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingXSmall),
        modifier = modifier,
    ) {
        images.withIndex().chunked(4).forEach { chunk ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXSmall),
            ) {
                for ((index, image) in chunk) {
                    EncryptingPhotoTile(
                        image = image,
                        appeared = index < state.appeared,
                        locked = index in state.locked,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun EncryptingPhotoTile(
    @DrawableRes image: Int,
    appeared: Boolean,
    locked: Boolean,
    modifier: Modifier = Modifier,
) {
    val appear by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
    )
    val lock by animateFloatAsState(
        targetValue = if (locked) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
    )
    val lockIcon by animateFloatAsState(
        targetValue = if (locked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                alpha = appear.coerceIn(0f, 1f)
                val scale = (0.6f + 0.4f * appear) * (1f - 0.06f * lock)
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp)),
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(8.dp * lock),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = lock }
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
        )
        Icon(
            painter = painterResource(R.drawable.ic_lock),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer {
                    alpha = lockIcon.coerceIn(0f, 1f)
                    scaleX = lockIcon
                    scaleY = lockIcon
                },
        )
    }
}

@PreviewLightDark
@Composable
private fun OnboardingAddFirstPhotosScreenPreview() {
    AppTheme {
        OnboardingAddFirstPhotosContent(
            nextRoute = { error("") }
        )
    }
}
