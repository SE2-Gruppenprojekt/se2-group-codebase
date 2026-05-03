package at.se2group.backend.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get


@SpringBootTest
@AutoConfigureMockMvc
class LeaderboardControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `getLeaderboard should return 200`() {
        mockMvc.get("/api/leaderboard")
            .andExpect {
                status { isOk() }
            }
    }
}
