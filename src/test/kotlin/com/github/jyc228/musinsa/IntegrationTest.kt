package com.github.jyc228.musinsa

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

    val client by lazy { MusinsaApiClient("http://localhost:${port}") }
    val categoryIds = (1L..8L).toList()
}
