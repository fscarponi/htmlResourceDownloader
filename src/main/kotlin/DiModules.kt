import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.kodein.di.*

import java.io.File


object DiModules {

    val parameters
    get() = DI.Module("globalDI"){

        bind<HttpClient>() with singleton {
            HttpClient(CIO)
        }

        bind<Boolean>("parameterOnFile") with provider {
            val f= File("parameters.json")
            f.exists() && f.isFile
        }

        bind<Parameters>() with singleton {
            if (instance("parameterOnFile")){
                val parametersText=File("parameters.json").readText()
                Json.decodeFromString(parametersText)
            }else{
                Json.decodeFromString(File("local.properties").readText())
            }
        }

        bind<Map<String,String>>("fileSkippedMap") with singleton {
            mutableMapOf()
        }


    }
}
