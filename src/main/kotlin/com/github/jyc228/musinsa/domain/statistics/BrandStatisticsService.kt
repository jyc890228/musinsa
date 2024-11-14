package com.github.jyc228.musinsa.domain.statistics

import com.github.jyc228.musinsa.domain.product.ProductEntity
import com.github.jyc228.musinsa.domain.product.ProductEvent
import com.github.jyc228.musinsa.domain.product.ProductService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class BrandStatisticsService(
    private val database: StatisticsDatabase,
    private val productService: ProductService
) {
    private var cheaperBrandProducts = mutableMapOf<Long, ProductEntity>()

    fun getCheaperBrandProduct(): Pair<Long, List<ProductEntity>>? {
        val brandId = cheaperBrandProducts.values.firstOrNull()?.brandId ?: return null
        return brandId to cheaperBrandProducts.values.toList()
    }

    @Scheduled(initialDelay = 0, fixedDelay = 1000 * 60)
    protected fun update() {
        val brandId = database.findCheaperBrandId() ?: return cheaperBrandProducts.clear()
        cheaperBrandProducts = productService.findAllProductsByBrandId(brandId).associateBy { it.id }.toMutableMap()
    }

    @EventListener(ProductEvent::class)
    protected fun listen(e: ProductEvent) {
        when (e) {
            is ProductEvent.Created -> handleCreated(e.entity)
            is ProductEvent.Updated -> handleUpdated(e.prev, e.next)
            is ProductEvent.Deleted -> update()
        }
    }

    /**
     * 신규 상품 등록시, 브랜드에 모든 카테고리 상품이 등록된 경우에 한해서 가격 비교 및 갱신
     */
    private fun handleCreated(newProduct: ProductEntity) {
        val products = productService.findAllProductsByBrandId(newProduct.brandId)
        if (cheaperBrandProducts.isEmpty() && products.size == 8) {
            cheaperBrandProducts = products.associateBy { it.id }.toMutableMap()
            return
        }
        if (products.size != cheaperBrandProducts.size) return
        if (products.sumOf { it.price } < cheaperBrandProducts.values.sumOf { it.price }) {
            cheaperBrandProducts = products.associateBy { it.id }.toMutableMap()
        }
    }

    private fun handleUpdated(prev: ProductEntity, next: ProductEntity) {
        if (prev.brandId != next.brandId) {
            // 기존 브랜드는 모든 카테고리에 상품이 없게됨
            // 변경된 브랜드는 모든 카테고리에 상품이 있는지 없는지 모르지만 제일싼 브랜드는 아님. 갱신
            return update()
        }
        if (prev.categoryId != next.categoryId) {
            return // 브랜드는 동일한 상태에서 카테고리 변경됨. 모든 카테고리에 상품이 없다는 의미이므로 갱신 안함
        }
        val cheaperProduct = cheaperBrandProducts[next.id]
        if (cheaperProduct == null) {
            if (prev.price < next.price) return // 기존 싼 상품 아닌데 가격 올림. 갱신 안함
            return update()
        }
        if (cheaperProduct.price < next.price) {
            return update() // 기존 싼 상품 가격 상승. 갱신
        }
        cheaperBrandProducts[next.id] = next // 기존 싼 상품 가격 하락
    }
}