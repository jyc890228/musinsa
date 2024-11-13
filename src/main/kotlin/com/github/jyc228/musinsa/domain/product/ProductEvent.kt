package com.github.jyc228.musinsa.domain.product

sealed class ProductEvent {
    class Created(val entity: ProductEntity) : ProductEvent()
    class Updated(val entity: ProductEntity) : ProductEvent()
    class Deleted(val pid: Long) : ProductEvent()
}