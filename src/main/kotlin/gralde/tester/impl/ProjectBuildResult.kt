package gralde.tester.impl

import gralde.tester.CodeComparator
import org.gradle.testkit.runner.BuildResult
import java.io.File

/**
 * Class representing the result of a project build with additional assertion capabilities.
 */
class ProjectBuildResult(
    private val projectRoot: File,
    val buildResult: BuildResult,
) {
    /**
     * Asserts that a file exists at the specified path and optionally checks its content.
     * Uses a DSL-style builder for more expressive assertions.
     *
     * @param block The configuration block for file assertion
     */
    fun assertFile(block: FileAssertionBuilder.() -> Unit) {
        val builder = FileAssertionBuilder().apply(block)

        // Validate required fields
        val path = builder.path ?: throw IllegalArgumentException("Path must be specified in assertFile")

        val file = projectRoot.resolve(path)
        assert(file.exists()) { "File $path does not exist" }

        // Check content if specified
        builder.expectedContent?.let { expectedContent ->
            val fileContent = file.readText()

            val contentMatches =
                if (builder.ignoreWhitespace) {
                    CodeComparator.Companion.equalsIgnoringWhitespace(fileContent, expectedContent)
                } else {
                    CodeComparator.Companion.equalsExactly(fileContent, expectedContent)
                }

            assert(contentMatches) {
                val comparisonType = if (builder.ignoreWhitespace) "(ignoring whitespace)" else "(exact match)"
                "File $path content does not exactly match expected content $comparisonType"
            }
        }
    }

    /**
     * Builder class for file assertions using DSL style.
     */
    class FileAssertionBuilder {
        var path: String? = null
        var expectedContent: String? = null

        /**
         * Sets the path to the file to be checked.
         */
        fun path(path: String) {
            this.path = path
        }

        /**
         * Sets the expected content of the file.
         */
        fun content(content: String) {
            this.expectedContent = content
        }

        fun contentExactly(content: String) {
            this.expectedContent = content
            this.ignoreWhitespace = false
        }

        var ignoreWhitespace: Boolean = true
    }

    /**
     * Legacy method for backward compatibility.
     */
    fun assertFileExists(
        path: String,
        expectedContent: String? = null,
        ignoreWhitespace: Boolean = true,
    ) {
        assertFile {
            this.path = path
            this.expectedContent = expectedContent
            this.ignoreWhitespace = ignoreWhitespace
        }
    }
}
