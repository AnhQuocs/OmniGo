package com.example.omnigo.features.driver.earnings.domain.model

data class DriverWallet(
    val walletId: Long = 0L,
    val balance: Double = 0.0,
    val userType: String = "DRIVER"
)

