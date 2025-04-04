package gradle.tester

import gralde.tester.CodeComparator
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TextComparatorTest {
    @Test
    fun `test equalsIgnoringWhitespace with different whitespace`() {
        val actual =
            """
            public class Test {
                public void method() {
                    System.out.println("Hello");
                }
            }
            """.trimIndent()

        val expected = "publicclassTest{publicvoidmethod(){System.out.println(\"Hello\");}}"
        assertTrue(CodeComparator.equalsIgnoringWhitespace(actual, expected))

        val expectedWithSpaces =
            """
            public   class    Test {
                public void method() {
                    System.out.println("Hello");
                }
            }
            """.trimIndent()
        assertTrue(CodeComparator.equalsIgnoringWhitespace(actual, expectedWithSpaces))

        val expectedWithDifferentContent = "public class Test { /* Different content */ }"
        assertFalse(CodeComparator.equalsIgnoringWhitespace(actual, expectedWithDifferentContent))

        val expectedWithExtraContent = actual + "// Extra content"
        assertFalse(CodeComparator.equalsIgnoringWhitespace(actual, expectedWithExtraContent))

        // Case sensitivity check
        val expectedDifferentCase = "PUBLIC CLASS Test"
        assertFalse(CodeComparator.equalsIgnoringWhitespace(actual, expectedDifferentCase))
    }

    @Test
    fun `test equalsExactly with exact matching`() {
        val actual =
            """
            public class Test {
                public void method() {
                    System.out.println("Hello");
                }
            }
            """.trimIndent()

        assertTrue(CodeComparator.equalsExactly(actual, actual))

        val expectedWithDifferentSpaces =
            """
            public class Test {
              public void method() {
                System.out.println("Hello");
              }
            }
            """.trimIndent()
        assertFalse(CodeComparator.equalsExactly(actual, expectedWithDifferentSpaces))
    }

    @Test
    fun `test with empty strings`() {
        assertTrue(CodeComparator.equalsIgnoringWhitespace("", ""))
        assertTrue(CodeComparator.equalsExactly("", ""))

        assertTrue(CodeComparator.equalsIgnoringWhitespace("   \n\t", ""))
        assertFalse(CodeComparator.equalsExactly("   \n\t", ""))
    }
}
