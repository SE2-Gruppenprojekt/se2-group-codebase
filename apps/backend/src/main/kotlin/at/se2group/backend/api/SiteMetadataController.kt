package at.se2group.backend.web

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Serves small public metadata resources for the deployed backend.
 *
 * The backend exposes `robots.txt` and `sitemap.xml` as conventional web
 * resources so crawlers, infrastructure tooling, and external inspection tools
 * can discover basic metadata about the deployed service without using the
 * application API.
 *
 * Even though both responses are static, they still form part of the public
 * HTTP surface of the backend. Their content type and body shape are therefore
 * treated as stable transport contracts and are covered by dedicated MVC tests.
 */
@RestController
class SiteMetadataController {

    @GetMapping("/robots.txt", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun robotsTxt(): String =
        // Keep the file intentionally minimal: crawlers are allowed and the
        // response should remain a valid plain-text robots file.
        """
        User-agent: *
        Disallow:
        """.trimIndent()

    @GetMapping("/sitemap.xml", produces = [MediaType.APPLICATION_XML_VALUE])
    fun sitemapXml(): String =
        // Publish the deployed health endpoint as the single discoverable URL
        // so external tooling can verify that the service exists and is up.
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
            <url>
                <loc>https://se2-group-codebase.onrender.com/actuator/health</loc>
            </url>
        </urlset>
        """.trimIndent()
}
