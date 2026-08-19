package dev.gaphunter.httpstatusinlinecompanion.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KotlinHttpStatusFinderTest : BasePlatformTestCase() {

    // --- Strong signal: call -> MUST show the hint ---

    fun testStatusCallOnAKnownCodeProducesAHit() {
        val file = myFixture.configureByText(
            "OrderController.kt",
            """
            class OrderController {
                fun handle(call: RoutingCall) {
                    call.response.status(404)
                }
            }
            """.trimIndent(),
        )

        val hits = KotlinHttpStatusFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(404, hits[0].code)
        assertEquals("Not Found", hits[0].officialName)
    }

    fun testNamedArgumentWithSignalNameProducesAHit() {
        val file = myFixture.configureByText(
            "OrderController.kt",
            """
            class OrderController {
                fun handle() {
                    respondWithStatus(status = 503)
                }

                fun respondWithStatus(status: Int) {}
            }
            """.trimIndent(),
        )

        val hits = KotlinHttpStatusFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(503, hits[0].code)
    }

    // --- Strong signal: annotation -> MUST show the hint ---

    fun testResponseStatusAnnotationProducesAHit() {
        val file = myFixture.configureByText(
            "NotFoundException.kt",
            """
            @ResponseStatus(404)
            class NotFoundException : RuntimeException()
            """.trimIndent(),
        )

        val hits = KotlinHttpStatusFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(404, hits[0].code)
    }

    // --- Strong signal: comparison -> MUST show the hint ---

    fun testComparisonAgainstAStatusCodeValProducesAHit() {
        val file = myFixture.configureByText(
            "OrderService.kt",
            """
            class OrderService {
                fun handle(statusCode: Int) {
                    if (statusCode == 404) {
                        retry()
                    }
                }

                fun retry() {}
            }
            """.trimIndent(),
        )

        val hits = KotlinHttpStatusFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(404, hits[0].code)
    }

    // --- THE most important test: same number, no signal context -> MUST NOT show the hint ---

    fun testTheSameNumberWithNoHttpSignalProducesNoHit() {
        val file = myFixture.configureByText(
            "OrderBatch.kt",
            """
            class OrderBatch {
                fun process() {
                    for (i in 0..500) {
                        doWork(i)
                    }
                }

                fun doWork(i: Int) {}
            }
            """.trimIndent(),
        )

        val hits = KotlinHttpStatusFinder.findAll(file)
        assertTrue("A range bound that happens to match a real HTTP code must never produce a hint", hits.isEmpty())
    }

    fun testAPlainValAssignmentWithNoSignalNameProducesNoHit() {
        val file = myFixture.configureByText(
            "Config.kt",
            """
            class Config {
                val userId = 404
                val timeoutMs = 500
            }
            """.trimIndent(),
        )

        val hits = KotlinHttpStatusFinder.findAll(file)
        assertTrue("An unrelated val named userId/timeoutMs must never produce a hint even if the value matches a real code", hits.isEmpty())
    }

    fun testAnArgumentToAnUnrelatedFunctionProducesNoHit() {
        val file = myFixture.configureByText(
            "Worker.kt",
            """
            class Worker {
                fun run() {
                    Thread.sleep(404)
                }
            }
            """.trimIndent(),
        )

        val hits = KotlinHttpStatusFinder.findAll(file)
        assertTrue(hits.isEmpty())
    }

    // --- Not a real registered code at all -> MUST NOT show the hint, even with strong signal ---

    fun testANumberThatLooksPlausibleButIsNotARealCodeProducesNoHitEvenWithSignal() {
        val file = myFixture.configureByText(
            "OrderController.kt",
            """
            class OrderController {
                fun handle(call: RoutingCall) {
                    call.response.status(499)
                }
            }
            """.trimIndent(),
        )

        val hits = KotlinHttpStatusFinder.findAll(file)
        assertTrue("499 is not a real IANA-registered code -- must never be shown, even with status() signal", hits.isEmpty())
    }

    // --- Kotlin-specific: string interpolation / non-integer constants must never crash or false-positive ---

    fun testStringLiteralArgumentToASignalCallProducesNoHit() {
        val file = myFixture.configureByText(
            "OrderController.kt",
            """
            class OrderController {
                fun handle(call: RoutingCall) {
                    call.response.status("404")
                }
            }
            """.trimIndent(),
        )

        val hits = KotlinHttpStatusFinder.findAll(file)
        assertTrue(hits.isEmpty())
    }

    // --- Boundary codes ---

    fun testBoundaryCode100WithSignalProducesAHit() {
        val file = myFixture.configureByText(
            "OrderController.kt",
            """
            class OrderController {
                fun handle(call: RoutingCall) {
                    call.response.status(100)
                }
            }
            """.trimIndent(),
        )

        val hits = KotlinHttpStatusFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals("Continue", hits[0].officialName)
    }

    fun testBoundaryCode599WithSignalProducesNoHitBecauseItIsNotARealCode() {
        val file = myFixture.configureByText(
            "OrderController.kt",
            """
            class OrderController {
                fun handle(call: RoutingCall) {
                    call.response.status(599)
                }
            }
            """.trimIndent(),
        )

        val hits = KotlinHttpStatusFinder.findAll(file)
        assertTrue(hits.isEmpty())
    }
}
