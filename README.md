# Gradle Tester

A Kotlin-based project for testing Gradle functionality using Gradle Test Kit.

## Overview

This project provides a framework for testing Gradle plugins and custom build logic. It uses Kotlin and is configured with the Gradle Test Kit to enable testing of Gradle-related functionality.

## How it works
```
    @Test
    fun `test adding local properties`() {
        testProject(projectRoot) {
            addKeyLocalProperties("API_KEY" to "test-api-key")
            addKeyLocalProperties("SDK_DIR" to "/path/to/sdk")
        }.apply {
            assertFile {
                path("local.properties")
                contentExactly("API_KEY=test-api-key\nSDK_DIR=/path/to/sdk")
            }
        }
    }
```
```
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
```
