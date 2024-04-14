/*
 *   Copyright 2020-2024 Leon Latsch
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

package dev.leonlatsch.photok.ui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.theme.AppTheme

/**
 * Magic Button in box scope. Automatically aligned bottom end and padded.
 */
@Composable
fun BoxScope.MagicFab(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    dev.leonlatsch.photok.ui.components.MagicFab(
        onClick = { /*TODO*/ },
        modifier = modifier
            .align(Alignment.BottomEnd)
            .padding(12.dp)
    )
}

@Composable
fun MagicFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = "Add",
            tint = Color.White
        )
    }
}

@Preview
@Composable
private fun MagicFabPreview() {
    AppTheme {
        MagicFab(
            onClick = {}
        )
    }
}