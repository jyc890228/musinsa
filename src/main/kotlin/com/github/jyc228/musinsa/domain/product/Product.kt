package com.github.jyc228.musinsa.domain.product

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigInteger
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Entity
@Table(name = "product")
class ProductEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "brand_id")
    var brandId: Long,

    @Column(name = "category_id")
    var categoryId: Long,

    @Column(name = "price")
    var price: BigInteger,
) {
    fun copy(
        brandId: Long? = null,
        categoryId: Long? = null,
        price: BigInteger? = null
    ) = ProductEntity(id, brandId ?: this.brandId, categoryId ?: this.categoryId, price ?: this.price)
}

@Repository
interface ProductRepository : JpaRepository<ProductEntity, Long> {
    fun findAllByBrandId(brandId: Long): List<ProductEntity>

    fun existsByBrandIdAndCategoryId(brandId: Long, categoryId: Long): Boolean
}