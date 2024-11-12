package com.github.jyc228.musinsa.domain.product

import com.github.jyc228.musinsa.IntegrationTest
import io.kotest.matchers.longs.shouldBeGreaterThan
import org.junit.jupiter.api.Test

class ProductControllerTest : IntegrationTest() {
    val brandId by lazy { client.createBrand("test") }

    @Test
    fun `product 를 등록할 수 있다`() {
        client.createProduct(brandId, 1, 1000.toBigInteger()) shouldBeGreaterThan 0
    }
}
