package com.github.jyc228.musinsa.domain.product

import com.github.jyc228.musinsa.InvalidRequestException
import com.github.jyc228.musinsa.ProductNotFoundException
import com.github.jyc228.musinsa.domain.brand.BrandService
import com.github.jyc228.musinsa.domain.category.CategoryService
import java.math.BigInteger
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val repository: ProductRepository,
    private val brandService: BrandService,
    private val categoryService: CategoryService,
) {
    @Transactional
    fun createProduct(request: ProductController.UpsertProductRequest): ProductEntity {
        if (request.price <= BigInteger.ZERO) {
            throw InvalidRequestException("price", "price must be greater than 0. $request")
        }
        categoryService.throwIfNotExist(request.categoryId)
        brandService.throwIfNotExist(request.brandId)
        if (repository.existsByBrandIdAndCategoryId(request.brandId, request.categoryId)) {
            throw InvalidRequestException("brandId, categoryId", "product already exists. $request")
        }
        return repository.save(
            ProductEntity(
                brandId = request.brandId,
                categoryId = request.categoryId,
                price = request.price
            )
        )
    }

    @Transactional
    fun updateProduct(request: ProductController.UpsertProductRequest, pid: Long) {
        if (request.price <= BigInteger.ZERO) {
            throw InvalidRequestException("price", "price must be greater than 0. $request")
        }
        val e = repository.findByIdOrNull(pid) ?: throw ProductNotFoundException(pid)
        if (e.categoryId != request.categoryId || e.brandId != request.brandId) {
            if (e.categoryId != request.categoryId) categoryService.throwIfNotExist(request.categoryId)
            if (e.brandId != request.brandId) brandService.throwIfNotExist(request.brandId)
            if (repository.existsByBrandIdAndCategoryId(request.brandId, request.categoryId)) {
                throw InvalidRequestException("brandId, categoryId", "product already exists. $request")
            }
        }
        e.categoryId = request.categoryId
        e.brandId = request.brandId
        e.price = request.price
        repository.save(e)
    }
}