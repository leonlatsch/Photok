


package dev.leonlatsch.photok.ui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.theme.AppTheme

/**
 * Magic Button in box scope. Automatically aligned bottom end and padded.
 */
@Composable
fun BoxScope.MagicFab(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    dev.leonlatsch.photok.ui.components.MagicFab(
        label = label,
        onClick = onClick,
        modifier = modifier
            .align(Alignment.BottomEnd)
            .padding(12.dp)
    )
}

@Composable
private fun MagicFab(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.contentColorFor(FloatingActionButtonDefaults.containerColor)
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = null,
            tint = color,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = color,
        )
    }
}

@PreviewLightDark
@Composable
private fun MagicFabPreview() {
    AppTheme {
        MagicFab(
            label = stringResource(R.string.import_menu_fab_label),
            onClick = {}
        )
    }
}

package dev.leonlatsch.photok.ui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.theme.AppTheme

/**
 * Magic Button in box scope. Automatically aligned bottom end and padded.
 */
@Composable
fun BoxScope.MagicFab(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    dev.leonlatsch.photok.ui.components.MagicFab(
        label = label,
        onClick = onClick,
        modifier = modifier
            .align(Alignment.BottomEnd)
            .padding(12.dp)
    )
}

@Composable
private fun MagicFab(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.contentColorFor(FloatingActionButtonDefaults.containerColor)
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = null,
            tint = color,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = color,
        )
    }
}

@PreviewLightDark
@Composable
private fun MagicFabPreview() {
    AppTheme {
        MagicFab(
            label = stringResource(R.string.import_menu_fab_label),
            onClick = {}
        )
    }
}