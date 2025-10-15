package burp


/**
 * Represents the configuration for transport.
 */
class TransportConfig {
    /*
     * Hostname.
     */
    var Host: String? = null

    /**
     * Protocol scheme (HTTP or HTTPS).
     */
    var Scheme: String? = null

    /**
     * Intercept ClientHello Proxy Address.
     */
    var InterceptProxyAddr: String? = null

    /**
     * Burp Proxy Address.
     */
    var BurpAddr: String? = null

    /**
     * The TLS fingerprint to use.
     */
    var Fingerprint: String? = null

    /*
     * Hexadecimal Client Hello
     */
    var HexClientHello: String? = null

    /*
     * Use intercepted fingerprint from request;
     */
    var UseInterceptedFingerprint: Boolean? = null

    /**
     * The maximum amount of time to wait for an HTTP response.
     */
    var HttpTimeout: Int = 0

    /**
     * the order of headers to be sent in the request.
     */
    var HeaderOrder: Array<String?>?
}