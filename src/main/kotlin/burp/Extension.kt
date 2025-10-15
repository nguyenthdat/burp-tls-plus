package burp

import burp.api.montoya.BurpExtension
import burp.api.montoya.MontoyaApi

@Suppress("unused")
class Extension : BurpExtension {
    private lateinit var apiRef: MontoyaApi

    override fun initialize(api: MontoyaApi?) {
        if (api == null) {
            return
        }
        apiRef = api
        api.extension()?.setName(NAME)
        api.logging().logToOutput("Loaded $NAME v$VERSION")
        api.extension()?.registerUnloadingHandler {
            api.logging().logToOutput("Unloaded $NAME")
        }
    }

    companion object {
        const val NAME: String = "TLS+"
        const val VERSION: String = "0.1.0"
    }
}
