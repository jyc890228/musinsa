package com.github.jyc228.musinsa.domain.brand

import com.github.jyc228.musinsa.IntegrationTest
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class BrandControllerTest : IntegrationTest() {

    @Test
    fun `brand 를 등록할 수 있다`() {
        runBlocking {
            val response = client.post("/api/brands") {
                setBody(BrandController.UpsertBrandRequest(name = "test"))
            }
            response.body<BrandController.BrandIdResponse>().id shouldBeGreaterThan 0
        }
    }
}
