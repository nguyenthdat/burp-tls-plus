package burp

import burp.api.montoya.MontoyaApi
import burp.api.montoya.persistence.Preferences
import uniffi.tlsplus.getFingerprints

class Settings(private val prefs: Preferences) {

    constructor(api: MontoyaApi) : this(api.persistence().preferences())

    var spoofProxyAddress: String
        get() = prefs.getString(KEY_SPOOF_PROXY_ADDRESS).orEmpty().ifEmpty { DEFAULT_SPOOF_PROXY_ADDRESS }
        set(value) { prefs.setString(KEY_SPOOF_PROXY_ADDRESS, value.trim()) }

    var interceptProxyAddress: String
        get() = prefs.getString(KEY_INTERCEPT_PROXY_ADDRESS).orEmpty().ifEmpty { DEFAULT_INTERCEPT_PROXY_ADDRESS }
        set(value) { prefs.setString(KEY_INTERCEPT_PROXY_ADDRESS, value.trim()) }

    var burpProxyAddress: String
        get() = prefs.getString(KEY_BURP_PROXY_ADDRESS).orEmpty().ifEmpty { DEFAULT_BURP_PROXY_ADDRESS }
        set(value) { prefs.setString(KEY_BURP_PROXY_ADDRESS, value.trim()) }

    var fingerprint: String
        get() = prefs.getString(KEY_FINGERPRINT).orEmpty().ifEmpty { DEFAULT_TLS_FINGERPRINT }
        set(value) { prefs.setString(KEY_FINGERPRINT, value) }

    var hexClientHello: String?
        get() = prefs.getString(KEY_HEX_CLIENT_HELLO)?.takeIf { it.isNotEmpty() }
        set(value) { prefs.setString(KEY_HEX_CLIENT_HELLO, value ?: "") }

    var useInterceptedFingerprint: Boolean
        get() = prefs.getBoolean(KEY_USE_INTERCEPTED_FINGERPRINT) ?: false
        set(value) { prefs.setBoolean(KEY_USE_INTERCEPTED_FINGERPRINT, value) }

    var httpTimeout: Int
        get() = (prefs.getInteger(KEY_HTTP_TIMEOUT) ?: DEFAULT_HTTP_TIMEOUT)
        set(value) { prefs.setInteger(KEY_HTTP_TIMEOUT, value.coerceIn(1, 300)) }

    val fingerprints: List<String> get() = getFingerprints()

    companion object {
        private const val KEY_SPOOF_PROXY_ADDRESS = "SpoofProxyAddress"
        private const val KEY_INTERCEPT_PROXY_ADDRESS = "InterceptProxyAddress"
        private const val KEY_BURP_PROXY_ADDRESS = "BurpProxyAddress"
        private const val KEY_FINGERPRINT = "Fingerprint"
        private const val KEY_HEX_CLIENT_HELLO = "HexClientHello"
        private const val KEY_USE_INTERCEPTED_FINGERPRINT = "UseInterceptedFingerprint"
        private const val KEY_HTTP_TIMEOUT = "HttpTimeout"

        const val DEFAULT_SPOOF_PROXY_ADDRESS = "127.0.0.1:8887"
        const val DEFAULT_INTERCEPT_PROXY_ADDRESS = "127.0.0.1:8886"
        const val DEFAULT_BURP_PROXY_ADDRESS = "127.0.0.1:8080"
        const val DEFAULT_HTTP_TIMEOUT = 30
        const val DEFAULT_TLS_FINGERPRINT = "default"
    }
}