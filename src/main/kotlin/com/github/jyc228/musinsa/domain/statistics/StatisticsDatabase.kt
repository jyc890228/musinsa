package com.github.jyc228.musinsa.domain.statistics

import com.github.jyc228.musinsa.domain.product.ProductEntity
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service

@Service
class StatisticsDatabase(private val em: EntityManager) {
    fun findCheaperProductByCategoryId(categoryId: Long): ProductEntity? {
        return em.createQuery(
            "FROM ProductEntity p WHERE p.categoryId = :categoryId ORDER BY p.price LIMIT 1",
            ProductEntity::class.java
        ).setParameter("categoryId", categoryId).resultList.getOrNull(0)
    }
}