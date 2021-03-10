import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

@Serializable
data class Parameters(
    val startUri: String,
    val outputDir: String,
    val filteredFileTypes: List<String>,
    val filteredDirs: List<String>
)

fun Parameters.toJson()= Json.encodeToString(Parameters.serializer(),this)
