package a

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import freemarker.cache.ClassTemplateLoader
import freemarker.cache.MultiTemplateLoader
import freemarker.template.Configuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.io.File
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class G : Plugin<Project> {
    open class GTask @Inject constructor(@Input val input: List<File>) : DefaultTask() {
        private val od = project.layout.buildDirectory.dir("aao").get()

        init {
            outputs.dir(od)
            input.forEach { inputs.dir(it) }
        }

        @TaskAction
        fun doGenerate() {
            val cfg = Configuration(Configuration.VERSION_2_3_32)
            cfg.templateLoader = MultiTemplateLoader(arrayOf(ClassTemplateLoader(this::class.java, "/")))
            runBlocking {
                for (file in input.asSequence().flatMap { it.walk() }.filter { it.isFile }) {
                    val isEnum = file.name.endsWith(".enum.yml")
                    val isFlag = file.name.endsWith(".flag.yml")
                    if (!isEnum && !isFlag) continue
                    launch {
                        val bytes = withContext(Dispatchers.IO) { file.readBytes() }
                        val data = Yaml.default.decodeFromStream<FlagSet>(bytes.inputStream())
                        val pkgPath = data.`package`.split('.').joinToString("/")
                        val typeOutFile = od.asFile.resolve(pkgPath + "/" + data.typename + ".kt")
                        typeOutFile.parentFile.mkdirs()
                        typeOutFile.outputStream().bufferedWriter(StandardCharsets.UTF_8).use { w ->
                            cfg.getTemplate(if (isFlag) "flag.ftl" else "enum.ftl").process(data, w)
                        }
                        val serOutFile = od.asFile.resolve(pkgPath + "/serializer/" + data.typename + "Serializer.kt")
                        serOutFile.parentFile.mkdirs()
                        serOutFile.outputStream().bufferedWriter(StandardCharsets.UTF_8).use { w ->
                            cfg.getTemplate(if (isFlag) "flagser.ftl" else "enumser.ftl").process(data, w)
                        }
                    }
                }
            }
        }
    }

    override fun apply(project: Project) {
        project.pluginManager.apply("org.jetbrains.gradle.plugin.idea-ext")
        val commonMainSourceSet = project.kotlinExtension.sourceSets.getByName("commonMain")
        val od = project.layout.buildDirectory.dir("aao").get()
        commonMainSourceSet.kotlin.srcDirs(od)
        val inputDirs =
            commonMainSourceSet.kotlin.srcDirs.map { it.resolve("../define") }.filter { it.isDirectory && it.exists() }
        val ideaModel = project.extensions.getByType(org.gradle.plugins.ide.idea.model.IdeaModel::class.java)
        val projectSettings =
            (ideaModel.project as ExtensionAware).extensions.getByType(org.jetbrains.gradle.ext.ProjectSettings::class.java)
        val taskTriggerConfig =
            (projectSettings as ExtensionAware).extensions.getByType(org.jetbrains.gradle.ext.TaskTriggersConfig::class.java)
        val gTask = project.tasks.register("tGenerateFlagFiles", GTask::class.java, inputDirs)
        ideaModel.module.generatedSourceDirs.add(od.asFile)
        taskTriggerConfig.afterSync(gTask)
        project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
            it.dependsOn(gTask)
        }
    }
}

@Serializable
internal data class FlagSet(
    val `package`: String,
    val typename: String,
    val dataLength: Int,
    val list: List<Item>,
) {
    companion object {
        val ref = listOf(
            listOf("Byte", "UByte"),
            listOf("Short", "UShort"),
            listOf("Int", "UInt"),
            listOf("Long", "ULong"),
        )
    }

    @Suppress("unused")
    val rawType = ref[when (dataLength) {
        1 -> 0
        2 -> 1
        4 -> 2
        8 -> 3
        else -> error("unsupported data length: $dataLength")
    }][0]

    @Serializable
    data class Item(
        val name: String,
        val value: String,
        val docs: String = "",
    )
}
