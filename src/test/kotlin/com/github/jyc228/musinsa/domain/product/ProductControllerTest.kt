package com.github.jyc228.musinsa.domain.product

import com.github.jyc228.musinsa.IntegrationTest
import com.github.jyc228.musinsa.MusinsaApiClient
import com.github.jyc228.musinsa.domain.category.Category
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ProductControllerTest : IntegrationTest() {
    val brandId by lazy { client.createBrand("test") }

    @Test
    fun `product 를 등록할 수 있다`() {
        client.createProduct(brandId, Category.allIds[0], 1000.toBigInteger()) shouldBeGreaterThan 0
    }

    @Test
    fun `product 를 수정할 수 있다`() {
        val pid = client.createProduct(brandId, Category.allIds[2], 1000.toBigInteger())

        shouldNotThrowAny { client.updateProduct(pid, brandId, Category.allIds[2], 2000.toBigInteger()) }
        shouldNotThrowAny { client.updateProduct(pid, brandId, Category.allIds[3], 2000.toBigInteger()) }

        shouldThrow<MusinsaApiClient.ResponseException> {
            client.updateProduct(pid + 394, brandId, 2, 2000.toBigInteger())
        }.statusCode shouldBe 404
        shouldThrow<MusinsaApiClient.ResponseException> {
            client.updateProduct(pid, brandId, Category.allIds[2], 0.toBigInteger())
        }.statusCode shouldBe 400
    }

    @Test
    fun `product 를 삭제할 수 있다`() {
        val pid = client.createProduct(brandId, Category.allIds[5], 1000.toBigInteger())

        shouldNotThrowAny { client.deleteProduct(pid) }
        shouldThrow<MusinsaApiClient.ResponseException> {
            client.deleteProduct(pid + 193)
        }.statusCode shouldBe 404
    }
}
