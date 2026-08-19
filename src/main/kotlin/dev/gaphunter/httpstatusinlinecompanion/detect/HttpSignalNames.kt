package dev.gaphunter.httpstatusinlinecompanion.detect

/**
 * The name-matching heuristic shared by both the Java and Kotlin PSI
 * walkers: does an identifier (method name, annotation name, or the
 * name of "the other side" of a comparison) look like it's about HTTP
 * status, strongly enough to justify showing an inlay on a nearby
 * numeric literal?
 *
 * This is the plugin's actual anti-false-positive design, documented
 * here (not just in the README) because it's the part most likely to
 * need tuning later: every literal 100-599 that is a real IANA code
 * (per [dev.gaphunter.httpstatusinlinecompanion.model.HttpStatusRegistry])
 * is a CANDIDATE, but the inlay only renders when the candidate sits in
 * one of a fixed set of "reasonable signal" contexts -- never on every
 * 3-digit literal in a file, which would flood ordinary code (`for (i
 * in 0..500)`, `Thread.sleep(404)` used as an arbitrary delay,
 * `val port = 443`-adjacent numbers that aren't the port itself, an
 * array of exactly 404 elements) with wrong hints. See the README's
 * "Detection heuristic" section for worked examples of both sides.
 */
object HttpSignalNames {

    /**
     * Method/function *simple* names (case-insensitive) whose argument
     * list is treated as strong HTTP-status signal when a candidate
     * literal appears in it -- real APIs from the JDK Servlet API,
     * Spring, Ktor, Python's stdlib/Flask/FastAPI, and generic
     * naming conventions any hand-rolled HTTP layer tends to reuse.
     * Deliberately name-based, not resolved-symbol-based (see
     * [dev.gaphunter.httpstatusinlinecompanion.psi.JavaHttpStatusFinder]'s
     * doc comment for why): this plugin works the same whether the
     * receiver is `javax.servlet.HttpServletResponse`,
     * `jakarta.servlet.HttpServletResponse`, a test double, or a
     * hand-rolled equivalent, because a call is never resolved to a
     * specific type.
     */
    private val SIGNAL_METHOD_NAMES = setOf(
        "setstatus",
        "senderror",
        "sendredirect",
        "status",
        "httpstatus",
        "statuscode",
        "responsestatus",
        "withstatus",
        "returnstatus",
        "reply",
        "valueof",
        "resolve",
    )

    /**
     * Annotation *simple* names (case-insensitive, no package prefix
     * needed since this is name-based) whose value/argument is treated
     * as strong signal -- Spring's `@ResponseStatus`, OpenAPI/Swagger's
     * `@ApiResponse`, JAX-RS-adjacent conventions.
     */
    private val SIGNAL_ANNOTATION_NAMES = setOf(
        "responsestatus",
        "apiresponse",
        "apiresponses",
        "statuscode",
    )

    /**
     * Substrings (case-insensitive) that make an identifier (variable,
     * field, parameter, property) look like it holds an HTTP status --
     * used for the "compared against a status-ish name" context
     * (`if (response.statusCode == 404)`, `httpStatus != 500`). Plain
     * substrings, not a closed set of exact names, because real code
     * varies (`statusCode`, `respStatus`, `httpCode`, `status_code`)
     * far more than method names do for this specific check.
     */
    private val SIGNAL_NAME_SUBSTRINGS = listOf("status", "httpcode", "errorcode", "respcode")

    fun isSignalMethodName(simpleName: String): Boolean =
        simpleName.lowercase() in SIGNAL_METHOD_NAMES

    fun isSignalAnnotationName(simpleName: String): Boolean =
        simpleName.lowercase() in SIGNAL_ANNOTATION_NAMES

    fun looksLikeStatusHolderName(identifier: String): Boolean {
        val lower = identifier.lowercase()
        return SIGNAL_NAME_SUBSTRINGS.any { lower.contains(it) }
    }
}
