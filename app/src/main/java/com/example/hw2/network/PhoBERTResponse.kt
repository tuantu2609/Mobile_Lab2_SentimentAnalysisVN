package com.example.hw2.network

data class PhoBERTResponse(
    val text: String,
    val sentiment: String,
    val label: Int
)