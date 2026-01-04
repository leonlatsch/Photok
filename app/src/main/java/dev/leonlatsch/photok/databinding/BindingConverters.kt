


package dev.leonlatsch.photok.databinding

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Converters for data binding.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
object BindingConverters {

    private val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
    private val decimalFormat = DecimalFormat("0")

    private const val GB_SUFFIX = " GB"
    private const val MB_SUFFIX = " MB"
    private const val KB_SUFFIX = " KB"
    private const val BYTES_SUFFIX = " Bytes"

    fun millisToFormattedDateConverter(millis: Long?): String? {
        millis ?: return "-"
        val date = Date(millis)
        return dateFormat.format(date)
    }

    fun formatByteSizeConverter(bytes: Long): String {
        val kiloBytes = bytes / 1024
        val megaBytes = kiloBytes / 1024
        val gigaBytes = megaBytes / 1024

        return when {
            gigaBytes >= 1 -> {
                decimalFormat.format(gigaBytes) + GB_SUFFIX
            }
            megaBytes >= 1 -> {
                decimalFormat.format(megaBytes) + MB_SUFFIX
            }
            kiloBytes >= 1 -> {
                decimalFormat.format(kiloBytes) + KB_SUFFIX
            }
            else -> {
                decimalFormat.format(bytes) + BYTES_SUFFIX
            }
        }
    }

    fun toStringConverter(obj: Any?) = obj.toString()

    fun upperCaseConverter(str: String?): String? = str?.uppercase()
}

package dev.leonlatsch.photok.databinding

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Converters for data binding.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
object BindingConverters {

    private val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
    private val decimalFormat = DecimalFormat("0")

    private const val GB_SUFFIX = " GB"
    private const val MB_SUFFIX = " MB"
    private const val KB_SUFFIX = " KB"
    private const val BYTES_SUFFIX = " Bytes"

    fun millisToFormattedDateConverter(millis: Long?): String? {
        millis ?: return "-"
        val date = Date(millis)
        return dateFormat.format(date)
    }

    fun formatByteSizeConverter(bytes: Long): String {
        val kiloBytes = bytes / 1024
        val megaBytes = kiloBytes / 1024
        val gigaBytes = megaBytes / 1024

        return when {
            gigaBytes >= 1 -> {
                decimalFormat.format(gigaBytes) + GB_SUFFIX
            }
            megaBytes >= 1 -> {
                decimalFormat.format(megaBytes) + MB_SUFFIX
            }
            kiloBytes >= 1 -> {
                decimalFormat.format(kiloBytes) + KB_SUFFIX
            }
            else -> {
                decimalFormat.format(bytes) + BYTES_SUFFIX
            }
        }
    }

    fun toStringConverter(obj: Any?) = obj.toString()

    fun upperCaseConverter(str: String?): String? = str?.uppercase()
}