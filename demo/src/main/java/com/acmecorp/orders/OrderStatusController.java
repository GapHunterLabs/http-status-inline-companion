package com.acmecorp.orders;

import javax.servlet.http.HttpServletResponse;

/** Real HTTP status contexts, for live demo of HTTP Status Code Inline Companion. */
public class OrderStatusController {

    // Strong signal: setStatus/sendError call -- SHOULD show inline hints.
    void handleMissingOrder(HttpServletResponse response) throws java.io.IOException {
        response.setStatus(404);
        response.sendError(500);
    }

    // Strong signal: comparison against a status-shaped identifier -- SHOULD show inline hints.
    boolean isSuccessfulResponse(int statusCode) {
        return statusCode == 200 || statusCode == 201;
    }

    // No signal at all -- should NOT show any hint, even though these look like status codes.
    void unrelatedLoop() {
        for (int i = 0; i < 500; i++) {
            System.out.println(i);
        }
    }

    // A number that merely looks plausible but isn't a real IANA code -- should NEVER get an
    // invented name even in a strong-signal context.
    void nonStandardCode(HttpServletResponse response) {
        response.setStatus(499);
    }
}
