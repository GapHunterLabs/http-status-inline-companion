package dev.gaphunter.httpstatusinlinecompanion.detect

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpSignalNamesTest {

    @Test
    fun `recognizes common HTTP-status-shaped method names case-insensitively`() {
        assertTrue(HttpSignalNames.isSignalMethodName("setStatus"))
        assertTrue(HttpSignalNames.isSignalMethodName("SETSTATUS"))
        assertTrue(HttpSignalNames.isSignalMethodName("sendError"))
        assertTrue(HttpSignalNames.isSignalMethodName("valueOf"))
        assertTrue(HttpSignalNames.isSignalMethodName("withStatus"))
    }

    @Test
    fun `does not treat unrelated method names as signal`() {
        assertFalse(HttpSignalNames.isSignalMethodName("sleep"))
        assertFalse(HttpSignalNames.isSignalMethodName("get"))
        assertFalse(HttpSignalNames.isSignalMethodName("repeat"))
    }

    @Test
    fun `recognizes HTTP-status-shaped annotation names case-insensitively`() {
        assertTrue(HttpSignalNames.isSignalAnnotationName("ResponseStatus"))
        assertTrue(HttpSignalNames.isSignalAnnotationName("ApiResponse"))
    }

    @Test
    fun `does not treat unrelated annotation names as signal`() {
        assertFalse(HttpSignalNames.isSignalAnnotationName("Deprecated"))
        assertFalse(HttpSignalNames.isSignalAnnotationName("Override"))
    }

    @Test
    fun `recognizes identifiers with a status-shaped substring`() {
        assertTrue(HttpSignalNames.looksLikeStatusHolderName("statusCode"))
        assertTrue(HttpSignalNames.looksLikeStatusHolderName("httpStatus"))
        assertTrue(HttpSignalNames.looksLikeStatusHolderName("respStatus"))
        assertTrue(HttpSignalNames.looksLikeStatusHolderName("status"))
    }

    @Test
    fun `does not treat unrelated identifiers as status holders`() {
        assertFalse(HttpSignalNames.looksLikeStatusHolderName("userId"))
        assertFalse(HttpSignalNames.looksLikeStatusHolderName("port"))
        assertFalse(HttpSignalNames.looksLikeStatusHolderName("timeoutMs"))
        assertFalse(HttpSignalNames.looksLikeStatusHolderName("i"))
    }
}
