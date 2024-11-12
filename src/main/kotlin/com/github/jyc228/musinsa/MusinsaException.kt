package com.github.jyc228.musinsa

sealed class MusinsaException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class BrandNotFoundException(id: Long) : MusinsaException("brand $id not found")
class CategoryNotFoundException(id: Long) : MusinsaException("category $id not found")
class ProductNotFoundException(id: Long) : MusinsaException("product $id not found")

class InvalidRequestException(val param: String, message: String) : MusinsaException(message)

class UpsertProductException(override val cause: MusinsaException) : MusinsaException("create product failed", cause)