package com.github.jyc228.musinsa.domain.product

import com.github.jyc228.musinsa.InvalidRequestException
import com.github.jyc228.musinsa.ProductNotFoundException
import com.github.jyc228.musinsa.domain.brand.BrandService
import com.github.jyc228.musinsa.domain.category.Category
import java.math.BigInteger
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
        if (request.price <= BigInteger.ZERO) {
            throw InvalidRequestException("price", "price must be greater than 0. $request")
        }
        Category.throwIfNotExist(request.categoryId)
        if (repository.existsByBrandIdAndCategoryId(request.brandId, request.categoryId)) {
            throw InvalidRequestException("brandId, categoryId", "product already exists. $request")
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
        if (request.price <= BigInteger.ZERO) {
            throw InvalidRequestException("price", "price must be greater than 0. $request")
        }
        val e = repository.findByIdOrNull(pid) ?: throw ProductNotFoundException(pid)
        if (e.categoryId != request.categoryId || e.brandId != request.brandId) {
            if (e.categoryId != request.categoryId) Category.throwIfNotExist(request.categoryId)
            if (e.brandId != request.brandId) brandService.throwIfNotExist(request.brandId)
            if (repository.existsByBrandIdAndCategoryId(request.brandId, request.categoryId)) {
                throw InvalidRequestException("brandId, categoryId", "product already exists. $request")
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
        app.publishEvent(ProductEvent.Deleted(pid))
    }

    fun findAllProductsByBrandId(brandId: Long): List<ProductEntity> = repository.findAllByBrandId(brandId)
}