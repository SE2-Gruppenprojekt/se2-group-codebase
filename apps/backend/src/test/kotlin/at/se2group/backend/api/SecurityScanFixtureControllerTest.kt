package at.se2group.backend.api

import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.persistence.LobbyRepository
import at.se2group.backend.persistence.TurnDraftRepository
import at.se2group.backend.service.SecurityScanFixtureService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = [
        "app.scan-fixture.enabled=true",
        "app.scan-fixture.secret=test-scan-secret"
    ]
)
@AutoConfigureMockMvc
@Transactional
class SecurityScanFixtureControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var lobbyRepository: LobbyRepository

    @Autowired
    lateinit var gameRepository: GameRepository

    @Autowired
    lateinit var turnDraftRepository: TurnDraftRepository

    @Test
    fun `returns 403 when scan secret header is missing`() {
        mockMvc.post("/internal/security/scan-fixture")
            .andExpect {
                status { isForbidden() }
                jsonPath("$.errorCode") { value("FORBIDDEN") }
                jsonPath("$.errorMessage") { value("Invalid scan fixture secret") }
            }
    }

    @Test
    fun `returns 403 when scan secret header is wrong`() {
        mockMvc.post("/internal/security/scan-fixture") {
            header("X-Scan-Secret", "wrong-secret")
        }
            .andExpect {
                status { isForbidden() }
                jsonPath("$.errorCode") { value("FORBIDDEN") }
                jsonPath("$.errorMessage") { value("Invalid scan fixture secret") }
            }
    }

    @Test
    fun `returns fixture ids when scan secret is valid`() {
        mockMvc.post("/internal/security/scan-fixture") {
            header("X-Scan-Secret", "test-scan-secret")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.lobbyId") { value(SecurityScanFixtureService.SCAN_OPEN_LOBBY_ID) }
                jsonPath("$.hostUserId") { value(SecurityScanFixtureService.SCAN_HOST_USER_ID) }
                jsonPath("$.guestUserId") { value(SecurityScanFixtureService.SCAN_GUEST_USER_ID) }
                jsonPath("$.gameId") { value(SecurityScanFixtureService.SCAN_GAME_ID) }
                jsonPath("$.draftOwnerUserId") { value(SecurityScanFixtureService.SCAN_HOST_USER_ID) }
                jsonPath("$.hostAccessToken") { isNotEmpty() }
                jsonPath("$.guestAccessToken") { isNotEmpty() }
            }

        assertTrue(lobbyRepository.findById(SecurityScanFixtureService.SCAN_OPEN_LOBBY_ID).isPresent)
        assertTrue(lobbyRepository.findById(SecurityScanFixtureService.SCAN_ACTIVE_LOBBY_ID).isPresent)
        assertTrue(gameRepository.findById(SecurityScanFixtureService.SCAN_GAME_ID).isPresent)
        assertTrue(turnDraftRepository.findById(SecurityScanFixtureService.SCAN_GAME_ID).isPresent)
    }
}
