package com.github.jyc228.musinsa.domain.statistics

import com.github.jyc228.musinsa.IntegrationTest
import com.github.jyc228.musinsa.domain.category.Category
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlin.random.Random
import org.junit.jupiter.api.Test

class StatisticsControllerTest : IntegrationTest() {
    @Test
    fun `카테고리별 제일 싼 상품을 찾을 수 있다`() {
        val brandIds = listOf("A", "B", "C").map { client.createBrand(it) }.toSet()
        brandIds
            .flatMap { b -> Category.allIds.map { c -> b to c } }
            .map { Triple(it.first, it.second, Random.nextInt(1000, 100000).toBigInteger()) }
            .forEach { (brandId, categoryId, price) -> client.createProduct(brandId, categoryId, price) }

        val result = client.getCategoryCheaperProduct()
        result.data shouldHaveSize 8
        result.totalPrice shouldBeGreaterThan 0.toBigInteger()
    }

    @Test
    fun `가장싼 브랜드 상품을 찾을 수 있다`() {
        listOf(
            "AA" to (10..100),
            "BB" to (1000..10000),
            "CC" to (1000..10000)
        ).forEach { (name, priceRange) ->
            val brandId = client.createBrand(name)
            Category.allIds.forEach { client.createProduct(brandId, it, priceRange.random().toBigInteger()) }
        }

        val result = client.getBrandCheaperProduct()
        result.lowestPrice.brand shouldBe "AA"
    }

    @Test
    fun `카테고리 이름으로 최소 가격, 최대 가격 상품을 찾을 수 있다`() {
        listOf(
            "DD" to (10..100),
            "EE" to (1000..10000),
            "FF" to (1000..10000)
        ).forEach { (name, priceRange) ->
            val brandId = client.createBrand(name)
            Category.allIds.forEach { client.createProduct(brandId, it, priceRange.random().toBigInteger()) }
        }

        val result = client.getCategoryProduct("상의")
        result.category shouldBe "상의"
        result.lowestPrice.price shouldBeGreaterThan 0.toBigInteger()
        result.lowestPrice.price shouldBeLessThan result.highestPrice.price
    }
}