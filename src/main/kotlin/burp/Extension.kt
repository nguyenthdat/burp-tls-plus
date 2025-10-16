package burp

import burp.api.montoya.BurpExtension
import burp.api.montoya.MontoyaApi
import burp.api.montoya.proxy.http.InterceptedRequest
import burp.api.montoya.proxy.http.ProxyRequestToBeSentAction
import com.google.gson.Gson
import uniffi.tlsplus.stopServer

@Suppress("unused")
class Extension : BurpExtension {
    private lateinit var api: MontoyaApi
    private lateinit var settings: Settings
    private var gson: Gson = Gson()

    override fun initialize(api: MontoyaApi?) {
        if (api == null) {
            return
        }
        this.api = api
        this.settings = Settings(api)
        api.extension()?.setName(NAME)
        api.logging().logToOutput("Loaded $NAME v$VERSION")
        api.extension().registerUnloadingHandler {
            val err =
                stopServer()

            err?.isEmpty()?.let {
                if (!it) {
                    api.logging().logToError(err)
                }
            }
        }

        api.userInterface().registerSettingsPanel (SettingsPanel(settings))

//        Thread(Runnable {
//            val err: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
//                ServerLibrary.INSTANCE.StartServer(settings.spoofProxyAddress)
//            if (!err.isEmpty()) {
//                api.logging().logToError(err)
//                val isGraceful = err.contains("Server stopped") || err.contains("address already in use")
//                if (!isGraceful) {
//                    api.extension().unload() // fatal error; disable the extension
//                }
//            }
//        }).start()
    }

    private fun processHttpRequest(request: InterceptedRequest): ProxyRequestToBeSentAction? {
        try {
//            val requestURI = URI(request.url())
//
//            if (requestURI.host.equals("tlsplus-error")) {
//                throw Error(String(request.body().bytes, StandardCharsets.UTF_8))
//            }
//
//            val headerOrder = arrayOfNulls<String>(request.headers().size)
//            for (i in request.headers().indices) {
//                headerOrder[i] = request.headers()[i].name()
//            }
//
//            val transportConfig = settings.toTransportConfig()
//            transportConfig.Host = requestURI.host
//            transportConfig.Scheme = requestURI.scheme
//            transportConfig.HeaderOrder = headerOrder
//
//            val goConfigJSON = gson.toJson(transportConfig)
//            val uri = URI("https://" + settings.spoofProxyAddress)
//            val httpService = HttpService.httpService(uri.host, uri.port, uri.scheme == "https")
//            val nextRequest = request.withService(httpService).withAddedHeader(HEADER_KEY, goConfigJSON)
//
//            return ProxyRequestToBeSentAction.continueWith(nextRequest)
            return null
        } catch (e: Exception) {
            api.logging().logToError("Http request error: $e")
            return null
        }
    }

    companion object {
        const val NAME: String = "TLS+"
        const val VERSION: String = "0.1.0"
        const val HEADER_KEY: String = "tlsplusconfig"
    }
}