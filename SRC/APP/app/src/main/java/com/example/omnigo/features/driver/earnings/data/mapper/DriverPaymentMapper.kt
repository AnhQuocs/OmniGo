package com.example.omnigo.features.driver.earnings.data.mapper

import com.example.omnigo.features.driver.earnings.data.remote.dto.TransactionResponseDto
import com.example.omnigo.features.driver.earnings.data.remote.dto.WalletResponseDto
import com.example.omnigo.features.driver.earnings.domain.model.DriverWallet
import com.example.omnigo.features.driver.earnings.domain.model.DriverWalletTransaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DriverPaymentMapper {

    fun mapToDomain(dto: WalletResponseDto): DriverWallet {
        return DriverWallet(
            walletId = dto.walletId ?: 0L,
            balance = dto.balance ?: 0.0,
            userType = dto.userType ?: "DRIVER"
        )
    }

    fun mapToDomain(dto: TransactionResponseDto): DriverWalletTransaction {
        val type = dto.transactionType.orEmpty().uppercase()
        val isIncome = type in listOf("DEPOSIT", "TRIP_INCOME", "REFUND")
        val amount = dto.amount ?: 0.0
        val prefix = if (isIncome) "+" else "-"
        val formattedAmount = String.format(Locale.getDefault(), "%s%,.0f ₫", prefix, amount)

        val description = when (type) {
            "DEPOSIT" -> "Nạp tiền ví ký quỹ"
            "TRIP_INCOME" -> "Thu nhập chuyến xe ${dto.bookingId?.let { "#$it" } ?: ""}".trim()
            "TRIP_PAYMENT" -> "Thanh toán cước phí ${dto.bookingId?.let { "#$it" } ?: ""}".trim()
            "COMMISSION_FEE" -> "Khấu trừ phí nền tảng"
            "WITHDRAW" -> "Rút tiền về ngân hàng"
            else -> "Giao dịch ví ${dto.orderId.orEmpty()}".trim()
        }

        val formattedTime = formatTime(dto.createdAt)
        val status = dto.status.orEmpty().uppercase().ifEmpty { "SUCCESS" }
        val statusLabel = when (status) {
            "PENDING" -> "Chờ thanh toán"
            "SUCCESS" -> "Thành công"
            "FAILED" -> "Thất bại"
            "CANCELED" -> "Đã hủy"
            else -> status
        }

        return DriverWalletTransaction(
            id = dto.id ?: 0L,
            description = description,
            timeFormatted = formattedTime,
            amountFormatted = formattedAmount,
            isIncome = isIncome,
            status = status,
            statusLabel = statusLabel
        )
    }

    private fun formatTime(raw: String?): String {
        if (raw.isNullOrBlank()) return "Gần đây"
        return try {
            val ldt = LocalDateTime.parse(raw.take(19))
            ldt.format(DateTimeFormatter.ofPattern("HH:mm - dd/MM"))
        } catch (_: Exception) {
            raw.take(16).replace("T", " ")
        }
    }
}
