package at.se2group.backend.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RootController {

    @GetMapping("/")
    fun root(): Map<String, String> =
        mapOf(
            "service" to "SE2 Rummikub Backend",
            "status" to "running"
        )
}
