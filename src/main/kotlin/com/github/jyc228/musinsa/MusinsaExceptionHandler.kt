package com.github.jyc228.musinsa

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class MusinsaExceptionHandler {

    @ExceptionHandler(MusinsaException::class)
    fun exceptionHandler(e: MusinsaException): ResponseEntity<ErrorResponse> = when (e) {
        is BrandNotFoundException -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse("brand not found"))
    }

    data class ErrorResponse(val message: String)
}