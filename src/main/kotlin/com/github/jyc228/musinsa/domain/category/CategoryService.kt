package com.github.jyc228.musinsa.domain.category

import com.github.jyc228.musinsa.CategoryNotFoundException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class CategoryService(private val repository: CategoryRepository) {

    private var categoryById: Map<Long, CategoryEntity> = emptyMap()

    @Scheduled(initialDelay = 0, fixedDelay = 1000 * 60 * 60)
    private fun update() {
        categoryById = repository.findAll().associateBy { it.id }
    }

    fun throwIfNotExist(id: Long) {
        if (id !in categoryById) throw CategoryNotFoundException(id)
    }

    fun getByIdOrNull(id: Long): CategoryEntity? = categoryById[id]
    fun getAllIds(): Set<Long> = categoryById.keys
}
