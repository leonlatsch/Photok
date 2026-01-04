


package dev.leonlatsch.photok.settings.ui.credits

/**
 * Representing data for an entry in the contributors page.
 *
 * @since 1.2.0
 * @author Leon Latsch
 */
data class CreditEntry(
    val contribution: String,
    val name: String? = null,
    val contact: String? = null,
    val website: String? = null
) {
    val isHeader: Boolean
        get() = contribution == LAYOUT_HEADER

    val isFooter: Boolean
        get() = contribution == LAYOUT_FOOTER

    companion object {
        private const val LAYOUT_HEADER = "layout_header"
        private const val LAYOUT_FOOTER = "layout_footer"

        /**
         * Creates an entry which will be interpreted as a header.
         */
        fun createHeader() = CreditEntry(LAYOUT_HEADER)

        /**
         * Creates an entry which will be interpreted as a footer.
         */
        fun createFooter() = CreditEntry(LAYOUT_FOOTER)
    }
}


package dev.leonlatsch.photok.settings.ui.credits

/**
 * Representing data for an entry in the contributors page.
 *
 * @since 1.2.0
 * @author Leon Latsch
 */
data class CreditEntry(
    val contribution: String,
    val name: String? = null,
    val contact: String? = null,
    val website: String? = null
) {
    val isHeader: Boolean
        get() = contribution == LAYOUT_HEADER

    val isFooter: Boolean
        get() = contribution == LAYOUT_FOOTER

    companion object {
        private const val LAYOUT_HEADER = "layout_header"
        private const val LAYOUT_FOOTER = "layout_footer"

        /**
         * Creates an entry which will be interpreted as a header.
         */
        fun createHeader() = CreditEntry(LAYOUT_HEADER)

        /**
         * Creates an entry which will be interpreted as a footer.
         */
        fun createFooter() = CreditEntry(LAYOUT_FOOTER)
    }
}
