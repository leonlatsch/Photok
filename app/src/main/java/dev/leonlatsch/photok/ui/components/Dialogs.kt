/*
 *   Copyright 2020 Leon Latsch
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

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import dev.leonlatsch.photok.R

object Dialogs {

    fun showConfirmDialog(context: Context, title: String, onPositiveButtonClicked: DialogInterface.OnClickListener) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setPositiveButton(R.string.common_yes, onPositiveButtonClicked)
            .setNegativeButton(R.string.common_no, null)
            .show()
    }
}