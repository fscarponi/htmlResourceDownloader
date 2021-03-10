import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant


suspend fun main() = coroutineScope {
    val startUri = "websiteurl"
    val outputDir = "out"
    val filteredFile = listOf(".pdf", ".xls")
    val filteredDir = listOf(
        "node_module",
        "build"
    )
    val par = Parameters(startUri, outputDir, filteredFile, filteredDir)
    runCatching {
        withContext(Dispatchers.IO) {
            val file = File("parameters.json")
            if (!file.exists()) {
                file.createNewFile()
                file.writeText(par.toJson())
            }
        }
    }
    val startTime = Instant.now()
    val job = async {
        ResourceDownloader().downloadResources()
    }
    job.await()
    val eta = Instant.now().epochSecond - startTime.epochSecond

    println("Total operation time:${eta.asETAString()}")
    println("if some file has been skipped for error is reported in skipped.csv")

}

private fun Long.asETAString(): String {
    val sb = StringBuilder()
    val hours = this / 3600
    val minutes = (this % 3600) / 60;
    val seconds = this % 60;
    if (hours > 0) sb.append("$hours hours ")
    if (minutes > 0) sb.append("$minutes minutes ")
    if (seconds > 0) sb.append("$seconds seconds")
    return sb.toString()
}
