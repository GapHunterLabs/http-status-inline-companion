<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# HTTP Status Code Inline Companion Changelog

## [Unreleased]

## [0.1.0]

### Added

- **Inlay hint**: any Java or Kotlin integer literal that is a real
  IANA-registered HTTP status code (RFC 9110 and related RFCs) shows
  its official name inline, right after the number (`404 -> Not
  Found`), when it sits in a context with reasonable HTTP signal --
  a call whose method name looks like status handling
  (`setStatus(404)`, `sendError(500)`, `HttpStatus.valueOf(404)`), an
  annotation like `@ResponseStatus(404)`/`@ApiResponse(code = 404)`,
  or a comparison against a status-shaped identifier (`statusCode ==
  404`).
- Deliberately does **not** hint on an ordinary numeric literal with no
  HTTP signal (a loop bound, an unrelated field, an argument to an
  unrelated method) even when the number happens to match a real
  status code -- see README "Detection heuristic" for the full rule
  and worked examples.
- A number that merely looks plausible (3 digits, in the 100-599
  range) but isn't a real IANA-registered code -- 499 (an Nginx
  convention), 250, 419 (a Laravel convention) -- never gets an
  invented name, even in a strong-signal context.

[Unreleased]: https://github.com/GapHunterLabs/http-status-inline-companion/compare/0.1.0...HEAD
[0.1.0]: https://github.com/GapHunterLabs/http-status-inline-companion/commits/0.1.0
