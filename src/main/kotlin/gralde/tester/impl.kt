package gralde.tester

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File

fun generateProject(
    fileForGeneration: File,
    block: ProjectGenerationBuilder.() -> Unit = {}
): BuildResult {
    val builder = ProjectGenerationBuilder(fileForGeneration)
    builder.block()
    return builder.generate()
}

class ProjectGenerationBuilder(private val root: File) {

    private var keysLocalProperties: List<Pair<String,String>> = mutableListOf()
    private var buildScript: String? = null
    private var checkAction: ProjectGenerationBuilder.() -> Unit = {}
    private var arg: String = "build"
    private var additionalSources: MutableList<Pair<String, String>> = mutableListOf()
    private var additionalFiles: MutableList<Pair<String, String>> = mutableListOf()
    private var settings: String? = null

    var isAssertBuild = true

    var isPrint = true

    fun addKeyLocalProperties(key: Pair<String, String>) {
        keysLocalProperties += key
    }

    fun buildScript(script: String) {
        this.buildScript = script
    }

    fun additionalSource(path: String, content: String) {
        additionalSources.add(path to content)
    }

    fun additionalSource(path: String, file: File) {
        val content = file.readText()
        additionalSources.add(path to content)
    }

    fun settings(content: String) {
        this.settings = content
    }

    fun gradleTask(arg: String) {
        this.arg = arg
    }

    fun check(action: ProjectGenerationBuilder.() -> Unit) {
        this.checkAction = action
    }

    private fun withKotlinSource(
        path: String,
        content: String,
    ) {
        val file = root.resolve("src/main/kotlin/$path")
        file.parentFile.mkdirs()
        file.writeText(content)
    }

    private fun write(
        path: String,
        content: String,
    ) {
        val file = root.resolve(path)
        file.parentFile.mkdirs()
        file.writeText(content)
    }

    fun generate(): BuildResult {
        val runner  = GradleRunner.create()
            with (runner) {
                withProjectDir(root)
                withArguments(arg)
                val localPropertiesContent = keysLocalProperties.joinToString("\n") { (key, value) -> "$key=$value" }
                if (localPropertiesContent.isNotEmpty()) {
                    write("local.properties", localPropertiesContent)
                }
                buildScript?.let { write("build.gradle.kts", it) }
                settings?.let { write("settings.gradle.kts", it) }
                additionalSources.forEach {
                    withKotlinSource(it.first, it.second)
                }
            }

        val result = runner.build()
        if (isAssertBuild) {
            assert(result.output.contains("BUILD SUCCESSFUL"))
        }
        if (isPrint) {
            println(result.output)
        }
        checkAction(this)

        return result
    }

}