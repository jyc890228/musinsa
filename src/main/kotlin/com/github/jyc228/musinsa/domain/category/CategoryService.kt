package com.github.jyc228.musinsa.domain.category

import com.github.jyc228.musinsa.CategoryNotFoundException
import org.springframework.stereotype.Service

@Service
class CategoryService(private val repository: CategoryRepository) {

    fun throwIfNotExist(id: Long) {
        if (!repository.existsById(id)) throw CategoryNotFoundException(id)
    }
}
