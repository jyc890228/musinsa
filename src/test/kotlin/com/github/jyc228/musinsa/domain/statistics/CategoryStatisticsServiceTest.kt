package com.github.jyc228.musinsa.domain.statistics

import com.github.jyc228.musinsa.domain.category.CategoryService
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
    private val categoryService: CategoryService = mock()

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
    fun `새롭게 등록된 상품이 기존 제일싼 상품보다 가격이 싸면 갱신`() {
        service.fireCreatedEvent(product(6, 3, 100))

        val result = service.getCategoryCheaperProduct().groupBy { it.categoryId }
        result shouldHaveSize 5
        result[3]?.single()?.price shouldBe 100.toBigInteger()
        result[5]?.single()?.price shouldBe 500.toBigInteger()
    }

    @Test
    fun `새롭게 등록된 상품이 기존 제일싼 상품보다 가격이 비싸면 갱신 안함`() {
        service.fireCreatedEvent(product(6, 3, 1000))

        val result = service.getCategoryCheaperProduct().groupBy { it.categoryId }
        result shouldHaveSize 5
        result[3]?.single()?.price shouldBe 300.toBigInteger()
    }

    @Test
    fun `제일싼 상품의 가격을 낮춘 경우 갱신`() {
        val product = products.random()

        service.fireUpdatedEvent(product.copy(price = product.price.toInt() / 2))
        val result = service.getCategoryCheaperProduct().groupBy { it.categoryId }

        result shouldHaveSize 5
        result[product.categoryId]?.single()?.price shouldBe product.price / 2.toBigInteger()
    }

    @Test
    fun `제일싼 상품의 가격을 올린 경우 db 데이터로 갱신`() {
        val product = products.random()
        val dbProduct = product(2, product.categoryId, product.price.toInt() + 100)
        given(database.findCheaperProductByCategoryId(product.categoryId)).willReturn(dbProduct)

        service.fireUpdatedEvent(product.copy(price = product.price.toInt() + 10000))
        val result = service.getCategoryCheaperProduct().groupBy { it.categoryId }

        result shouldHaveSize 5
        result[product.categoryId]?.single().let {
            it?.id shouldBe dbProduct.id
            it?.price shouldBe dbProduct.price
        }
    }

    @Test
    fun `카테고리 변경하면 기존 카테고리는 db 데이터로 갱신`() {
        val newCategory = 7L
        val product = products.random()
        val dbProduct = product(2, product.categoryId, product.price.toInt() + 100)
        given(database.findCheaperProductByCategoryId(product.categoryId)).willReturn(dbProduct)

        service.fireUpdatedEvent(product.copy(categoryId = newCategory))
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
        given(database.findCheaperProductByCategoryId(product.categoryId)).willReturn(dbProduct)

        service.fireDeletedEvent(product.id)
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
        given(database.findCheaperProductByCategoryId(product.categoryId)).willReturn(null)

        service.fireDeletedEvent(product.id)
        val result = service.getCategoryCheaperProduct().groupBy { it.categoryId }

        result shouldHaveSize 4
        result[product.categoryId] shouldBe null
    }

    private fun prepareTest() {
        given(categoryService.getAllIds()).willReturn(setOf(1, 2, 3, 4, 5))
        products.onEach { given(database.findCheaperProductByCategoryId(it.categoryId)).willReturn(it) }
        service.fireUpdate()
    }

    private fun product(brandId: Long, categoryId: Long, price: Int) =
        ProductEntity(Random.nextLong(), brandId, categoryId, price.toBigInteger())

    private fun ProductEntity.copy(categoryId: Long? = null, price: Int? = null) =
        ProductEntity(id, brandId, categoryId ?: this.categoryId, price?.toBigInteger() ?: this.price)

    inner class TestCategoryStatisticsService : CategoryStatisticsService(database, categoryService) {
        fun fireUpdate() = super.update()
        fun fireCreatedEvent(product: ProductEntity) = super.listen(ProductEvent.Created(product))
        fun fireUpdatedEvent(product: ProductEntity) = super.listen(ProductEvent.Updated(product))
        fun fireDeletedEvent(pid: Long) = super.listen(ProductEvent.Deleted(pid))
    }
}