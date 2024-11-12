package com.github.jyc228.musinsa.domain.brand

import org.springframework.stereotype.Service

@Service
class BrandService(
    private val repository: BrandRepository
) {
    fun createBrand(request: BrandController.UpsertBrandRequest): BrandEntity {
        return repository.save(BrandEntity(name = request.name))
    }
}