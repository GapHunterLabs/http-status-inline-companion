# HTTP Status Code Inline Companion

IntelliJ-family plugin. Shows the official name of any HTTP status
code (`404 -> Not Found`, `503 -> Service Unavailable`) inline, right
after the numeric literal, in Java and Kotlin code — no need to look
it up. Works in any code context, not tied to a specific HTTP
framework's own annotations.

## Why it exists

An original idea, not a port of an existing competitor — validated
against this catalog's own idea-validation discipline before
being built: (1) confirmed no plugin in this catalog or in JetBrains
Marketplace does exactly this (the closest results in a Marketplace
search are full IDE-suite plugins like Toolset without this feature
isolated on its own); (2) confirmed buildable in the ~10-day budget
with a technique this catalog already has proven — the same inlay
rendering mechanism Error Lens Companion and Regex Named Group
Companion already ship. Same "apuesta consciente sin ancla de
mercado" treatment as Refactor Simulator / Test Scaffold Companion:
v0.1 ships free, no time/marketing investment disproportionate to
real demand signal until there's evidence of adoption.

## Detection heuristic

The real risk with this idea is noise: a `404` in code can be an HTTP
status, but it can just as easily be a user ID, a port, a timeout in
milliseconds, or a loop bound. Showing the inlay on *every* numeric
literal that happens to match a real status code would be an
unacceptable false-positive rate, not a feature. So the plugin only
lights up when a candidate literal (a real IANA-registered code,
100–599) sits in one of these specific contexts:

- **A call whose method name looks like status handling** —
  `setStatus(404)`, `sendError(500)`, `sendRedirect(301)`,
  `HttpStatus.valueOf(404)`, `.status(200)`, `.reply(404)` — matched
  by simple method name (case-insensitive), not by resolving the
  receiver type, so it works the same whether the call is against
  `javax.servlet.HttpServletResponse`, `jakarta.servlet.HttpServletResponse`,
  a test double, or a hand-rolled equivalent.
- **An annotation whose value looks like a status** —
  `@ResponseStatus(404)`, `@ApiResponse(code = 404)` (Spring/
  OpenAPI-style conventions, matched by annotation simple name).
- **A comparison against an identifier that looks like it holds a
  status** — `statusCode == 404`, `httpCode != 500`, `respStatus ==
  200` — matched by substring on the identifier's name (`status`,
  `httpcode`, `errorcode`, `respcode`), since real code names this
  field far more variably than it names a method.

**What deliberately does NOT trigger the hint**, even when the number
matches a real status code:

- `for (i in 0..500)` — a loop bound, no HTTP-shaped context nearby.
- `Thread.sleep(404)` — an arbitrary delay used as a literal, not a
  status handling call.
- `val port = 443` — 443 happens to also be a valid HTTPS port, but
  `port` isn't a status-holder name and there's no status-handling
  call around it.

And a number that merely *looks* plausible (3 digits, in the 100–599
range) but isn't a real IANA-registered code — `499` (an Nginx
convention), `250`, `419` (a Laravel convention) — never gets an
invented name, even sitting inside a strong-signal context like
`setStatus(499)`. The plugin never fabricates a status name; it only
shows one it can back with the real spec.

## Why built this way

- **The status table is a fixed, hand-rolled spec, never a
  dependency or generated at runtime** — sourced from RFC 9110 §15
  and the real IANA HTTP Status Code Registry, same pattern already
  proven in this catalog for `NginxDirectiveIndex`. Deliberately
  includes real-but-less-common
  codes from other RFCs in the same registry (WebDAV's 207/422/423/424,
  451 "Unavailable For Legal Reasons", 425 "Too Early") that
  developers do actually type, while deliberately excluding
  widely-used-but-never-standardized vendor conventions (Nginx's 499,
  Laravel's 419) — showing an invented name for those would violate
  the plugin's own "never fabricate a name" rule.
- **Name-based signal matching, not resolved-symbol-based.** Detecting
  intent from a method/annotation's simple name (rather than resolving
  it to a specific class) means the plugin works identically across
  Servlet API, Spring, Ktor, and hand-rolled HTTP layers, without a
  dependency on any specific framework plugin.
- **Same inlay rendering pattern already proven twice in this
  catalog** (`TextEditorHighlightingPassFactoryRegistrar` +
  `EditorCustomElementRenderer` + `InlayModel`), first shipped in
  Error Lens Companion, reused again by Regex Named Group Companion —
  no new rendering mechanism invented here.
- **`supportsKotlinPluginMode` declared explicitly.** Same real,
  evidence-derived gotcha already documented for Highlight Companion
  and Regex Named Group Companion: without it, the whole plugin can
  silently fail to load once the Kotlin plugin's K2 mode is the
  default. `KotlinHttpStatusFinder` only walks structural PSI
  (`KtConstantExpression`, `KtCallExpression`, `KtBinaryExpression`,
  `KtAnnotationEntry`) and never calls any resolve/analysis API, so
  both K1 and K2 are genuinely supported.
- **100% local** — no network call, no account, no telemetry. The
  status-code lookup is a static, bundled table.

## Usage

Open any `.java` or `.kt` file. Any HTTP status code literal sitting
in one of the signal contexts above gets its official name inline,
automatically — no action needed, nothing to configure in v0.1.

## v0.1 scope

Free, all of it — no paywall, nothing held back for a future tier.
Java and Kotlin only, the standard RFC 9110 / IANA registry only.
Deferred to a possible future v0.2 (not started, not promised):
custom status codes from well-known APIs (Stripe, GitHub, AWS each
have their own extensions/nuances), and codes defined in a
project-local config file.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us
at **gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
