package com.hamza

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.htmlunit.WebClient
import org.htmlunit.html.HtmlPage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
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
}
