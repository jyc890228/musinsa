package com.github.jyc228.musinsa.domain.statistics

import com.github.jyc228.musinsa.domain.product.ProductEntity
import com.github.jyc228.musinsa.domain.product.ProductEvent
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import kotlin.random.Random
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock

class CategoryStatisticsServiceTest {
    private val database: StatisticsDatabase = mock()

    private val service = TestCategoryStatisticsService()
    private val minPriceProducts = mutableMapOf<Int, ProductEntity>()

    @BeforeEach
    fun setup() {
        mockProduct(categoryId = 1, min = 100, max = 1000)
        mockProduct(categoryId = 2, min = 200, max = 2000)
        mockProduct(categoryId = 3, min = 300, max = 3000)
        mockProduct(categoryId = 4, min = 400, max = 4000)
        mockProduct(categoryId = 5, min = 500, max = 5000)
        service.fireUpdate()
    }

    @Test
    fun `새롭게 등록된 상품이 기존 min, max 가격 범위 밖이면 갱신`() {
        val newMin = product(categoryId = 3, price = 100)
        val newMax = product(categoryId = 3, price = 20000000)

        service.fireCreatedEvent(newMin)
        service.getLowestPriceCategoryProduct() shouldContainAll minPriceProducts.toExpected(newMin)

        service.fireCreatedEvent(newMax)
        service.getCategoryMinMaxProduct(3)?.toPricePair() shouldBe (100 to 20000000)
    }

    @Test
    fun `새롭게 등록된 상품이 기존 min, max 가격 범위 안에 있으면 갱신 안함`() {
        val new = product(categoryId = 3, price = 1000)

        service.fireCreatedEvent(new)

        service.getLowestPriceCategoryProduct() shouldContainAll minPriceProducts.toExpected()
        service.getCategoryMinMaxProduct(3)?.toPricePair() shouldBe (300 to 3000)
    }

    @Test
    fun `제일싼 상품의 가격을 낮춘 경우 갱신`() {
        val prev = minPriceProducts.getByCategoryId(2)
        val next = prev.copy(price = 100.toBigInteger())

        service.fireUpdatedEvent(prev, next)

        service.getLowestPriceCategoryProduct() shouldContainAll minPriceProducts.toExpected(next)
    }

    @Test
    fun `제일싼 상품의 가격을 올린 경우 db 데이터로 갱신`() {
        val dbData = product(4, 420)
        given(database.findLowestPriceProductByCategoryId(dbData.categoryId)).willReturn(dbData)

        val prev = minPriceProducts.getByCategoryId(4)
        val next = prev.copy(price = 430.toBigInteger())
        service.fireUpdatedEvent(prev, next)

        service.getLowestPriceCategoryProduct() shouldContainAll minPriceProducts.toExpected(dbData)
    }

    @Test
    fun `제일싼 상품의 가격을 제일 비싸게 갱신한 경우`() {
        val dbData = product(5, 720)
        given(database.findLowestPriceProductByCategoryId(dbData.categoryId)).willReturn(dbData)

        val prev = minPriceProducts.getByCategoryId(5)
        val next = prev.copy(price = 6000.toBigInteger())
        service.fireUpdatedEvent(prev, next)

        service.getCategoryMinMaxProduct(5)?.toPricePair() shouldBe (720 to 6000)
    }

    @Test
    fun `카테고리 변경하면 기존 카테고리는 db 데이터로 갱신`() {
        val dbData = product(1, 150)
        given(database.findLowestPriceProductByCategoryId(dbData.categoryId)).willReturn(dbData)

        val prev = minPriceProducts.getByCategoryId(1)
        val next = prev.copy(categoryId = 7)
        service.fireUpdatedEvent(prev, next)

        service.getLowestPriceCategoryProduct() shouldContainAll minPriceProducts.toExpected(dbData, next)
    }

