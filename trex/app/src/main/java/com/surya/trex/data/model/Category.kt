package com.surya.trex.data.model

data class Category(
    val id: Int,
    val category_name: String,
    val category_type: String
)

data class CategoryCreateRequest(
    val category_name: String,
    val category_type: String
)

data class CategoryUpdateRequest(
    val category_name: String,
    val category_type: String
)

data class CategoryDeleteRequest(
    val category_id: Int
)
