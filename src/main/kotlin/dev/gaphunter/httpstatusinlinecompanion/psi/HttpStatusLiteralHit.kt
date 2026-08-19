package dev.gaphunter.httpstatusinlinecompanion.psi

/**
 * One numeric literal in source that (a) is a real IANA-registered
 * HTTP status code per
 * [dev.gaphunter.httpstatusinlinecompanion.model.HttpStatusRegistry]
 * AND (b) sits in a context with reasonable HTTP signal per
 * [dev.gaphunter.httpstatusinlinecompanion.detect.HttpSignalNames] --
 * both conditions must hold before a finder ever produces one of these.
 * [officialName] is carried on the hit itself (not re-looked-up later)
 * so the highlighting pass never needs a second registry lookup.
 */
data class HttpStatusLiteralHit(
    val code: Int,
    val officialName: String,
    val literalEndOffset: Int,
)
