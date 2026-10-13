package com.example.luapp

enum class ActivityType {
    FEEDING, POOP, BATH
}

data class Feeding(
    val id: Int,
    val type: ActivityType = ActivityType.FEEDING,
    val amountMl: Int = 0,
    val timestamp: Long
)
