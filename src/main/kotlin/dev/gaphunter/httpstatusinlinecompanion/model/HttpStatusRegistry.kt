package dev.gaphunter.httpstatusinlinecompanion.model

/**
 * The static, immutable table of official HTTP status code names,
 * source RFC 9110 §15 ("HTTP Status Codes") and the IANA HTTP Status
 * Code Registry it defines
 * (https://www.iana.org/assignments/http-status-codes). This table is
 * never inferred/generated -- it's a fixed real-world spec, same
 * "hand-roll a small stable table instead of a dependency" pattern
 * already proven for `NginxDirectiveIndex` (see `CONSTITUTION.md` §6).
 *
 * Deliberately includes codes from RFCs beyond just 9110 that are part
 * of the same official IANA registry and appear routinely in real code
 * (404/409/422/429 alone don't cover what developers actually type):
 * WebDAV (RFC 4918: 102, 207, 422, 423, 424, 507, 508), the
 * "Unavailable For Legal Reasons" 451 (RFC 7725), "Too Early" 425 (RFC
 * 8470), and the widely-used-but-never-standardized 218/419/430/450/499
 * are intentionally EXCLUDED -- they're vendor/framework conventions
 * (Nginx's 499, Laravel's 419), not IANA-registered codes, so showing
 * an invented name for them would violate this plugin's own "never
 * fabricate a name" rule (see [HttpStatusRegistry.nameFor]'s doc).
 */
object HttpStatusRegistry {

    private val CODES: Map<Int, String> = mapOf(
        // 1xx Informational
        100 to "Continue",
        101 to "Switching Protocols",
        102 to "Processing",
        103 to "Early Hints",

        // 2xx Success
        200 to "OK",
        201 to "Created",
        202 to "Accepted",
        203 to "Non-Authoritative Information",
        204 to "No Content",
        205 to "Reset Content",
        206 to "Partial Content",
        207 to "Multi-Status",
        208 to "Already Reported",
        226 to "IM Used",

        // 3xx Redirection
        300 to "Multiple Choices",
        301 to "Moved Permanently",
        302 to "Found",
        303 to "See Other",
        304 to "Not Modified",
        305 to "Use Proxy",
        307 to "Temporary Redirect",
        308 to "Permanent Redirect",

        // 4xx Client Error
        400 to "Bad Request",
        401 to "Unauthorized",
        402 to "Payment Required",
        403 to "Forbidden",
        404 to "Not Found",
        405 to "Method Not Allowed",
        406 to "Not Acceptable",
        407 to "Proxy Authentication Required",
        408 to "Request Timeout",
        409 to "Conflict",
        410 to "Gone",
        411 to "Length Required",
        412 to "Precondition Failed",
        413 to "Content Too Large",
        414 to "URI Too Long",
        415 to "Unsupported Media Type",
        416 to "Range Not Satisfiable",
        417 to "Expectation Failed",
        421 to "Misdirected Request",
        422 to "Unprocessable Content",
        423 to "Locked",
        424 to "Failed Dependency",
        425 to "Too Early",
        426 to "Upgrade Required",
        428 to "Precondition Required",
        429 to "Too Many Requests",
        431 to "Request Header Fields Too Large",
        451 to "Unavailable For Legal Reasons",

        // 5xx Server Error
        500 to "Internal Server Error",
        501 to "Not Implemented",
        502 to "Bad Gateway",
        503 to "Service Unavailable",
        504 to "Gateway Timeout",
        505 to "HTTP Version Not Supported",
        506 to "Variant Also Negotiates",
        507 to "Insufficient Storage",
        508 to "Loop Detected",
        510 to "Not Extended",
        511 to "Network Authentication Required",
    )

    /**
     * The official name for [code], or null if [code] is not a real
     * IANA-registered HTTP status code -- including numbers that merely
     * *look* plausible (100-599, 3 digits) but aren't registered, like
     * 250 or 499. Never guesses/fabricates a name for those; callers
     * must treat null as "show nothing", never invent a label.
     */
    fun nameFor(code: Int): String? = CODES[code]

    /** True for any integer that is a real, IANA-registered HTTP status code. */
    fun isKnownStatusCode(code: Int): Boolean = CODES.containsKey(code)
}
