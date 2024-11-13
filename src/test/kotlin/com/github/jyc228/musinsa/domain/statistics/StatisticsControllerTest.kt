package com.github.jyc228.musinsa.domain.statistics

import com.github.jyc228.musinsa.IntegrationTest
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kotlin.random.Random
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class StatisticsControllerTest : IntegrationTest() {
    @Test
    fun `카테고리별 제일 싼 상품을 찾을 수 있다`() {
        val brandIds = listOf("A", "B", "C").map { client.createBrand(it) }.toSet()
        brandIds
            .flatMap { b -> categoryIds.map { c -> b to c } }
            .map { Triple(it.first, it.second, Random.nextInt(1000, 100000).toBigInteger()) }
            .forEach { (brandId, categoryId, price) -> client.createProduct(brandId, categoryId, price) }

        val result = client.getCategoryCheaperProduct()
        result.data shouldHaveSize 8
        result.totalPrice shouldBeGreaterThan 0.toBigInteger()
    }
}