package com.hamza

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.UUID

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@Controller
@RequestMapping(value = ["/"], produces = [MediaType.TEXT_HTML_VALUE])
class WebCtrl
    @Autowired
    constructor(
        private val assetManifestReader: AssetManifestReader,
    ) {
        @ModelAttribute("assetManifest")
        fun injectAssetManifest(): Map<String, String> = assetManifestReader.getAll()

        @GetMapping
        fun index(model: Model) = Mono.just("index")

        @GetMapping("/other")
        fun other() = Mono.just("other")

        @GetMapping("/fragments")
        fun fragments(model: Model) = Mono.just("fragments")
    }

@Configuration
class FilterConf {
    @Bean
    fun nonceFilter(props: NonceFilterProps) = NonceFilter(props.include)

    @Configuration
    @ConfigurationProperties(prefix = "filters.nonce")
    class NonceFilterProps(
        var include: List<String>?,
    )

    class NonceFilter(
        private val include: List<String>?,
    ) : WebFilter {
        override fun filter(
            exchange: ServerWebExchange,
            chain: WebFilterChain,
        ): Mono<Void> {
            val path = exchange.request.path.value()
            if (include?.contains(path) == true) {
                val nonce = UUID.randomUUID().toString()
                // will apply model.addAttribute("nonce", nonce) on included thymeleaf views
                exchange.attributes["nonce"] = nonce
                exchange.response.headers.set("X-Nonce", nonce)
            }
            return chain.filter(exchange)
        }
    }
}

@Component
class AssetManifestReader
    @Autowired
    constructor(
        private val resourceLoader: ResourceLoader,
    ) {
        private val assetMap: Map<String, String>

        init {
            val mapper = ObjectMapper()
            val resource = resourceLoader.getResource("classpath:/static/dist/asset-manifest.json")
            resource.inputStream.use {
                assetMap = mapper.readValue(it, object : TypeReference<Map<String, String>>() {})
            }
        }

        fun get(name: String) =
            assetMap[name] ?: throw Exception("Asset $name not found in /static/dist/asset-manifest.json")

        fun getAll(): Map<String, String> = assetMap
    }
