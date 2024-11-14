package com.github.jyc228.musinsa.domain.statistics

import com.github.jyc228.musinsa.domain.category.CategoryService
import com.github.jyc228.musinsa.domain.product.ProductEntity
import com.github.jyc228.musinsa.domain.product.ProductEvent
import com.github.jyc228.musinsa.domain.product.ProductService
import io.kotest.matchers.shouldBe
import java.math.BigInteger
import kotlin.random.Random
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.times
import org.mockito.BDDMockito.verify
import org.mockito.Mockito
import org.mockito.Mockito.*

class BrandStatisticsServiceTest {
    private val database: StatisticsDatabase = mock()
    private val productService: ProductService = mock()

    private val service = TestBrandStatisticsService()
    private val products = (1..8L).map { product(1, it, (it * 100).toInt()) }

    @BeforeEach
    fun setup() {
        prepareTest()
    }

    @Test
    fun `새로운 상품 등록시, 등록된 브랜드의 상품 개수가 8개 이하인 경우 아무것도 하지 않는다`() {
        service.fireCreatedEvent(product(2, 1, 100))

        service.getCheaperBrandProduct()?.first shouldBe 1
    }

    @Test
    fun `새로운 상품 등록시, 등록된 브랜드의 상품 개수가 8개면 갱신`() {
        val products = (1..8L).map { product(3, it, (it * 10).toInt()) }
        products.forEachIndexed { i, e ->
            given(productService.findAllProductsByBrandId(3)).willReturn(products.take(i + 1))
            if (i == 7) given(database.findCheaperBrandId()).willReturn(3)
            service.fireCreatedEvent(e)
        }

        val result = service.getCheaperBrandProduct()
        result?.first shouldBe 3
        result?.second?.totalPrice shouldBe products.totalPrice
    }

    @Test
    fun `상품 브랜드 수정시, 무조건 갱신`() {
        given(database.findCheaperBrandId()).willReturn(555)
        given(productService.findAllProductsByBrandId(555))
            .willReturn((1..8L).map { product(555, it, (it * 10).toInt()) })

        service.fireUpdatedEvent(products[1], products[1].copy(brandId = 999))
        service.getCheaperBrandProduct()?.first shouldBe 555
    }

    @Test
    fun `상품 카테고리 수정시, 아무것도 안함`() {
        service.fireUpdatedEvent(product(23, 1, 100), product(23, 4, 100))

        service.getCheaperBrandProduct()?.first shouldBe 1
    }

    @Test
    fun `저렴한 브랜드 상품이 아닌 상품 가격 수정`() {
        service.fireUpdatedEvent(
            product(23, 1, 100),
            product(23, 1, 200)
        )
        service.getCheaperBrandProduct()?.first shouldBe 1

        given(database.findCheaperBrandId()).willReturn(2)
        given(productService.findAllProductsByBrandId(2))
            .willReturn((1..8L).map { product(2, it, (it * 10).toInt()) })

        service.fireUpdatedEvent(
            product(23, 1, 200),
            product(23, 1, 50)
        )
        service.getCheaperBrandProduct()?.first shouldBe 2
    }

    @Test
    fun `저렴한 브랜드 상품 가격 수정`() {
        service.fireUpdatedEvent(
            products[0],
            products[0].copy(price = products[0].price - 10.toBigInteger())
        )
        service.getCheaperBrandProduct().let {
            it?.first shouldBe 1
            it?.second?.totalPrice shouldBe products.totalPrice - 10.toBigInteger()
        }

        given(database.findCheaperBrandId()).willReturn(99)
        given(productService.findAllProductsByBrandId(99))
            .willReturn((1..8L).map { product(99, it, (it * 10).toInt()) })
        service.fireUpdatedEvent(
            products[0],
            products[0].copy(price = products[0].price + 10.toBigInteger())
        )
        service.getCheaperBrandProduct()?.first shouldBe 99
    }

    private fun prepareTest() {
        given(database.findCheaperBrandId()).willReturn(1)
        given(productService.findAllProductsByBrandId(1)).willReturn(products)
        service.fireUpdate()
    }

    private fun product(brandId: Long, categoryId: Long, price: Int) =
        ProductEntity(Random.nextLong(), brandId, categoryId, price.toBigInteger())

    private val List<ProductEntity>.totalPrice: BigInteger get() = this.sumOf { it.price }

    inner class TestBrandStatisticsService : BrandStatisticsService(database, productService) {
        fun fireUpdate() = super.update()
        fun fireCreatedEvent(product: ProductEntity) = super.listen(ProductEvent.Created(product))
        fun fireUpdatedEvent(prev: ProductEntity, next: ProductEntity) = super.listen(ProductEvent.Updated(prev, next))
    }
}