package at.se2group.backend.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * MVC tests for [SiteMetadataController].
 *
 * This class covers the two static site metadata endpoints exposed by the
 * backend: `robots.txt` and `sitemap.xml`.
 *
 * Even though both endpoints return static content, they are still public HTTP
 * contracts and can be consumed by crawlers, infrastructure tooling, and
 * external inspection tools. The tests therefore verify the response at the
 * MVC layer rather than only calling controller methods directly.
 *
 * Coverage goals in this test:
 *
 * - both routes are mapped correctly
 * - each route returns `200 OK`
 * - the declared content type matches the intended file type
 * - the body content stays stable and human-readable
 * - the sitemap keeps pointing at the intended backend health location
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
                // The content type is part of the HTTP contract, not just the
                // body string, because crawlers interpret the file by media type.
                content { contentTypeCompatibleWith(MediaType.TEXT_PLAIN) }
                content {
                    string(
                        """
                        User-agent: *
                        Disallow:
                        """.trimIndent()
                    )
                }
                header { string("X-Content-Type-Options", "nosniff") }
                header { string("Cross-Origin-Resource-Policy", "same-origin") }
                header { string("Cross-Origin-Embedder-Policy", "require-corp") }
                header { string("Cross-Origin-Opener-Policy", "same-origin") }
                header { string("Strict-Transport-Security", "max-age=31536000; includeSubDomains") }
                header { string("Cache-Control", "no-cache, no-store, must-revalidate, private") }
                header { string("Pragma", "no-cache") }
                header { string("Expires", "0") }
            }
    }

    @Test
    fun `sitemap xml returns application xml sitemap`() {
        mockMvc.get("/sitemap.xml")
            .andExpect {
                status { isOk() }
                // Keep the XML media type explicit so the endpoint behaves like
                // a real sitemap resource instead of a generic text response.
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
                header { string("X-Content-Type-Options", "nosniff") }
                header { string("Cross-Origin-Resource-Policy", "same-origin") }
                header { string("Cross-Origin-Embedder-Policy", "require-corp") }
                header { string("Cross-Origin-Opener-Policy", "same-origin") }
                header { string("Strict-Transport-Security", "max-age=31536000; includeSubDomains") }
                header { string("Cache-Control", "no-cache, no-store, must-revalidate, private") }
                header { string("Pragma", "no-cache") }
                header { string("Expires", "0") }
            }
    }
}
