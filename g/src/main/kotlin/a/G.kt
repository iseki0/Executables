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
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.LinkOption
import javax.inject.Inject
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteExisting
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.walk

class G : Plugin<Project> {
    open class GTask @Inject constructor(@Input val input: List<File>) : DefaultTask() {
        private val od = project.layout.buildDirectory.dir("aao").get()

        init {
            outputs.dir(od)
            input.forEach { inputs.dir(it) }
        }

        @OptIn(ExperimentalPathApi::class)
        @TaskAction
        fun doGenerate() {
            od.asFile.toPath()
                .walk()
                .filter { it.isRegularFile(LinkOption.NOFOLLOW_LINKS) }
                .filter { it.extension.equals("kt", true) }
                .forEach { it.deleteExisting() }
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
                    }
                }
            }
        }
    }

    override fun apply(project: Project) {
        val commonMainSourceSet = project.kotlinExtension.sourceSets.getByName("commonMain")
        val od = project.layout.buildDirectory.dir("aao").get()
        commonMainSourceSet.kotlin.srcDirs(od)
        val inputDirs =
            commonMainSourceSet.kotlin.srcDirs.map { it.resolve("../define") }.filter { it.isDirectory && it.exists() }
        val gTask = project.tasks.register("tGenerateFlagFiles", GTask::class.java, inputDirs)
        project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
            it.dependsOn(gTask)
        }
        project.tasks.configureEach {
            if (it.name.endsWith("SourcesJar") || it.name == "sourcesJar") {
                it.dependsOn(gTask)
            }
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
