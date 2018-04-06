package sparklin.kshell.repl

import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.cli.jvm.repl.GenericReplCompiler
import java.io.File
import kotlin.reflect.KClass

fun findClassJarsOrEmpty(klass: KClass<out Any>, filterJarByRegex: Regex = ".*".toRegex()): List<File> {
    return listOf(klass.containingClasspath(filterJarByRegex)).filterNotNull()
}

fun <T : Any> List<T>.assertNotEmpty(error: String): List<T> {
    if (this.isEmpty()) throw IllegalStateException(error)
    return this
}

internal fun <T : Any> KClass<T>.containingClasspath(filterJarName: Regex = ".*".toRegex()): File? {
    val clp = "${qualifiedName?.replace('.', '/')}.class"
    val baseList = Thread.currentThread().contextClassLoader.getResources(clp)
            ?.toList()
            ?.map { it.toString() }
    return baseList
            ?.map { url ->
                zipOrJarUrlToBaseFile(url) ?: qualifiedName?.let { classFilenameToBaseDir(url, clp) }
                ?: throw IllegalStateException("Expecting a local classpath when searching for class: ${qualifiedName}")
            }
            ?.find {
                filterJarName.matches(it)
            }
            ?.let { File(it) }
}

private val zipOrJarRegex = """(?:zip:|jar:file:)(.*)!\/(?:.*)""".toRegex()
private val filePathRegex = """(?:file:)(.*)""".toRegex()

// TODO: are some of these already in PathUtils?
fun findKotlinCompilerJarsOrEmpty(useEmbeddedCompiler: Boolean = true): List<File> {
    val filter = if (useEmbeddedCompiler) """.*\/kotlin-compiler-embeddable.*\.jar""".toRegex()
    else """.*\/kotlin-compiler-(?!embeddable).*\.jar""".toRegex()
    return listOf(K2JVMCompiler::class.containingClasspath(filter)).filterNotNull()
}

internal fun classFilenameToBaseDir(url: String, resource: String): String? {
    return filePathRegex.find(url)?.let { it.groupValues[1].removeSuffix(resource) }
}

internal fun zipOrJarUrlToBaseFile(url: String): String? {
    return zipOrJarRegex.find(url)?.let { it.groupValues[1] }
}

fun findKotlinCompilerJars(useEmbeddedCompiler: Boolean = true): List<File> {
    return findKotlinCompilerJarsOrEmpty(useEmbeddedCompiler).assertNotEmpty("Cannot find kotlin compiler classpath, which is required")
}

fun findKotlinStdLibJarsOrEmpty(): List<File> {
    return listOf(Pair::class.containingClasspath(""".*\/kotlin-stdlib.*\.jar""".toRegex())).filterNotNull()
}

fun findKotlinStdLibJars(): List<File> {
    return findKotlinStdLibJarsOrEmpty().assertNotEmpty("Cannot find kotlin stdlib classpath, which is required")
}


fun findRequiredJarFiles(includeScriptEngine: Boolean = false,
                                  includeKotlinCompiler: Boolean = false,
                                  useEmbeddableCompiler: Boolean = true,
                                  includeStdLib: Boolean = true,
                                  additionalClasses: List<KClass<out Any>> = emptyList()): List<File> {
    val additionalClassJars = additionalClasses.map { findClassJarsOrEmpty(it).assertNotEmpty("Missing JAR for additional class $it") }.flatten()
    val scriptEngineJars = if (includeScriptEngine) findClassJarsOrEmpty(GenericReplCompiler::class).assertNotEmpty("Cannot find repl engine classpath, which is required")
    else emptyList()
    val kotlinJars = (if (includeKotlinCompiler) findKotlinCompilerJars(useEmbeddableCompiler) else emptyList()) +
            (if (includeStdLib) findKotlinStdLibJars() else emptyList())
    return (additionalClassJars + scriptEngineJars + kotlinJars).toSet().toList()
}

fun getJavaVersion(): Int {
    val default = 0x10006
    val version = System.getProperty("java.specification.version") ?: return default
    val components = version.split('.')
    return try {
        when (components.size) {
            0 -> default
            1 -> components[0].toInt() * 0x10000
            else -> components[0].toInt() * 0x10000 + components[1].toInt()
        }
    } catch (e: NumberFormatException) {
        default
    }
}

fun makeFileBaseName(codeLine: CodeLine) =
        "Line_${codeLine.no}" + if (codeLine.part != 0) "_${codeLine.part}" else ""

fun List<Snippet>.containsWithName(name: String): Boolean =
        this.any { it is NamedSnippet && it.name == name }

fun List<Snippet>.shadow(snippets: List<Snippet>) {
    filterIsInstance<DeclarationSnippet>().forEach {
        val historyItem = it
        if (snippets.filterIsInstance<DeclarationSnippet>().any { historyItem.signature() == it.signature()  }) historyItem.shadowed = true
    }
}

inline fun <reified T : Snippet> List<Snippet>.filter(): List<T> =
        filter { it is T }.map { it as T }