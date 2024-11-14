package com.github.jyc228.musinsa.domain.category

import com.github.jyc228.musinsa.CategoryNotFoundException

data class Category(
    val id: Int,
    val name: String
) {
    companion object {
        val all = listOf(
            Category(1, "상의"),
            Category(2, "아우터"),
            Category(3, "바지"),
            Category(4, "스니커즈"),
            Category(5, "가방"),
            Category(6, "모자"),
            Category(7, "양말"),
            Category(8, "액세서리"),
        ).associateBy { it.id }

        val allIds = all.keys.toList()

        operator fun get(id: Int) = all[id]
        operator fun get(name: String) = all.values.find { it.name == name }
        fun getOrThrow(name: String) = get(name) ?: throw CategoryNotFoundException(name)
        fun throwIfNotExist(id: Int) {
            if (id !in all) throw CategoryNotFoundException(id)
        }
    }
}
