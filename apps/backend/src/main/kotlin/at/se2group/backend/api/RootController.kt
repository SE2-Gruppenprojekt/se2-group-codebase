package at.se2group.backend.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Exposes a minimal root endpoint for quick service identification.
 *
 * The backend root path is intentionally lightweight and returns a small JSON
 * object that confirms which service answered the request and whether it is
 * currently running. This is useful for quick manual checks, simple monitoring,
 * and environments where a caller reaches `/` before it knows any application-
 * specific endpoint.
 *
 * The returned map is part of the public HTTP contract and is therefore kept
 * intentionally small and stable.
 */
@RestController
class RootController {

    @GetMapping("/")
    fun root(): Map<String, String> =
        // Use a fixed two-field payload so health-style callers and manual
        // checks get predictable service metadata without extra parsing.
        mapOf(
            "service" to "SE2 Rummikub Backend",
            "status" to "running"
        )
}
