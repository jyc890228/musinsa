package com.github.jyc228.musinsa.domain.statistics

import com.github.jyc228.musinsa.domain.category.CategoryService
import com.github.jyc228.musinsa.domain.product.ProductEntity
import com.github.jyc228.musinsa.domain.product.ProductEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class CategoryStatisticsService(
    private val database: StatisticsDatabase,
    private val categoryService: CategoryService,
) {
    private var cheaperProductByCategoryId = mutableMapOf<Long, ProductEntity>()

    @Scheduled(initialDelay = 0, fixedDelay = 1000 * 60)
    protected fun update() {
        cheaperProductByCategoryId = categoryService.getAllIds()
            .mapNotNull { database.findCheaperProductByCategoryId(it) }
            .associateBy { it.categoryId }
            .toMutableMap()
    }

    @EventListener(ProductEvent::class)
    protected fun listen(e: ProductEvent) {
        when (e) {
            is ProductEvent.Created -> updateCategoryMinPriceProduct(e.entity)
            is ProductEvent.Updated -> handleUpdated(e.next)
            is ProductEvent.Deleted -> updateCategoryMinPriceProduct(
                cheaperProductByCategoryId.values.find { it.id == e.pid }?.categoryId ?: return
            )
        }
    }

    private fun handleUpdated(next: ProductEntity) {
        cheaperProductByCategoryId[next.categoryId]?.takeIf { it.categoryId == next.categoryId }?.let { prev ->
            // 가격만 수정한 케이스
            if (prev.price < next.price) updateCategoryMinPriceProduct(prev.categoryId)
            else cheaperProductByCategoryId[next.categoryId] = next
            return@handleUpdated
        }
        val prev = cheaperProductByCategoryId.values.find { it.id == next.id }
            ?: return updateCategoryMinPriceProduct(next) // 이전에 가장 싼 상품이 아님.

        // 기존에 싼 가격으로 등록된 상품의 카테고리가 변경됨.
        updateCategoryMinPriceProduct(prev.categoryId) // 기존 카테고리 가장 싼 상품 갱신
        updateCategoryMinPriceProduct(next)            // 업데이트된 카테고리 가장 싼 상품 갱신
    }

    private fun updateCategoryMinPriceProduct(categoryId: Long) {
        when (val product = database.findCheaperProductByCategoryId(categoryId)) {
            null -> cheaperProductByCategoryId -= categoryId // 카테고리에 상품이 없음
            else -> cheaperProductByCategoryId[categoryId] = product
        }
    }

    private fun updateCategoryMinPriceProduct(next: ProductEntity) {
        val prev = cheaperProductByCategoryId[next.categoryId]
        if (prev == null || prev.price > next.price) {
            cheaperProductByCategoryId[next.categoryId] = next
        }
    }

    fun getCategoryCheaperProduct(): List<ProductEntity> {
        return cheaperProductByCategoryId.values.sortedBy { it.categoryId }
    }
}