    @Test
    fun `상품 삭제하면 해당 카테고리 db 데이터로 갱신`() {
        val dbData = product(2, 270)
        given(database.findLowestPriceProductByCategoryId(dbData.categoryId)).willReturn(dbData)

        service.fireDeletedEvent(minPriceProducts.getByCategoryId(2))

        service.getLowestPriceCategoryProduct() shouldContainAll minPriceProducts.toExpected(dbData)
    }

    @Test
    fun `상품 삭제했는데 db 에서 조회된 싼 상품이 없으면 삭제`() {
        given(database.findLowestPriceProductByCategoryId(2)).willReturn(null)

        service.fireDeletedEvent(minPriceProducts.getByCategoryId(2))

        val expected = minPriceProducts.toExpected().apply { removeIf { it.categoryId == 2 } shouldBe true }
        service.getLowestPriceCategoryProduct() shouldContainAll expected
    }

    @Test
    fun `상품이 없는 카테고리에서 이벤트 발생`() {
        val new = product(8, 10)

        service.fireCreatedEvent(new)
        service.getCategoryMinMaxProduct(8)?.toPricePair() shouldBe (10 to 10)

        // 카테고리에 상품이 한개 있는 상태에서 가격 수정 -> min, max 같이 갱신
        val nextPriceUp = new.copy(price = 20.toBigInteger())
        given(database.findLowestPriceProductByCategoryId(8)).willReturn(nextPriceUp)
        service.fireUpdatedEvent(new, nextPriceUp)
        service.getCategoryMinMaxProduct(8)?.toPricePair() shouldBe (20 to 20)

        // 카테고리에 상품이 한개 있는 상태에서 가격 수정 -> min, max 같이 갱신
        val nextPriceDown = new.copy(price = 15.toBigInteger())
        given(database.findHighestPriceProductByCategoryId(8)).willReturn(nextPriceDown)
        service.fireUpdatedEvent(nextPriceUp, nextPriceDown)
        service.getCategoryMinMaxProduct(8)?.toPricePair() shouldBe (15 to 15)

        given(database.findLowestPriceProductByCategoryId(8)).willReturn(null)
        service.fireDeletedEvent(nextPriceDown)
        service.getCategoryMinMaxProduct(8)?.toPricePair() shouldBe null
        service.getLowestPriceCategoryProduct().find { it.categoryId == 8 } shouldBe null

        service.fireCreatedEvent(new)
        service.getCategoryMinMaxProduct(8)?.toPricePair() shouldBe (10 to 10)
    }

    private fun Map<Int, ProductEntity>.getByCategoryId(cid: Int): ProductEntity = values.first { it.categoryId == cid }

    private fun Map<Int, ProductEntity>.toExpected(vararg products: ProductEntity): MutableList<ProductEntity> {
        val copy = toMutableMap()
        products.forEach { copy[it.categoryId] = it }
        return copy.values.sortedBy { it.categoryId }.toMutableList()
    }

    private fun mockProduct(categoryId: Int, min: Int?, max: Int?) {
        min?.let {
            val product = product(categoryId, it)
            given(database.findLowestPriceProductByCategoryId(categoryId)).willReturn(product)
            minPriceProducts[categoryId] = product
        }
        max?.let {
            val product = product(categoryId, it)
            given(database.findHighestPriceProductByCategoryId(categoryId)).willReturn(product)
        }
    }

    private fun product(categoryId: Int, price: Int) =
        ProductEntity(Random.nextLong(), Random.nextLong(), categoryId, price.toBigInteger())

    private fun Pair<ProductEntity, ProductEntity>.toPricePair() = Pair(first.price.toInt(), second.price.toInt())

    inner class TestCategoryStatisticsService : CategoryStatisticsService(database) {
        fun fireUpdate() = super.update()
        fun fireCreatedEvent(product: ProductEntity) = super.listen(ProductEvent.Created(product))
        fun fireUpdatedEvent(prev: ProductEntity, next: ProductEntity) = super.listen(ProductEvent.Updated(prev, next))
        fun fireDeletedEvent(product: ProductEntity) = super.listen(ProductEvent.Deleted(product))
    }
}