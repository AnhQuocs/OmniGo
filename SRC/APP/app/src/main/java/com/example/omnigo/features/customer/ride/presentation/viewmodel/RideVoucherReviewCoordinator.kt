package com.example.omnigo.features.customer.ride.presentation.viewmodel

import com.example.omnigo.R
import com.example.omnigo.features.customer.promos.domain.usecase.ApplyVoucherUseCase
import com.example.omnigo.features.customer.promos.domain.usecase.CommitVoucherUseCase
import com.example.omnigo.features.customer.promos.domain.usecase.GetApplicableVouchersUseCase
import com.example.omnigo.features.customer.promos.domain.usecase.RollbackVoucherUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.SubmitBookingReviewUseCase
import com.example.omnigo.features.customer.ride.presentation.state.RideBookingUiState
import com.example.omnigo.utils.UiText
import com.example.omnigo.utils.formatVnd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class RideVoucherReviewCoordinator @Inject constructor(
    private val getApplicableVouchersUseCase: GetApplicableVouchersUseCase,
    private val applyVoucherUseCase: ApplyVoucherUseCase,
    private val commitVoucherUseCase: CommitVoucherUseCase,
    private val rollbackVoucherUseCase: RollbackVoucherUseCase,
    private val submitBookingReviewUseCase: SubmitBookingReviewUseCase
) {

    fun openVoucherSheet(
        scope: CoroutineScope,
        currentState: RideBookingUiState,
        updateState: ((RideBookingUiState) -> RideBookingUiState) -> Unit
    ) {
        val baseFare = currentState.pricingEstimate?.totalPrice ?: 50000.0
        updateState { it.copy(showVoucherSheet = true, isLoadingVouchers = true) }
        scope.launch {
            getApplicableVouchersUseCase(orderAmount = baseFare, serviceType = "RIDE")
                .onSuccess { list ->
                    updateState { it.copy(isLoadingVouchers = false, applicableVouchers = list) }
                }
                .onFailure {
                    updateState { it.copy(isLoadingVouchers = false) }
                }
        }
    }

    fun applyVoucherCode(
        scope: CoroutineScope,
        code: String,
        currentState: RideBookingUiState,
        updateState: ((RideBookingUiState) -> RideBookingUiState) -> Unit
    ) {
        val baseFare = currentState.pricingEstimate?.totalPrice
        if (baseFare == null || baseFare <= 0.0) {
            updateState { it.copy(pendingPromoCode = code, showVoucherSheet = false) }
            return
        }
        updateState { it.copy(isLoadingVouchers = true) }
        scope.launch {
            applyVoucherUseCase(code = code, orderAmount = baseFare, serviceType = "RIDE")
                .onSuccess { applied ->
                    updateState {
                        it.copy(
                            isLoadingVouchers = false,
                            showVoucherSheet = false,
                            appliedVoucher = applied,
                            pendingPromoCode = null,
                            infoMessage = UiText.StringResource(
                                R.string.promos_saved_code_msg,
                                applied.voucherCode,
                                applied.discountAmount.formatVnd()
                            )
                        )
                    }
                }
                .onFailure { err ->
                    updateState {
                        it.copy(
                            isLoadingVouchers = false,
                            errorMessage = err.message?.let(UiText::DynamicString)
                        )
                    }
                }
        }
    }

    fun applyPendingVoucherIfEligible(
        scope: CoroutineScope,
        code: String,
        baseFare: Double,
        updateState: ((RideBookingUiState) -> RideBookingUiState) -> Unit
    ) {
        scope.launch {
            applyVoucherUseCase(code = code, orderAmount = baseFare, serviceType = "RIDE")
                .onSuccess { applied ->
                    updateState {
                        it.copy(
                            appliedVoucher = applied,
                            pendingPromoCode = null,
                            infoMessage = UiText.StringResource(
                                R.string.promos_saved_code_msg,
                                applied.voucherCode,
                                applied.discountAmount.formatVnd()
                            )
                        )
                    }
                }
        }
    }

    fun commitAppliedVoucherIfPresent(
        scope: CoroutineScope,
        currentState: RideBookingUiState,
        bookingId: Long
    ) {
        val applied = currentState.appliedVoucher ?: return
        val orderAmt = currentState.pricingEstimate?.totalPrice ?: applied.originalAmount
        scope.launch {
            commitVoucherUseCase(
                code = applied.voucherCode,
                orderAmount = orderAmt,
                serviceType = "RIDE",
                orderId = bookingId.toString(),
                discountAmount = applied.discountAmount
            )
        }
    }

    fun rollbackVoucherIfPresent(
        scope: CoroutineScope,
        voucherCode: String?,
        bookingId: Long
    ) {
        if (voucherCode.isNullOrBlank()) return
        scope.launch {
            rollbackVoucherUseCase(code = voucherCode, orderId = bookingId.toString())
        }
    }

    fun submitTripReview(
        scope: CoroutineScope,
        bookingId: Long,
        rating: Int,
        comment: String?,
        updateState: ((RideBookingUiState) -> RideBookingUiState) -> Unit,
        onComplete: () -> Unit
    ) {
        scope.launch {
            submitBookingReviewUseCase(bookingId = bookingId, rating = rating, comment = comment)
                .onSuccess {
                    updateState {
                        it.copy(infoMessage = UiText.StringResource(R.string.ride_review_submitted_msg))
                    }
                    onComplete()
                }
                .onFailure {
                    onComplete()
                }
        }
    }

    suspend fun loadApplicableVouchers(
        orderAmount: Double,
        serviceType: String
    ): List<com.example.omnigo.features.customer.promos.domain.model.Voucher> {
        return getApplicableVouchersUseCase(orderAmount = orderAmount, serviceType = serviceType)
            .getOrDefault(emptyList())
    }

    suspend fun applyVoucher(
        code: String,
        orderAmount: Double,
        serviceType: String
    ): Result<com.example.omnigo.features.customer.promos.domain.model.AppliedVoucherResult> {
        return applyVoucherUseCase(code = code, orderAmount = orderAmount, serviceType = serviceType)
    }

    suspend fun commitVoucher(
        code: String,
        orderAmount: Double,
        serviceType: String,
        orderId: String?,
        discountAmount: Double = 0.0
    ): Result<com.example.omnigo.features.customer.promos.domain.model.AppliedVoucherResult> {
        return commitVoucherUseCase(
            code = code,
            orderAmount = orderAmount,
            serviceType = serviceType,
            orderId = orderId,
            discountAmount = discountAmount
        )
    }

    suspend fun rollbackVoucher(
        code: String,
        orderId: String?
    ): Result<Unit> {
        return rollbackVoucherUseCase(code = code, orderId = orderId)
    }
}

