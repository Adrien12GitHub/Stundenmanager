package com.example.stundenmanager

import java.util.Date

data class Shift(
    val userId: String,
    val startTime: Date,
    val endTime: Date
)