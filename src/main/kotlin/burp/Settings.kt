package burp

import burp.api.montoya.MontoyaApi
import burp.api.montoya.persistence.Preferences

class Settings(api: MontoyaApi) {
    private val storage: Preferences = api.persistence().preferences()

    var spoofProxyAddress: String = "SpoofProxyAddress"
        get() = this.read(field, DEFAULT_SPOOF_PROXY_ADDRESS)!!
        set(spoofProxyAddress) {
            this.write(field, spoofProxyAddress)
        }
    var interceptProxyAddress: String = "InterceptProxyAddress"
        get() = this.read(field, DEFAULT_INTERCEPT_PROXY_ADDRESS)!!
        set(interceptProxyAddress) {
            this.write(field, interceptProxyAddress)
        }
    var burpProxyAddress: String = "BurpProxyAddress"
        get() = this.read(field, DEFAULT_BURP_PROXY_ADDRESS)!!
        set(burpProxyAddress) {
            this.write(field, burpProxyAddress)
        }
    var fingerprint: String = "Fingerprint"
        get() = this.read(field, DEFAULT_TLS_FINGERPRINT)!!
        set(fingerprint) {
            this.write(field, fingerprint)
        }
    var hexClientHello: String = "HexClientHello"
        get() = this.read(field, "")!!
        set(hexClientHello) {
            this.write(field, hexClientHello)
        }
    var useInterceptedFingerprint: String = "UseInterceptedFingerprint"
        get() = this.read(field, USE_INTERCEPTED_FINGERPRINT)
        set(useInterceptedFingerprint) {
            this.write(field, useInterceptedFingerprint)
        }
    var httpTimeout: String = "HttpTimeout"
        get() = this.read(field, DEFAULT_HTTP_TIMEOUT)
        set(httpTimeout) {
            this.write(field, httpTimeout)
        }

    fun read(key: String?, defaultValue: String?): String? {
        val value = this.storage.getString(key)
        if (value == null || value.isEmpty()) {
            this.write(key, defaultValue)
            return defaultValue
        }
        return value
    }

    fun read(key: String?, defaultValue: Boolean): String {
        val value = this.storage.getBoolean(key)
        if (value == null) {
            this.storage.setBoolean(key, defaultValue)
            return defaultValue
        }
        return value
    }

    fun read(key: String?, defaultValue: Int): String {
        val value = this.storage.getInteger(key)
        if (value == null) {
            this.storage.setInteger(key, defaultValue)
            return defaultValue
        }
        return value
    }

    fun write(key: String?, value: String?) {
        this.storage.setString(key, value)
    }

    fun write(key: String?, value: Boolean) {
        this.storage.setBoolean(key, value)
    }

    fun write(key: String?, value: Int) {
        this.storage.setInteger(key, value)
    }

//    val fingerprints: Array<String?>
//        get() = ServerLibrary.INSTANCE.GetFingerprints().split("\n")

    fun toTransportConfig(): TransportConfig {
        val transportConfig: TransportConfig = TransportConfig()
        transportConfig.Fingerprint = this.fingerprint
        transportConfig.HexClientHello = this.hexClientHello
        transportConfig.HttpTimeout = this.httpTimeout
        transportConfig.UseInterceptedFingerprint = this.useInterceptedFingerprint
        transportConfig.BurpAddr = this.burpProxyAddress
        transportConfig.InterceptProxyAddr = this.interceptProxyAddress
        return transportConfig
    }

    companion object {
        const val DEFAULT_SPOOF_PROXY_ADDRESS: String = "127.0.0.1:8887"
        const val DEFAULT_INTERCEPT_PROXY_ADDRESS: String = "127.0.0.1:8886"
        const val DEFAULT_BURP_PROXY_ADDRESS: String = "127.0.0.1:8080"
        const val DEFAULT_HTTP_TIMEOUT: Int = 30
        const val DEFAULT_TLS_FINGERPRINT: String = "default"
        const val USE_INTERCEPTED_FINGERPRINT: Boolean = false
    }
}