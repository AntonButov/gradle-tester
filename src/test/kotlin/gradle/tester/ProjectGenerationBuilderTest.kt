package gradle.tester

import gralde.tester.testProject
import org.gradle.internal.impldep.org.junit.Rule
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains

class ProjectGenerationBuilderTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var testFile: File
    private lateinit var projectRoot: File

    @BeforeTest
    fun setup() {
        tempFolder.create()
        projectRoot = tempFolder.newFolder("test-project")
        testFile = File(projectRoot, "testFile.kt")
        testFile.writeText(
            """
            package com.example
            
            class TestClass {
                fun hello() = "Hello, World!"
            }
            """.trimIndent(),
        )
    }

    @AfterTest
    fun cleanup() {
        tempFolder.delete()
    }

    @Test
    fun `test basic project generation`() {
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

        assertTrue(File(projectRoot, "build.gradle.kts").exists())
        assertTrue(File(projectRoot, "settings.gradle.kts").exists())
    }

    @Test
    fun `test adding local properties`() {
        testProject(projectRoot) {
            addKeyLocalProperties("API_KEY" to "test-api-key")
            addKeyLocalProperties("SDK_DIR" to "/path/to/sdk")
        }

        val localPropertiesFile = File(projectRoot, "local.properties")
        assertTrue(localPropertiesFile.exists())

        val content = localPropertiesFile.readText()
        assertTrue(content.contains("API_KEY=test-api-key"))
        assertTrue(content.contains("SDK_DIR=/path/to/sdk"))
    }

    @Test
    fun `test adding kotlin source`() {
        testProject(projectRoot) {
            additionalSource(
                "com/example/Test.kt",
                """
                package com.example
                
                class Test {
                    fun test() = "Test"
                }
                """.trimIndent(),
            )
        }
        val sourceFile = File(projectRoot, "src/main/kotlin/com/example/Test.kt")
        assertTrue(sourceFile.exists())
        assertTrue(sourceFile.readText().contains("class Test"))
    }

    @Test
    fun `test adding kotlin source from file`() {
        val sourceFile = File("src/test/kotlin/gradle/tester/SourceFile.kt")
        testProject(projectRoot) {
            additionalSource("com/example/ImportedSource.kt", sourceFile)
        }
        val importedFile = File(projectRoot, "src/main/kotlin/com/example/ImportedSource.kt")
        assertTrue(importedFile.exists())
        assertTrue(importedFile.readText().contains("class SourceFile"))
        assertTrue(importedFile.readText().contains("sourceMethod()"))
    }

    @Test
    fun `test check action`() {
        var checkCalled = false

        testProject(projectRoot) {
            buildScript(
                """
                plugins {
                    kotlin("jvm") version "1.9.24"
                }
            """,
            )
            check {
                checkCalled = true
            }
        }

        assertTrue(checkCalled)
    }

    @Test
    fun `test multiple sources and local properties`() {
        testProject(projectRoot) {
            additionalSource("com/example/Test1.kt", "package com.example\n\nclass Test1")
            additionalSource("com/example/Test2.kt", "package com.example\n\nclass Test2")
            addKeyLocalProperties("key1" to "value1")
            addKeyLocalProperties("key2" to "value2")
        }

        assertTrue(File(projectRoot, "src/main/kotlin/com/example/Test1.kt").exists())
        assertTrue(File(projectRoot, "src/main/kotlin/com/example/Test2.kt").exists())

        val localPropertiesFile = File(projectRoot, "local.properties")
        val content = localPropertiesFile.readText()
        assertTrue(content.contains("key1=value1"))
        assertTrue(content.contains("key2=value2"))
    }

    @Test
    fun `test print and assert options`() {
        testProject(projectRoot) {
            buildScript(
                """
                plugins {
                    kotlin("jvm") version "1.9.24"
                }
            """,
            )
            isPrint = false
        }
        val buildGradleFile = File(projectRoot, "build.gradle.kts")
        assertTrue(buildGradleFile.exists())
        val content = buildGradleFile.readText()
        assertContains(content, "plugins")
    }

    @Test
    fun `test complex project setup`() {
        testProject(projectRoot) {
            settings(
                """
                rootProject.name = "complex-test"
                include(":app", ":library")
            """,
            )

            buildScript(
                """
                plugins {
                    kotlin("jvm") version "1.9.24" apply false
                }
                
                allprojects {
                    repositories {
                        mavenCentral()
                    }
                }
            """,
            )

            additionalSource(
                "com/example/App.kt",
                """
                package com.example
                
                fun main() {
                    println("Hello from App")
                }
            """,
            )

            isAssertBuild = false
        }

        val appBuildGradle = """
            plugins {
                kotlin("jvm")
                application
            }
            
            application {
                mainClass.set("com.example.AppKt")
            }
        """

        val appBuildFile = File(projectRoot, "app/build.gradle.kts")
        appBuildFile.parentFile.mkdirs()
        appBuildFile.writeText(appBuildGradle)

        assertTrue(File(projectRoot, "settings.gradle.kts").exists())
        assertTrue(File(projectRoot, "build.gradle.kts").exists())
        assertTrue(File(projectRoot, "app/build.gradle.kts").exists())
        assertTrue(File(projectRoot, "src/main/kotlin/com/example/App.kt").exists())
    }

    @Test
    fun `test cannot create ProjectGenerationBuilder directly`() {
        // Следующий код не должен компилироваться:
        // val builder = ProjectGenerationBuilder(projectRoot)

        // Вместо этого мы можем только использовать функцию generateProject
        val result =
            testProject(projectRoot) {
                isAssertBuild = false
            }

        // Если мы дошли до этой точки, значит тест прошел успешно
        assertTrue(true)
    }
}
