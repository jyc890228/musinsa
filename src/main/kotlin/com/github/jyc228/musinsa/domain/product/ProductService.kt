package com.github.jyc228.musinsa.domain.product

import com.github.jyc228.musinsa.InvalidRequestException
import com.github.jyc228.musinsa.ProductNotFoundException
import com.github.jyc228.musinsa.domain.brand.BrandService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val repository: ProductRepository,
    private val brandService: BrandService,
    private val app: ApplicationEventPublisher
) {
    @Transactional
    fun createProduct(request: ProductController.UpsertProductRequest): ProductEntity {
        request.throwIfInvalid()
        if (repository.existsByBrandIdAndCategoryId(request.brandId, request.categoryId)) {
            throw InvalidRequestException(request.brandId to request.categoryId, "product already exists")
        }
        brandService.addProductCount(request.brandId, 1)
        return repository.save(
            ProductEntity(
                brandId = request.brandId,
                categoryId = request.categoryId,
                price = request.price
            )
        ).also { app.publishEvent(ProductEvent.Created(it)) }
    }

    @Transactional
    fun updateProduct(request: ProductController.UpsertProductRequest, pid: Long) {
        request.throwIfInvalid()
        val e = repository.findByIdOrNull(pid) ?: throw ProductNotFoundException(pid)
        if (e.categoryId != request.categoryId || e.brandId != request.brandId) {
            if (e.brandId != request.brandId) brandService.throwIfNotExist(request.brandId)
            if (repository.existsByBrandIdAndCategoryId(request.brandId, request.categoryId)) {
                throw InvalidRequestException(request.brandId to request.categoryId, "product already exists")
            }
        }
        if (e.categoryId != request.categoryId) {
            brandService.addProductCount(e.brandId, -1)
            brandService.addProductCount(request.brandId, 1)
        }
        val prev = e.copy()
        e.categoryId = request.categoryId
        e.brandId = request.brandId
        e.price = request.price
        repository.save(e)
        app.publishEvent(ProductEvent.Updated(prev, e))
    }

    fun deleteProduct(pid: Long) {
        val product = repository.findByIdOrNull(pid) ?: throw ProductNotFoundException(pid)
        repository.delete(product)
        brandService.addProductCount(product.brandId, -1)
        app.publishEvent(ProductEvent.Deleted(product))
    }

    fun findAllProductsByBrandId(brandId: Long): List<ProductEntity> = repository.findAllByBrandId(brandId)
}