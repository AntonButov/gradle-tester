package gralde.tester

import org.gradle.testkit.runner.BuildResult
import java.io.File

fun testProject(
    fileForGeneration: File,
    block: ProjectGenerationBuilder.() -> Unit,
): BuildResult {
    val builder = ProjectGenerationBuilder.create(fileForGeneration)
    builder.block()
    return builder.generate()
}
