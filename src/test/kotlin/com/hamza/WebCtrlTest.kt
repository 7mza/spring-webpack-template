package com.hamza

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.htmlunit.WebClient
import org.htmlunit.html.HtmlLink
import org.htmlunit.html.HtmlPage
import org.htmlunit.html.HtmlScript
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebCtrlTest {
    private val htmlClient =
        WebClient().apply {
            options.isJavaScriptEnabled = true
            options.isRedirectEnabled = false
            options.isThrowExceptionOnFailingStatusCode = true
            options.isThrowExceptionOnScriptError = false
        }

    @LocalServerPort
    private lateinit var port: String

    @Autowired
    private lateinit var assetManifestReader: AssetManifestReader

    @AfterEach
    fun afterEach() {
        htmlClient.cookieManager.clearCookies()
    }

    @Test
    fun `nonce propagated on configured paths`() {
        val page: HtmlPage = htmlClient.getPage("http://localhost:$port")
        val response = page.webResponse
        assertThat(response.statusCode).isEqualTo(200)
        val nonce = response.getResponseHeaderValue("X-Nonce")
        assertThat(nonce).isNotEmpty()
        assertThatCode { UUID.fromString(nonce) }.doesNotThrowAnyException()
        val script = page.getElementsByTagName("script")
        assertThat(script.first().getAttribute("nonce")).isEqualTo(nonce)
    }

    @Test
    fun `no nonce required if path not configured`() {
        val page: HtmlPage = htmlClient.getPage("http://localhost:$port/other")
        val response = page.webResponse
        assertThat(response.statusCode).isEqualTo(200)
        val nonce = response.getResponseHeaderValue("X-Nonce")
        assertThat(nonce).isNull()
        val script = page.getElementsByTagName("script")
        assertThat(script.first().getAttribute("nonce")).isEmpty()
    }

    @Test
    fun `assetManifestReader init`() {
        assertThat(assetManifestReader.getAll().size).isEqualTo(6)
        assertThat(assetManifestReader.getAll().keys)
            .containsExactlyInAnyOrder(
                "bootstrap-icons.woff",
                "bootstrap-icons.woff2",
                "main.js",
                "shared.css",
                "shared.js",
                "vendor.js",
            )
    }

    @Test
    fun `static assets are populated from manifest`() {
        val page: HtmlPage = htmlClient.getPage("http://localhost:$port")
        assertThat(page.webResponse.statusCode).isEqualTo(HttpStatus.OK.value())
        assertThat(page.url.path).isEqualTo("/")

        val scriptTags = page.getByXPath<HtmlScript>("//script")
        val srcs = scriptTags.map { it.srcAttribute }
        assertThat(srcs)
            .containsExactlyInAnyOrder(
                assetManifestReader.get("vendor.js"),
                assetManifestReader.get("shared.js"),
                assetManifestReader.get("main.js"),
            )

        val stylesheetLinks = page.getByXPath<HtmlLink>("//link[@rel='stylesheet']")
        val hrefs = stylesheetLinks.map { it.hrefAttribute }
        assertThat(hrefs).containsExactlyInAnyOrder(assetManifestReader.get("shared.css"))
    }
}
