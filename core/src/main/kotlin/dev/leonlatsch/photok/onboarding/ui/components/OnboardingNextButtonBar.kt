package dev.leonlatsch.photok.onboarding.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.core.R
import dev.leonlatsch.photok.ui.theme.Dimens

@Composable
fun OnboardingNexButtonBar(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.common_continue),
    enabled: Boolean = true,
    loading: Boolean = false,
    onNext: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Button(
            onClick = onNext,
            enabled = enabled && !loading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacingLarge)
                .padding(bottom = Dimens.spacingMedium)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = text,
                    modifier = Modifier.alpha(if (loading) 0f else 1f),
                )
                if (loading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = LocalContentColor.current,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
