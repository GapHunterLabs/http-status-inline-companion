package dev.gaphunter.httpstatusinlinecompanion.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JavaHttpStatusFinderTest : BasePlatformTestCase() {

    // --- Strong signal: method call -> MUST show the hint ---

    fun testSetStatusCallOnAKnownCodeProducesAHit() {
        val file = myFixture.configureByText(
            "OrderController.java",
            """
            class OrderController {
                void handle(javax.servlet.http.HttpServletResponse response) {
                    response.setStatus(404);
                }
            }
            """.trimIndent(),
        )

        val hits = JavaHttpStatusFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(404, hits[0].code)
        assertEquals("Not Found", hits[0].officialName)
    }

    fun testSendErrorCallOnAKnownCodeProducesAHit() {
        val file = myFixture.configureByText(
            "OrderController.java",
            """
            class OrderController {
                void handle(javax.servlet.http.HttpServletResponse response) throws java.io.IOException {
                    response.sendError(503);
                }
            }
            """.trimIndent(),
        )

        val hits = JavaHttpStatusFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(503, hits[0].code)
        assertEquals("Service Unavailable", hits[0].officialName)
    }

    // --- Strong signal: annotation -> MUST show the hint ---

    fun testResponseStatusAnnotationShorthandProducesAHit() {
        val file = myFixture.configureByText(
            "NotFoundException.java",
            """
            @ResponseStatus(404)
            class NotFoundException extends RuntimeException {
            }
            """.trimIndent(),
        )

        val hits = JavaHttpStatusFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(404, hits[0].code)
    }

    fun testResponseStatusAnnotationNamedAttributeProducesAHit() {
        val file = myFixture.configureByText(
            "NotFoundException.java",
            """
            @ResponseStatus(value = 404)
            class NotFoundException extends RuntimeException {
            }
            """.trimIndent(),
        )

        val hits = JavaHttpStatusFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(404, hits[0].code)
    }

    // --- Strong signal: comparison against a status-shaped name -> MUST show the hint ---

    fun testComparisonAgainstAStatusCodeFieldProducesAHit() {
        val file = myFixture.configureByText(
            "OrderService.java",
            """
            class OrderService {
                void handle(int statusCode) {
                    if (statusCode == 404) {
                        retry();
                    }
                }

                void retry() {}
            }
            """.trimIndent(),
        )

        val hits = JavaHttpStatusFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(404, hits[0].code)
    }

    // --- THE most important test: same number, no signal context -> MUST NOT show the hint ---

    fun testTheSameNumberWithNoHttpSignalProducesNoHit() {
        val file = myFixture.configureByText(
            "OrderBatch.java",
            """
            class OrderBatch {
                void process() {
                    for (int i = 0; i < 500; i++) {
                        doWork(i);
                    }
                }

                void doWork(int i) {}
            }
            """.trimIndent(),
        )

        val hits = JavaHttpStatusFinder.findAll(file)
        assertTrue("A loop bound that happens to match a real HTTP code must never produce a hint", hits.isEmpty())
    }

    fun testAPlainIntFieldAssignmentWithNoSignalNameProducesNoHit() {
        val file = myFixture.configureByText(
            "Config.java",
            """
            class Config {
                private int userId = 404;
                private int timeoutMs = 500;
            }
            """.trimIndent(),
        )

        val hits = JavaHttpStatusFinder.findAll(file)
        assertTrue("An unrelated field named userId/timeoutMs must never produce a hint even if the value matches a real code", hits.isEmpty())
    }

    fun testAnArgumentToAnUnrelatedMethodProducesNoHit() {
        val file = myFixture.configureByText(
            "Worker.java",
            """
            class Worker {
                void run() {
                    Thread.sleep(404);
                }
            }
            """.trimIndent(),
        )

        val hits = JavaHttpStatusFinder.findAll(file)
        assertTrue(hits.isEmpty())
    }

    // --- Not a real registered code at all -> MUST NOT show the hint, even with strong signal ---

    fun testANumberThatLooksPlausibleButIsNotARealCodeProducesNoHitEvenWithSignal() {
        val file = myFixture.configureByText(
            "Controller.java",
            """
            class Controller {
                void handle(javax.servlet.http.HttpServletResponse response) {
                    response.setStatus(499);
                }
            }
            """.trimIndent(),
        )

        val hits = JavaHttpStatusFinder.findAll(file)
        assertTrue("499 is not a real IANA-registered code (it's an Nginx convention) -- must never be shown, even with setStatus signal", hits.isEmpty())
    }

    fun testAnotherPlausibleButUnregisteredNumberInSignalContextProducesNoHit() {
        val file = myFixture.configureByText(
            "Controller.java",
            """
            class Controller {
                void handle(javax.servlet.http.HttpServletResponse response) {
                    response.setStatus(250);
                }
            }
            """.trimIndent(),
        )

        val hits = JavaHttpStatusFinder.findAll(file)
        assertTrue(hits.isEmpty())
    }

    // --- Boundary codes ---

    fun testBoundaryCode100WithSignalProducesAHit() {
        val file = myFixture.configureByText(
            "Controller.java",
            """
            class Controller {
                void handle(javax.servlet.http.HttpServletResponse response) {
                    response.setStatus(100);
                }
            }
            """.trimIndent(),
        )

        val hits = JavaHttpStatusFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals("Continue", hits[0].officialName)
    }

    fun testBoundaryCode599WithSignalProducesNoHitBecauseItIsNotARealCode() {
        val file = myFixture.configureByText(
            "Controller.java",
            """
            class Controller {
                void handle(javax.servlet.http.HttpServletResponse response) {
                    response.setStatus(599);
                }
            }
            """.trimIndent(),
        )

        val hits = JavaHttpStatusFinder.findAll(file)
        assertTrue("599 is the top of the 3-digit range but IANA has no code that high", hits.isEmpty())
    }
}
