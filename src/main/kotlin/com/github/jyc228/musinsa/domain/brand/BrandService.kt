package com.github.jyc228.musinsa.domain.brand

import com.github.jyc228.musinsa.BrandNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class BrandService(
    private val repository: BrandRepository
) {
    fun createBrand(request: BrandController.UpsertBrandRequest): BrandEntity {
        return repository.save(BrandEntity(name = request.name))
    }

    fun updateBrand(request: BrandController.UpsertBrandRequest, id: Long) {
        if (repository.updateNameById(request.name, id) == 0) throw BrandNotFoundException(id)
    }

    fun deleteBrand(id: Long) {
        if (repository.removeById(id) == 0) throw BrandNotFoundException(id)
    }

    fun throwIfNotExist(id: Long) {
        if (!repository.existsById(id)) throw BrandNotFoundException(id)
    }

    fun findAllById(id: Set<Long>): List<BrandEntity> = repository.findAllById(id)

    fun addProductCount(id: Long, amount: Int) {
        if (repository.addProductCount(id, amount) == 0) throw BrandNotFoundException(id)
    }

    fun findByIdOrNull(id: Long) = repository.findByIdOrNull(id)
}