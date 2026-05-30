package at.se2group.backend.web

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * MVC tests for [SiteMetadataController].
 *
 * The assertions cover both static metadata endpoints so the exact response
 * body and declared content type remain part of the public backend contract.
 */
@WebMvcTest(SiteMetadataController::class)
class SiteMetadataControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `robots txt returns plain text crawl policy`() {
        mockMvc.get("/robots.txt")
            .andExpect {
                status { isOk() }
                content { contentTypeCompatibleWith(MediaType.TEXT_PLAIN) }
                content {
                    string(
                        """
                        User-agent: *
                        Disallow:
                        """.trimIndent()
                    )
                }
            }
    }

    @Test
    fun `sitemap xml returns application xml sitemap`() {
        mockMvc.get("/sitemap.xml")
            .andExpect {
                status { isOk() }
                content { contentTypeCompatibleWith(MediaType.APPLICATION_XML) }
                content {
                    xml(
                        """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
                            <url>
                                <loc>https://se2-group-codebase.onrender.com/actuator/health</loc>
                            </url>
                        </urlset>
                        """.trimIndent()
                    )
                }
            }
    }
}
