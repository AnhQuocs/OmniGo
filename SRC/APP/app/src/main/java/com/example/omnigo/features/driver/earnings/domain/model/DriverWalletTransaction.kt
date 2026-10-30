package com.example.omnigo.features.driver.earnings.domain.model

data class DriverWalletTransaction(
    val id: Long = 0L,
    val description: String = "",
    val timeFormatted: String = "",
    val amountFormatted: String = "",
    val isIncome: Boolean = true,
    val status: String = "SUCCESS",
    val statusLabel: String = "Thành công"
)
