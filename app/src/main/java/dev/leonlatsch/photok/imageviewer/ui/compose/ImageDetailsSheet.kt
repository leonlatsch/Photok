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

package dev.leonlatsch.photok.imageviewer.ui.compose

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.BindingConverters
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDetailsSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    photo: Photo,
) {
    val dateFormat = remember {
        SimpleDateFormat("E, dd. MMM. yyyy • HH:mm", Locale.getDefault())
    }

    val formattedDateImported = remember(photo) {
        dateFormat.format(photo.importedAt)
    }

    val formattedLastModified = remember(photo) {
        if (photo.lastModified != null) {
            dateFormat.format(photo.lastModified)
        } else {
            null
        }
    }

    if (visible) {
        val state = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )

        ModalBottomSheet(
            sheetState = state,
            onDismissRequest = onDismissRequest,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.view_photo_detail_import_at_label),
                    color = MaterialTheme.colorScheme.outline,
                )
                Text(
                    text = formattedDateImported,
                    fontWeight = FontWeight.Medium,
                )

                Spacer(Modifier.height(20.dp))


                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.view_photo_detail_title),
                        fontWeight = FontWeight.Medium,
                    )

                    DetailsRow(
                        icon = R.drawable.ic_image,
                        text = photo.fileName,
                        subText = stringResource(R.string.view_photo_detail_file_name_label)
                    )

                    val formattedSize = remember(photo.size) {
                        BindingConverters.formatByteSizeConverter(photo.size)
                    }

                    DetailsRow(
                        icon = R.drawable.ic_photo_size,
                        text = formattedSize,
                        subText = stringResource(R.string.view_photo_detail_size_label)
                    )

                    DetailsRow(
                        icon = R.drawable.ic_png,
                        text = photo.type.toString(),
                        subText = stringResource(R.string.view_photo_detail_file_type_label)
                    )

                    if (formattedLastModified != null) {
                        DetailsRow(
                            icon = R.drawable.ic_edit,
                            text = formattedLastModified,
                            subText = stringResource(R.string.view_photo_detail_last_modified)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun DetailsRow(
    @DrawableRes icon: Int,
    text: String,
    subText: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
        )

        Column() {
            Text(
                text = text,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
            )
            Text(
                text = subText,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
            )

        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme() {
        Scaffold() {
            ImageDetailsSheet(
                visible = true,
                onDismissRequest = {},
                photo = Photo(
                    fileName = "Preview File aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                    importedAt = System.currentTimeMillis(),
                    type = PhotoType.JPEG,
                    size = 512L,
                    lastModified = System.currentTimeMillis() - 23942334234L,
                ),
            )
        }
    }
}