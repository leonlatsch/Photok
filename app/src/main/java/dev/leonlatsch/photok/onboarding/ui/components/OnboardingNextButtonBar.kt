package dev.leonlatsch.photok.onboarding.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.leonlatsch.photok.ui.theme.Dimens

@Composable
fun OnboardingNexButtonBar(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onNext: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Button(
            onClick = onNext,
            enabled = enabled,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(horizontal = Dimens.spacingLarge)
        ) {
            Text(text = "Next")
        }
    }
}