/*
 *   Copyright 2020-2021 Leon Latsch
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

package dev.leonlatsch.photok.ui.settings.thirdparty

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.other.openUrl

/**
 * View Holder for displaying a [OssEntry].
 *
 * @since 1.2.1
 * @author Leon Latsch
 */
class OssViewHolder(
    private val context: Context,
    view: View
) : RecyclerView.ViewHolder(view) {

    private val licenseGeneral = itemView.findViewById<LinearLayout>(R.id.itemOssGeneral)
    private val project = itemView.findViewById<AppCompatTextView>(R.id.itemOssProject)
    private val version = itemView.findViewById<AppCompatTextView>(R.id.itemOssVersion)
    private val license = itemView.findViewById<AppCompatTextView>(R.id.itemOssLicense)

    private val licenseDetails = itemView.findViewById<LinearLayout>(R.id.itemOssDetails)
    private val packageName = itemView.findViewById<AppCompatTextView>(R.id.itemOssPackageName)

    fun bindTo(ossEntry: OssEntry) {
        project.text = ossEntry.project
        version.text = ossEntry.version
        licenseGeneral.setOnClickListener {
            openUrl(context, ossEntry.url)
        }

        ossEntry.licenses.firstOrNull().let {
            it ?: return@let
            license.text = it.license
            licenseDetails.setOnClickListener { _ ->
                openUrl(context, it.licenseUrl)
            }
        }

        packageName.text = ossEntry.dependency
    }
}