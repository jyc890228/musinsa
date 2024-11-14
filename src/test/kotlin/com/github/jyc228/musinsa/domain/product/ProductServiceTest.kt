package com.github.jyc228.musinsa.domain.product

import com.github.jyc228.musinsa.InvalidRequestException
import com.github.jyc228.musinsa.domain.brand.BrandService
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import com.github.jyc228.musinsa.domain.product.ProductController.UpsertProductRequest as request

class ProductServiceTest {
    private val repository: ProductRepository = mock()
    private val brandService: BrandService = mock()

    private val service = ProductService(repository, brandService, mock())

    @Test
    fun `가격이 0원 이하인 경우 상품 등록 실패`() {
        val request = request(1, 1, 0.toBigInteger())

        shouldThrow<InvalidRequestException> { service.createProduct(request) }
    }

    @Test
    fun `brand, category 에 이미 상품이 있으면 등록 실패`() {
        val request = request(1, 1, 0.toBigInteger())

        given(repository.existsByBrandIdAndCategoryId(request.brandId, request.categoryId)).willReturn(true)

        shouldThrow<InvalidRequestException> { service.createProduct(request(1, 1, 0.toBigInteger())) }
    }
}