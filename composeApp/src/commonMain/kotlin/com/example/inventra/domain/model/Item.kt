package com.example.inventra.domain.model

data class Item(
    val id: String,
    val name: String,
    val category: String,
    val location: String,
    val status: String,
    val imageUrl: String?
)
