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
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.reactive.result.view.Rendering
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.nio.charset.StandardCharsets
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
        private val assetManifestReader: ReactiveAssetManifestReader,
    ) {
        @GetMapping
        fun index(): Mono<Rendering> =
            assetManifestReader
                .getAll()
                .map {
                    Rendering
                        .view("index")
                        .modelAttribute("assetManifest", it)
                        .build()
                }

        @GetMapping("/other")
        fun other() =
            assetManifestReader
                .getAll()
                .map {
                    Rendering
                        .view("other")
                        .modelAttribute("assetManifest", it)
                        .build()
                }

        @GetMapping("/fragments")
        fun fragments() =
            assetManifestReader
                .getAll()
                .map {
                    Rendering
                        .view("fragments")
                        .modelAttribute("assetManifest", it)
                        .build()
                }
    }

@Configuration
class FilterConf {
    @Bean
    fun nonceFilter(props: NonceFilterProps) = NonceFilter(props.include)

    @Configuration
    @ConfigurationProperties(prefix = "filters.nonce")
    class NonceFilterProps {
        var include: List<String>? = null
    }

    class NonceFilter(
        private val include: List<String>?,
    ) : WebFilter {
        override fun filter(
            exchange: ServerWebExchange,
            chain: WebFilterChain,
        ): Mono<Void> {
            val path = exchange.request.path.value()
            return if (include?.contains(path) == true) {
                Mono
                    .fromCallable { UUID.randomUUID().toString() }
                    .subscribeOn(Schedulers.boundedElastic())
                    .flatMap { nonce ->
                        // will apply model.addAttribute("nonce", nonce) on included thymeleaf views
                        exchange.attributes["nonce"] = nonce
                        exchange.response.headers.set("X-Nonce", nonce)
                        chain.filter(exchange)
                    }
            } else {
                Mono.defer { chain.filter(exchange) }
                // chain.filter(exchange)
            }
        }
    }
}

@Component
class ReactiveAssetManifestReader(
    private val resourceLoader: ResourceLoader,
    private val objectMapper: ObjectMapper,
) {
    private val assetMapMono: Mono<Map<String, String>> by lazy {
        Mono
            .fromCallable {
                resourceLoader.getResource("classpath:/static/dist/asset-manifest.json")
            }.subscribeOn(Schedulers.boundedElastic())
            .flatMap { resource ->
                DataBufferUtils
                    .read(resource, DefaultDataBufferFactory(), 4096)
                    .reduce { buf1, buf2 ->
                        val combined =
                            DefaultDataBufferFactory().allocateBuffer(
                                buf1.readableByteCount() + buf2.readableByteCount(),
                            )
                        combined.write(buf1)
                        combined.write(buf2)
                        DataBufferUtils.release(buf1)
                        DataBufferUtils.release(buf2)
                        combined
                    }.map { dataBuffer ->
                        val content =
                            dataBuffer
                                .readableByteBuffers()
                                .asSequence()
                                .map { StandardCharsets.UTF_8.decode(it).toString() }
                                .joinToString("")
                        DataBufferUtils.release(dataBuffer)
                        objectMapper.readValue(content, object : TypeReference<Map<String, String>>() {})
                    }.cache()
            }
    }

    fun get(name: String): Mono<String> =
        assetMapMono.flatMap { map ->
            map[name]?.let { Mono.just(it) }
                ?: Mono.error(Exception("Asset $name not found in /static/dist/asset-manifest.json"))
        }

    fun getAll(): Mono<Map<String, String>> = assetMapMono
}
