package gradle.tester

import gralde.tester.testProject
import org.gradle.internal.impldep.org.junit.Rule
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ProjectGenerationBuilderTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var projectRoot: File

    @BeforeTest
    fun setup() {
        tempFolder.create()
        projectRoot = tempFolder.newFolder("test-project")
    }

    @AfterTest
    fun cleanup() {
        tempFolder.delete()
    }

    @Test
    fun `test basic project generation`() {
        val result =
            testProject(projectRoot) {
                buildScript(
                    """
                plugins {
                    kotlin("jvm") version "1.9.24"
                }
                
                repositories {
                    mavenCentral()
                }
                
                dependencies {
                    implementation(kotlin("stdlib"))
                }
                
            """,
                )
                settings("rootProject.name = \"test-project\"")
            }

        result.assertFile {
            path("build.gradle.kts")
        }

        result.assertFile {
            path("settings.gradle.kts")
        }
    }

    @Test
    fun `test adding local properties`() {
        val result =
            testProject(projectRoot) {
                addKeyLocalProperties("API_KEY" to "test-api-key")
                addKeyLocalProperties("SDK_DIR" to "/path/to/sdk")
            }

        result.assertFile {
            path("local.properties")
            content("API_KEY=test-api-key\nSDK_DIR=/path/to/sdk")
        }
    }

    @Test
    fun `test adding kotlin source`() {
        val result =
            testProject(projectRoot) {
                additionalSource(
                    "src/main/kotlin/com/example/Test.kt",
                    """
                    package com.example
                    
                    class Test {
                        fun test() = "Test"
                    }
                    """.trimIndent(),
                )
            }

        result.assertFile {
            path("src/main/kotlin/com/example/Test.kt")
            content(
                """
                package com.example
                
                class Test {
                    fun test() = "Test"
                }
                """.trimIndent(),
            )
        }
    }

    @Test
    fun `test adding kotlin source from file`() {
        val sourceFile = File("src/test/kotlin/gradle/tester/SourceFile.kt")
        val result =
            testProject(projectRoot) {
                additionalSource("src/main/kotlin/com/example/ImportedSource.kt", sourceFile)
            }

        result.assertFile {
            path("src/main/kotlin/com/example/ImportedSource.kt")
            content(
                """
                package gradle.tester

                    class SourceFile {
                      fun sourceMethod() = "From source file"
                    }
                """.trimIndent(),
            )
        }

        result.assertFile {
            path("src/main/kotlin/com/example/ImportedSource.kt")
            content(
                """
                class SourceFile {
                    fun sourceMethod() = "From source file"
                }
                """.trimIndent(),
            )
        }
    }

    @Test
    fun `test multiple sources and local properties`() {
        val result =
            testProject(projectRoot) {
                additionalSource("src/main/kotlin/com/example/Test1.kt", "package com.example\n\nclass Test1")
                additionalSource("src/main/kotlin/com/example/Test2.kt", "package com.example\n\nclass Test2")
                addKeyLocalProperties("key1" to "value1")
                addKeyLocalProperties("key2" to "value2")
            }

        result.assertFile {
            path("src/main/kotlin/com/example/Test1.kt")
        }

        result.assertFile {
            path("src/main/kotlin/com/example/Test2.kt")
        }

        result.assertFile {
            path("local.properties")
            content("key1=value1\nkey2=value2")
        }
    }

    @Test
    fun `test assert options`() {
        val result =
            testProject(projectRoot) {
                buildScript(
                    """
                plugins {
                    kotlin("jvm") version "1.9.24"
                }
            """,
                )
            }

        result.assertFile {
            path("build.gradle.kts")
            contentExactly(
                """
                plugins {
                    kotlin("jvm") version "1.9.24"
                }
            """,
            )
        }
    }

    @Test
    fun `test assertFile DSL method with exact content matching`() {
        val expectedContent =
            """
            package com.example
            
            class TestContent {
                fun testMethod() = "Test Content"
            }
            """.trimIndent()

        val result =
            testProject(projectRoot) {
                additionalSource(
                    "src/main/kotlin/com/example/TestContent.kt",
                    expectedContent,
                )
            }

        // Test with whitespace-insensitive matching
        result.assertFile {
            path("src/main/kotlin/com/example/TestContent.kt")
            content(
                """
                package com.example
                
                class    TestContent   {
                    fun    testMethod()    =    "Test Content"
                }
                """.trimIndent(),
            )
        }

        // Test with exact matching
        result.assertFile {
            path("src/main/kotlin/com/example/TestContent.kt")
            contentExactly(expectedContent)
        }
    }
}
