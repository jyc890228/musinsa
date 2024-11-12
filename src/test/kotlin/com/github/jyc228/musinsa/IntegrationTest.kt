package com.github.jyc228.musinsa

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.jackson.jackson
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class IntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    val client by lazy {
        HttpClient {
            defaultRequest {
                url("http://localhost:${this@IntegrationTest.port}")
                contentType(ContentType.Application.Json)
            }
            install(ContentNegotiation) {
                jackson { }
            }
        }
    }
}
