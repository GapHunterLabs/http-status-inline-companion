package dev.gaphunter.httpstatusinlinecompanion.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpStatusRegistryTest {

    @Test
    fun `known common codes resolve to their official RFC 9110 name`() {
        assertEquals("OK", HttpStatusRegistry.nameFor(200))
        assertEquals("Not Found", HttpStatusRegistry.nameFor(404))
        assertEquals("Internal Server Error", HttpStatusRegistry.nameFor(500))
        assertEquals("Service Unavailable", HttpStatusRegistry.nameFor(503))
        assertEquals("Too Many Requests", HttpStatusRegistry.nameFor(429))
    }

    @Test
    fun `boundary codes of the real registry resolve correctly`() {
        // 100 is the lowest real IANA-registered status code.
        assertEquals("Continue", HttpStatusRegistry.nameFor(100))
        // 511 is the highest real IANA-registered status code (Network Authentication Required).
        assertEquals("Network Authentication Required", HttpStatusRegistry.nameFor(511))
    }

    @Test
    fun `599 is in the 100-599 numeric range but is not a real registered code`() {
        // The brief's own boundary case: 599 is the top of the "looks like an HTTP code"
        // 3-digit range, but IANA has no code that high -- must never be fabricated.
        assertNull(HttpStatusRegistry.nameFor(599))
        assertFalse(HttpStatusRegistry.isKnownStatusCode(599))
    }

    @Test
    fun `numbers that look plausible but are not real registered codes return null, never an invented name`() {
        assertNull(HttpStatusRegistry.nameFor(499)) // Nginx convention, not IANA-registered
        assertNull(HttpStatusRegistry.nameFor(250)) // plausible 3-digit number, not a real code
        assertNull(HttpStatusRegistry.nameFor(419)) // Laravel convention, not IANA-registered
    }

    @Test
    fun `numbers outside the 100-599 status range are never known codes`() {
        assertFalse(HttpStatusRegistry.isKnownStatusCode(0))
        assertFalse(HttpStatusRegistry.isKnownStatusCode(42))
        assertFalse(HttpStatusRegistry.isKnownStatusCode(600))
        assertFalse(HttpStatusRegistry.isKnownStatusCode(1000))
    }

    @Test
    fun `isKnownStatusCode agrees with nameFor for a real code`() {
        assertTrue(HttpStatusRegistry.isKnownStatusCode(201))
        assertEquals("Created", HttpStatusRegistry.nameFor(201))
    }
}
