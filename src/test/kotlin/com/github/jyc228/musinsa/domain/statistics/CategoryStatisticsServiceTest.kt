package com.github.jyc228.musinsa.domain.statistics

import com.github.jyc228.musinsa.domain.product.ProductEntity
import com.github.jyc228.musinsa.domain.product.ProductEvent
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.random.Random
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock

class CategoryStatisticsServiceTest {
    private val database: StatisticsDatabase = mock()

    private val service = TestCategoryStatisticsService()
    private val products = listOf(
        product(1, 1, 100),
        product(1, 2, 200),
        product(1, 3, 300),
        product(1, 4, 400),
        product(1, 5, 500),
    )

    @BeforeEach
    fun setup() {
        prepareTest()
    }

    @Test
    fun `새롭게 등록된 상품이 기존 min, max 가격 범위 밖이면 갱신`() {
        service.fireCreatedEvent(product(6, 3, 100))

        service.getCategoryCheaperProduct().groupBy { it.categoryId }.let {
            it shouldHaveSize 5
            it[3]?.single()?.price shouldBe 100.toBigInteger()
            it[5]?.single()?.price shouldBe 500.toBigInteger()
        }

        service.fireCreatedEvent(product(6, 3, 20000000))

        service.getCategoryMinMaxProduct(3)?.toPricePair() shouldBe (100.toBigInteger() to 20000000.toBigInteger())
    }

    @Test
    fun `새롭게 등록된 상품이 기존 min, max 가격 범위 안에 있으면 갱신 안함`() {
        service.fireCreatedEvent(product(6, 3, 1000))

        val result = service.getCategoryCheaperProduct().groupBy { it.categoryId }
        result shouldHaveSize 5
        result[3]?.single()?.price shouldBe 300.toBigInteger()
        service.getCategoryMinMaxProduct(3)?.toPricePair() shouldBe (300.toBigInteger() to (300 * 100).toBigInteger())
    }

    @Test
    fun `제일싼 상품의 가격을 낮춘 경우 갱신`() {
        val product = products.random()

        service.fireUpdatedEvent(product, product.copy(price = product.price / 2.toBigInteger()))
        val result = service.getCategoryCheaperProduct().groupBy { it.categoryId }

        result shouldHaveSize 5
        result[product.categoryId]?.single()?.price shouldBe product.price / 2.toBigInteger()
    }

    @Test
    fun `제일싼 상품의 가격을 올린 경우 db 데이터로 갱신`() {
        val product = products.random()
        val dbProduct = product(2, product.categoryId, product.price.toInt() + 100)
        given(database.findLowestPriceProductByCategoryId(product.categoryId)).willReturn(dbProduct)

        service.fireUpdatedEvent(product, product.copy(price = product.price + 10000.toBigInteger()))
        val result = service.getCategoryCheaperProduct().groupBy { it.categoryId }

        result shouldHaveSize 5
        result[product.categoryId]?.single().let {
            it?.id shouldBe dbProduct.id
            it?.price shouldBe dbProduct.price
        }
    }

    @Test
    fun `제일싼 상품의 가격을 제일 비싸게 갱신한 경우`() {
        val product = products.random()
        given(database.findLowestPriceProductByCategoryId(product.categoryId))
            .willReturn(product.copy(price = product.price + 100.toBigInteger()))

        service.fireUpdatedEvent(product, product.copy(price = product.price + 10000000.toBigInteger()))
        val result = service.getCategoryMinMaxProduct(product.categoryId)

        result?.toPricePair() shouldBe (product.price + 100.toBigInteger() to product.price + 10000000.toBigInteger())
    }

    @Test
    fun `카테고리 변경하면 기존 카테고리는 db 데이터로 갱신`() {
        val newCategory = 7
        val product = products.random()
        val dbProduct = product(2, product.categoryId, product.price.toInt() + 100)
        given(database.findLowestPriceProductByCategoryId(product.categoryId)).willReturn(dbProduct)

        service.fireUpdatedEvent(product, product.copy(categoryId = newCategory))
        val result = service.getCategoryCheaperProduct().groupBy { it.categoryId }

        result shouldHaveSize 6
        result[product.categoryId]?.single().let {
            it?.id shouldBe dbProduct.id
            it?.price shouldBe dbProduct.price
        }
        result[newCategory]?.single().let {
            it?.id shouldBe product.id
            it?.price shouldBe product.price
        }
    }

    @Test
    fun `상품 삭제하면 해당 카테고리 db 데이터로 갱신`() {
        val product = products.random()
        val dbProduct = product(2, product.categoryId, product.price.toInt() + 100)
        given(database.findLowestPriceProductByCategoryId(product.categoryId)).willReturn(dbProduct)

        service.fireDeletedEvent(product)
        val result = service.getCategoryCheaperProduct().groupBy { it.categoryId }

        result shouldHaveSize 5
        result[product.categoryId]?.single().let {
            it?.id shouldBe dbProduct.id
            it?.price shouldBe dbProduct.price
        }
    }

    @Test
    fun `상품 삭제했는데 db 에서 조회된 싼 상품이 없으면 삭제`() {
        val product = products.random()
        given(database.findLowestPriceProductByCategoryId(product.categoryId)).willReturn(null)

        service.fireDeletedEvent(product)
        val result = service.getCategoryCheaperProduct().groupBy { it.categoryId }

        result shouldHaveSize 4
        result[product.categoryId] shouldBe null
    }

    private fun prepareTest() {
        products.onEach {
            given(database.findLowestPriceProductByCategoryId(it.categoryId)).willReturn(it)
            given(database.findHighestPriceProductByCategoryId(it.categoryId)).willReturn(it.copy(price = it.price * 100.toBigInteger()))
        }
        service.fireUpdate()
    }

    private fun product(brandId: Long, categoryId: Int, price: Int) =
        ProductEntity(Random.nextLong(), brandId, categoryId, price.toBigInteger())

    private fun Pair<ProductEntity, ProductEntity>.toPricePair() = Pair(first.price, second.price)

    inner class TestCategoryStatisticsService : CategoryStatisticsService(database) {
        fun fireUpdate() = super.update()
        fun fireCreatedEvent(product: ProductEntity) = super.listen(ProductEvent.Created(product))
        fun fireUpdatedEvent(prev: ProductEntity, next: ProductEntity) = super.listen(ProductEvent.Updated(prev, next))
        fun fireDeletedEvent(product: ProductEntity) = super.listen(ProductEvent.Deleted(product))
    }
}