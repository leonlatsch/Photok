/*
 *   Copyright 2020-2026 Leon Latsch
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

package dev.leonlatsch.photok.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.leonlatsch.photok.main.ui.navigation.RootRoute
import dev.leonlatsch.photok.navigation.LocalNavigator
import dev.leonlatsch.photok.ui.ObserveAsEvents
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel = hiltViewModel()) {
    val navigator = LocalNavigator.current

    ObserveAsEvents(viewModel.navigationEvents) { event ->
        when (event) {
            OnboardingNavigationEvent.Finished -> navigator.replaceAll(RootRoute.Setup)
        }
    }

    OnboardingContent(handleUiEvent = viewModel::handleUiEvent)
}

@Composable
private fun OnboardingContent(handleUiEvent: (OnboardingUiEvent) -> Unit) {
    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Onboarding",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { handleUiEvent(OnboardingUiEvent.Finish) }) {
                Text(text = "Continue")
            }
        }
    }
}

@Preview
@Composable
private fun OnboardingContentPreview() {
    AppTheme {
        OnboardingContent(handleUiEvent = {})
    }
}
