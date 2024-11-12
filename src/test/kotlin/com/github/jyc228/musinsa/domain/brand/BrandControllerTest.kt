package com.github.jyc228.musinsa.domain.brand

import com.github.jyc228.musinsa.IntegrationTest
import com.github.jyc228.musinsa.MusinsaApiClient
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.longs.shouldBeGreaterThan
import org.junit.jupiter.api.Test

class BrandControllerTest : IntegrationTest() {

    @Test
    fun `brand 를 등록할 수 있다`() {
        client.createBrand("test") shouldBeGreaterThan 0
    }

    @Test
    fun `brand 를 수정할 수 있다`() {
        val newBrandId = client.createBrand("test2")
        shouldNotThrowAny { client.updateBrand(newBrandId, "test2 updated") }
        shouldThrow<MusinsaApiClient.ResponseException> {
            client.updateBrand(newBrandId + 9999, "not exist brand updated")
        }
    }
}
