import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import it.skrape.core.fetcher.HttpFetcher
import it.skrape.core.htmlDocument
import it.skrape.exceptions.ElementNotFoundException
import it.skrape.extract
import it.skrape.selects.eachHref
import it.skrape.selects.html5.a
import it.skrape.skrape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.io.File

class ResourceDownloader : DIAware {

    override val di: DI by DI.lazy {
        import(DiModules.parameters)
    }

    private val settings: Parameters by instance()
    val fileSkipped: MutableMap<String, String> by instance("fileSkippedMap")

    suspend fun downloadResources() = coroutineScope {

        val startUri = settings.startUri
        val filteredDir = settings.filteredDirs
        extractFilesUri(startUri, filteredDir)
        launch(Dispatchers.IO){
                val file = File("skippedFiles.csv")
                if (file.exists()) file.delete()
                file.createNewFile()
                val sb=StringBuilder()
                fileSkipped.forEach {
                    sb.append(it.key+";"+it.value+"\n")
                }
                file.writeText(sb.toString())
        }

    }

    private suspend fun extractFilesUri(initialUri: String, filterDirNames: List<String>): Unit = coroutineScope {
        val links = getLinks(initialUri)
        links.forEach {
            launch {
                if (it.matches(".*\\.[A-z]{2,15}$".toRegex())) {
                    println("trovato file: $it")
                    downloadFile(it)
                } else {
                    println("trovato link: $it")
                    if (!shouldBeFiltered(it, filterDirNames) && !isAnHTMLFile(it)) {
                        try {
                            extractFilesUri(it, filterDirNames)
                        } catch (e: ElementNotFoundException) {
                            println("cannot go deep into $it problably is a link to parent")
                        }
                    }
                }
            }
        }
    }


    private suspend fun downloadFile(uri: String) = coroutineScope {
        val outputDir = settings.outputDir
        val fileSkipped: MutableMap<String, String> by instance("fileSkippedMap")
        //remove from full pat the start site uri
        val relativeUri = uri.replace(settings.startUri, "")
        //split with / and re
        val pathSegments = relativeUri.split("/").filter { it != "" }
        //create output path

        val path = outputDir + File.separator + pathSegments.dropLast(1).joinToString(File.separator)
        //create file and path

        File(path).mkdirs()
        //use or create correct dir
        // di is here signed with '//' delimiter
        launch(Dispatchers.IO) {
            val ktorHttpClient: HttpClient by instance()
            println("creo file ${outputDir + File.separator + pathSegments.joinToString(File.separator)}")
            val file = File(outputDir + File.separator + pathSegments.joinToString(File.separator))
            try {
                file.createNewFile()
            } catch (e: Exception) {
                println("ERRORE SU FILE $file")
                fileSkipped[file.toString()] = "ERROR: + ${e.localizedMessage}"
            }

            println("scrivo in $file, esiste->" + file.exists())
            if (pathSegments.last().endsWith(".html")) {
                try {
                    ktorHttpClient.get<ByteReadChannel>(uri)
                } catch (e: ClientRequestException) {
                    file.writeText(e.message!!)
                }
            } else {
                val channel = ktorHttpClient.get<ByteReadChannel>(uri)
                file.outputStream().use {
                    channel.copyTo(it)
                }
            }
        }

    }

    private fun getLinks(uri: String): List<String> {
        return skrape(HttpFetcher) {
            request {
                url = uri
            }
            extract {
                htmlDocument {
                    a {
                        findAll {
                            eachHref.map { uri + it.prependIfMissing("/") }
                        }
                    }
                }
            }
        }
    }

    private fun isAnHTMLFile(it: String) =
        it.contains("#")

    private fun shouldBeFiltered(uri: String, filterDirNames: List<String>): Boolean {
        filterDirNames.forEach { if (uri.contains(it)) return true }
        return false
    }


}
