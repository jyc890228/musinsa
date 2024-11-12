package com.github.jyc228.musinsa.domain.brand

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@DataJpaTest
class BrandRepositoryTest {

    @Autowired
    lateinit var em: TestEntityManager

    @Autowired
    lateinit var repository: BrandRepository

    @Test
    fun `deleteById`() {
        val brand = repository.save(BrandEntity(name = "test-1"))
        em.clear()

        repository.deleteById(brand.id)
        em.flush()
    }

    @Test
    fun `removeById`() {
        val brand = repository.save(BrandEntity(name = "test-2"))
        em.clear()

        repository.removeById(brand.id)
        em.flush()
    }
}
