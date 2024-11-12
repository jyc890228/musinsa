package com.github.jyc228.musinsa

sealed class MusinsaException(message: String) : RuntimeException(message)

class BrandNotFoundException(id: Long) : MusinsaException("brand $id not found")