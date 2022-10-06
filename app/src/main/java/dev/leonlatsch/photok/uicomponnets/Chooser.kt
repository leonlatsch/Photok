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

package dev.leonlatsch.photok.uicomponnets

import android.content.Intent
import androidx.fragment.app.Fragment
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.other.extensions.startActivityForResultAndIgnoreTimer
import dev.leonlatsch.photok.uicomponnets.Chooser.Builder
import pub.devrel.easypermissions.EasyPermissions

/**
 * Intent chooser that requests permissions.
 *
 * Uer [Builder] to set request codes and permissions.
 *
 * @sample dev.leonlatsch.photok.main.gallery.ImportMenuDialog.startSelectBackup
 *
 * @since 1.3.0
 * @author Leon Latsch
 */
class Chooser {

    internal var message: String? = null
    internal var mimeType: String? = null
    internal var requestCode: Int = 0
    internal var permissionRequestCode: Int = 0
    internal var permission: String? = null
    internal var allowMultiple: Boolean = false

    /**
     * Show a document chooser.
     * Or request the [permission]
     */
    fun show(fragment: Fragment) {
        if (permission == null || EasyPermissions.hasPermissions(
                fragment.requireContext(),
                permission
            )
        ) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = mimeType
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            fragment.startActivityForResultAndIgnoreTimer(
                Intent.createChooser(intent, message),
                requestCode
            )
        } else {
            EasyPermissions.requestPermissions(
                fragment,
                fragment.requireContext().getString(R.string.import_permission_rationale),
                permissionRequestCode,
                permission
            )
        }
    }

    /**
     * Class to build a [Chooser].
     */
    class Builder {

        private val chooser = Chooser()

        /**
         * Set message
         */
        fun message(message: String): Builder {
            chooser.message = message
            return this
        }

        /**
         * Set mimeType
         */
        fun mimeType(mimeType: String): Builder {
            chooser.mimeType = mimeType
            return this
        }

        /**
         * Set requestCode
         */
        fun requestCode(requestCode: Int): Builder {
            chooser.requestCode = requestCode
            return this
        }

        /**
         * Set permissionCode
         */
        fun permissionCode(permissionCode: Int): Builder {
            chooser.permissionRequestCode = permissionCode
            return this
        }

        /**
         * Set permission
         */
        fun permission(permission: String): Builder {
            chooser.permission = permission
            return this
        }

        /**
         * Set allowMultiple
         */
        fun allowMultiple(): Builder {
            chooser.allowMultiple = true
            return this
        }

        /**
         * Show the chooser
         */
        fun show(fragment: Fragment) = chooser.show(fragment)
    }
}