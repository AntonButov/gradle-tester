package gralde.tester

import gralde.tester.impl.ProjectBuildResult
import gralde.tester.impl.ProjectGenerationBuilder
import java.io.File

/**
 * Creates and configures a test project with the given configuration.
 *
 * @param fileForGeneration The root directory for the test project
 * @param block The configuration block for the project
 * @return ProjectBuildResult containing the build result and assertion capabilities
 */
fun testProject(
    fileForGeneration: File,
    block: ProjectGenerationBuilder.() -> Unit,
): ProjectBuildResult {
    val builder = ProjectGenerationBuilder.create(fileForGeneration)
    builder.block()
    return builder.generate()
}
