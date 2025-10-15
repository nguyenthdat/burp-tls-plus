package burp

import burp.api.montoya.BurpExtension
import burp.api.montoya.MontoyaApi
import burp.api.montoya.core.ToolType
import burp.api.montoya.http.handler.HttpHandler
import burp.api.montoya.http.handler.HttpRequestToBeSent
import burp.api.montoya.http.handler.HttpResponseReceived
import burp.api.montoya.http.handler.RequestToBeSentAction
import burp.api.montoya.http.handler.ResponseReceivedAction
import burp.api.montoya.ui.editor.extension.EditorCreationContext
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpResponseEditor
import burp.api.montoya.ui.editor.extension.HttpResponseEditorProvider

class BurpExtender : BurpExtension, HttpHandler, HttpResponseEditorProvider {
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
api.http().registerHttpHandler(this)
api.userInterface().registerHttpResponseEditorProvider(this)
}

override fun handleHttpRequestToBeSent(req: HttpRequestToBeSent?): RequestToBeSentAction? {
return null
}

override fun handleHttpResponseReceived(res: HttpResponseReceived?): ResponseReceivedAction? {
if (res == null) {
return null
}
if (!res.toolSource().isFromTool(ToolType.REPEATER)) {
return null
}
lastMessageFromListener = res.toByteArray().bytes
val svc = res.initiatingRequest().httpService()
lastPortFromListener = svc.port()
lastProtocolFromListener = if (svc.secure()) "https" else "http"
lastHostFromListener = svc.host()
return null
}

override fun provideHttpResponseEditor(creationContext: EditorCreationContext?): ExtensionProvidedHttpResponseEditor? {
if (creationContext == null) {
return null
}
return DiffMessageTab(apiRef)
}

companion object {
const val NAME: String = "Diff Last Response"
const val VERSION: String = "0.1.0"
var lastMessageFromListener: ByteArray? = null
var lastPortFromListener: Int = 0
var lastHostFromListener: String? = null
var lastProtocolFromListener: String? = null
}
}
