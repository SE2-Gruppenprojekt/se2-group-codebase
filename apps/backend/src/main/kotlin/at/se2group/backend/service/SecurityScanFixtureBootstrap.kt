package at.se2group.backend.service

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * Seeds a small deterministic backend state used by the security scan workflows.
 *
 * The ZAP Automation Framework can import the API contract from `/v3/api-docs`,
 * but a few stateful endpoints still need real identifiers and valid existing
 * backend state to produce meaningful traffic. This bootstrap creates that
 * narrow scan-safe fixture so the AF plan can exercise representative lobby and
 * game flows without depending on ad hoc manual data.
 *
 * The fixture is intentionally small:
 *
 * - one open lobby for valid lobby reads and idempotent ready/settings flows
 * - one active game with a current draft for valid game reads and draft updates
 *
 * The data is recreated idempotently on startup using fixed identifiers. Tests
 * disable this component through `app.scan-fixture.enabled=false` so the
 * fixture does not pollute unrelated backend test scenarios.
 */
@Component
@ConditionalOnProperty(
    prefix = "app.scan-fixture",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false
)
class SecurityScanFixtureBootstrap(
    private val securityScanFixtureService: SecurityScanFixtureService
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        securityScanFixtureService.recreateFixture()
    }
}
