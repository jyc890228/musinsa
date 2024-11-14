package com.github.jyc228.musinsa

import com.github.jyc228.musinsa.domain.brand.BrandController
import com.github.jyc228.musinsa.domain.brand.BrandService
import com.github.jyc228.musinsa.domain.category.Category
import com.github.jyc228.musinsa.domain.product.ProductController
import com.github.jyc228.musinsa.domain.product.ProductService
import org.springframework.boot.web.context.WebServerInitializedEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("init-data")
class InitDataService(
    private val brandService: BrandService,
    private val productService: ProductService
) : ApplicationListener<WebServerInitializedEvent> {

    override fun onApplicationEvent(event: WebServerInitializedEvent) {
        println("init data")
        val csv = requireNotNull(InitDataService::class.java.getResource("/data.csv"))
        csv.readText().splitToSequence("\n")
            .filter { it.isNotBlank() }
            .map { it.split(",") }
            .drop(1)
            .forEach { data ->
                val brand = brandService.createBrand(BrandController.UpsertBrandRequest(data[0]))
                Category.allIds.forEach { categoryId ->
                    productService.createProduct(
                        ProductController.UpsertProductRequest(
                            brandId = brand.id,
                            categoryId = categoryId,
                            price = data[categoryId].toBigInteger()
                        )
                    )
                }
            }
        println("init data end")
    }
}