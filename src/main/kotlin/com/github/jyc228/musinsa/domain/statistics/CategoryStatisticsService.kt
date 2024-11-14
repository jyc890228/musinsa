package com.github.jyc228.musinsa.domain.statistics

import com.github.jyc228.musinsa.domain.category.Category
import com.github.jyc228.musinsa.domain.product.ProductEntity
import com.github.jyc228.musinsa.domain.product.ProductEvent
import com.github.jyc228.musinsa.domain.statistics.CategoryStatisticsService.Price
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

/**
 * 카테고리 단위로 최소, 최대 가격을 수집
 *
 * category id 와 price 에 인덱스를 적용했고 카테고리는 8개 고정이므로 이벤트 처리중일땐 쿼리 최대 2번 [Price.refreshIfBoundary], 갱신 주기때는 최대 16번 실행 [update].
 *
 * native 한방 쿼리 복잡하게 만드는것보단 작고 코스트 낮은 쿼리 여러번 돌리는게 더 우위에 있다고 판단했습니다.
 */
@Service
class CategoryStatisticsService(
    private val database: StatisticsDatabase,
) {
    private var priceByCid = mutableMapOf<Int, Price>()

    fun getCategoryMinMaxProduct(categoryId: Int): Pair<ProductEntity, ProductEntity>? {
        val price = priceByCid[categoryId] ?: return null
        return Pair(price.minEntity ?: return null, price.maxEntity ?: return null)
    }

    fun getCategoryCheaperProduct(): List<ProductEntity> {
        return priceByCid.values.mapNotNull { it.minEntity }.sortedBy { it.categoryId }
    }

    @Scheduled(initialDelay = 0, fixedDelay = 1000 * 60)
    protected fun update() {
        priceByCid = Category.allIds
            .mapNotNull {
                val min = database.findLowestPriceProductByCategoryId(it) ?: return@mapNotNull null
                val max = database.findHighestPriceProductByCategoryId(it)
                it to Price(min, max ?: min)
            }
            .associateBy({ it.first }, { it.second })
            .toMutableMap()
    }

    @EventListener(ProductEvent::class)
    protected fun listen(e: ProductEvent) {
        when (e) {
            is ProductEvent.Created -> priceByCid.getOrPut(e.entity.categoryId) { Price(e.entity) }.update(e.entity)
            is ProductEvent.Updated -> handleUpdated(e.prev, e.next)
            is ProductEvent.Deleted -> priceByCid[e.entity.categoryId]?.refreshIfBoundary(e.entity, database)
        }
    }

    private fun handleUpdated(prev: ProductEntity, next: ProductEntity) {
        // 카테고리가 바뀌는 경우
        // 기존 카테고리에서 min, max 인 경우 -> db 데이터로 기존 카테고리 min, max 갱신
        // 기존 카테고리에서 min, max 가 아닌 경우 -> 아무것도 안함
        // 신규 카테고리에서 min, max 안에 들어온 경우 -> 아무것도 안함
        // 신규 카테고리에서 min, max 를 초과한 경우 -> event 데이터로 신규 min, max 갱신
        if (prev.categoryId != next.categoryId) {
            priceByCid[prev.categoryId]?.refreshIfBoundary(prev, database)
            priceByCid.getOrPut(next.categoryId) { Price(next) }.update(next)
            return
        }

        // 가격만 바뀌는 경우
        // 변경후 가격이 min, max 범위를 넘은 경우 -> 갱신
        // 변경전 가격이 min, max -> db 데이터로 min, max 갱신
        if (prev.price != next.price) {
            val price = priceByCid.getOrPut(next.categoryId) { Price(next) }
            price.update(next)
            price.refreshIfBoundary(prev, database)
            return
        }
    }

    private data class Price(var minEntity: ProductEntity?, var maxEntity: ProductEntity?) {
        constructor(e: ProductEntity) : this(e, e)

        val min get() = minEntity?.price
        val max get() = maxEntity?.price

        /** [product] 가격이 [min]..[max] 범위 밖에 있으면 갱신한다. */
        fun update(product: ProductEntity) {
            when {
                min == null || product.price < min -> minEntity = product
                max == null || max!! < product.price -> maxEntity = product
            }
        }

        /** [product] 가격이 [min], [max] 와 동일한 경우, db 값으로 갱신한다. */
        fun refreshIfBoundary(product: ProductEntity, database: StatisticsDatabase) {
            when (product.price) {
                min -> minEntity = database.findLowestPriceProductByCategoryId(product.categoryId)
                max -> maxEntity = database.findHighestPriceProductByCategoryId(product.categoryId)
            }
        }
    }
}