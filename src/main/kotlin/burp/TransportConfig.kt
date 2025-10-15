package burp

/**
 * Represents the configuration for transport.
 */
class TransportConfig {
    /*
     * Hostname.
     */
    var Host: String = ""

    /**
     * Protocol scheme (HTTP or HTTPS).
     */
    var Scheme: String = ""

    /**
     * Intercept ClientHello Proxy Address.
     */
    var InterceptProxyAddr: String = ""

    /**
     * Burp Proxy Address.
     */
    var BurpAddr: String = ""

    /**
     * The TLS fingerprint to use.
     */
    var Fingerprint: String = ""

    /*
     * Hexadecimal Client Hello
     */
    var HexClientHello: String = ""

    /*
     * Use intercepted fingerprint from request;
     */
    var UseInterceptedFingerprint: Boolean = false

    /**
     * The maximum amount of time to wait for an HTTP response.
     */
    var HttpTimeout: Int = 0

    /**
     * the order of headers to be sent in the request.
     */
    lateinit var HeaderOrder: Array<String>
}