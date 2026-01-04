


package dev.leonlatsch.photok.settings.ui.thirdparty

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentOssLicensesBinding
import dev.leonlatsch.photok.other.systemBarsPadding
import dev.leonlatsch.photok.uicomponnets.bindings.BindableFragment

private const val LICENSE_REPORT_FILE = "open_source_licenses.html"

/**
 * Fragment for displaying open source licenses.
 *
 * @since 1.2.1
 * @author Leon Latsch
 */
class OssLicensesFragment :
    BindableFragment<FragmentOssLicensesBinding>(R.layout.fragment_oss_licenses) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.systemBarsPadding()

        binding.ossToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.licenseWebView.loadUrl("file:///android_asset/$LICENSE_REPORT_FILE")
    }
}

package dev.leonlatsch.photok.settings.ui.thirdparty

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentOssLicensesBinding
import dev.leonlatsch.photok.other.systemBarsPadding
import dev.leonlatsch.photok.uicomponnets.bindings.BindableFragment

private const val LICENSE_REPORT_FILE = "open_source_licenses.html"

/**
 * Fragment for displaying open source licenses.
 *
 * @since 1.2.1
 * @author Leon Latsch
 */
class OssLicensesFragment :
    BindableFragment<FragmentOssLicensesBinding>(R.layout.fragment_oss_licenses) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.systemBarsPadding()

        binding.ossToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.licenseWebView.loadUrl("file:///android_asset/$LICENSE_REPORT_FILE")
    }
}