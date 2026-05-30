package at.se2group.backend.web

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SiteMetadataController {

    @GetMapping("/robots.txt", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun robotsTxt(): String =
        """
        User-agent: *
        Disallow:
        """.trimIndent()

    @GetMapping("/sitemap.xml", produces = [MediaType.APPLICATION_XML_VALUE])
    fun sitemapXml(): String =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
            <url>
                <loc>https://se2-group-codebase.onrender.com/actuator/health</loc>
            </url>
        </urlset>
        """.trimIndent()
}
