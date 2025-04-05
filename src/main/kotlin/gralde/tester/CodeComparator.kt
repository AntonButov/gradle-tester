package gralde.tester

/**
 * Utility class for comparing text content with whitespace normalization.
 */
class CodeComparator {
    companion object {
        /**
         * Checks if two strings are equal ignoring whitespace differences and package declarations.
         *
         * @param actual The actual text to check
         * @param expected The expected text
         * @return true if the normalized texts are equal
         */
        fun equalsIgnoringWhitespace(
            actual: String,
            expected: String,
        ): Boolean {
            // First, remove package declarations
            val actualWithoutPackage = actual.replace(Regex("package\\s+[\\w.]+\\s*"), "")
            val expectedWithoutPackage = expected.replace(Regex("package\\s+[\\w.]+\\s*"), "")

            // Remove all whitespace
            val normalizedActual = actualWithoutPackage.replace(Regex("\\s+"), "")
            val normalizedExpected = expectedWithoutPackage.replace(Regex("\\s+"), "")

            // Remove any extra braces at the beginning or end that might be from different indentation styles
            val trimmedActual = normalizedActual.trim('{', '}', ' ', '\t', '\n', '\r')
            val trimmedExpected = normalizedExpected.trim('{', '}', ' ', '\t', '\n', '\r')

            return trimmedActual == trimmedExpected
        }

        /**
         * Checks if two strings are exactly equal.
         *
         * @param actual The actual text to check
         * @param expected The expected text
         * @return true if the texts are exactly equal
         */
        fun equalsExactly(
            actual: String,
            expected: String,
        ): Boolean {
            return actual == expected
        }
    }
}
