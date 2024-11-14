package com.github.jyc228.musinsa.domain.brand

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Entity
@Table(name = "brand")
class BrandEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "name")
    var name: String,

    @Column(name = "product_count")
    var productCount: Int = 0,
)

@Repository
interface BrandRepository : JpaRepository<BrandEntity, Long> {
    @Transactional
    @Modifying
    @Query("""UPDATE BrandEntity e SET e.name = :name WHERE e.id = :id""")
    fun updateNameById(name: String, id: Long): Int

    /**
     * [deleteById] 는 select -> delete 이므로 쿼리 2번 수행
     * BrandRepositoryTest 코드 참조
     */
    @Transactional
    @Modifying
    @Query("""DELETE BrandEntity e WHERE e.id = :id""")
    fun removeById(id: Long): Int

    @Transactional
    @Modifying
    @Query("""
        UPDATE BrandEntity e 
        SET e.productCount = CASE 
            WHEN e.productCount + :amount >= 0 THEN e.productCount + :amount 
        ELSE 0 END
        WHERE e.id = :id
        """)
    fun addProductCount(id: Long, amount: Int): Int
}