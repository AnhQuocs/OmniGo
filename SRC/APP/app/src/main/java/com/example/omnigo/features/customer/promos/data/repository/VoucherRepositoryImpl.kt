package com.example.omnigo.features.customer.promos.data.repository

import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.features.customer.promos.data.mapper.VoucherMapper
import com.example.omnigo.features.customer.promos.data.remote.dto.ApplyVoucherRequestDto
import com.example.omnigo.features.customer.promos.data.remote.dto.CommitVoucherRequestDto
import com.example.omnigo.features.customer.promos.domain.model.AppliedVoucherResult
import com.example.omnigo.features.customer.promos.domain.model.Voucher
import com.example.omnigo.features.customer.promos.domain.model.VoucherUsage
import com.example.omnigo.features.customer.promos.domain.repository.VoucherRepository
import com.example.omnigo.features.customer.ride.data.remote.api.PricingApi
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoucherRepositoryImpl @Inject constructor(
    private val pricingApi: PricingApi,
    private val sessionManager: SessionManager
) : VoucherRepository {

    override suspend fun getVouchers(serviceType: String?): Result<List<Voucher>> {
        return try {
            val userId = sessionManager.getUserId()?.toString()
            val response = pricingApi.getVouchers(serviceType = serviceType, userId = userId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                val list = body.data?.map(VoucherMapper::toDomain) ?: emptyList()
                Result.success(list)
            } else {
                Result.failure(Exception(extractError(response, body?.message, "Không thể tải danh sách voucher")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getApplicableVouchers(
        orderAmount: Double,
        serviceType: String
    ): Result<List<Voucher>> {
        return try {
            val userId = sessionManager.getUserId()?.toString()
            val response = pricingApi.getApplicableVouchers(
                orderAmount = orderAmount,
                serviceType = serviceType,
                userId = userId
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                val list = body.data?.map(VoucherMapper::toDomain) ?: emptyList()
                Result.success(list)
            } else {
                Result.failure(Exception(extractError(response, body?.message, "Không thể tải voucher phù hợp")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun applyVoucher(
        code: String,
        orderAmount: Double,
        serviceType: String
    ): Result<AppliedVoucherResult> {
        return try {
            val trimmedCode = code.trim().uppercase()
            if (trimmedCode.isBlank()) {
                return Result.failure(Exception("Vui lòng nhập mã giảm giá"))
            }
            val userId = sessionManager.getUserId()?.toString()
            val response = pricingApi.applyVoucher(
                ApplyVoucherRequestDto(
                    code = trimmedCode,
                    orderAmount = orderAmount,
                    serviceType = serviceType,
                    userId = userId
                )
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(VoucherMapper.toAppliedDomain(body.data, trimmedCode, orderAmount))
            } else {
                Result.failure(Exception(extractError(response, body?.message, "Mã giảm giá không hợp lệ")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun commitVoucher(
        code: String,
        orderAmount: Double,
        serviceType: String,
        orderId: String?
    ): Result<AppliedVoucherResult> {
        return try {
            val trimmedCode = code.trim().uppercase()
            val userId = sessionManager.getUserId()?.toString()?.takeIf { it.isNotBlank() }
                ?: return Result.failure(Exception("Thiếu userId để xác nhận mã giảm giá"))
            val response = pricingApi.commitVoucher(
                CommitVoucherRequestDto(
                    code = trimmedCode,
                    orderAmount = orderAmount,
                    serviceType = serviceType,
                    userId = userId,
                    orderId = orderId
                )
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(VoucherMapper.toAppliedDomain(body.data, trimmedCode, orderAmount))
            } else {
                Result.failure(Exception(extractError(response, body?.message, "Không thể xác nhận mã giảm giá")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rollbackVoucher(code: String, orderId: String?): Result<Unit> {
        return try {
            val trimmedCode = code.trim().uppercase()
            val userId = sessionManager.getUserId()?.toString()?.takeIf { it.isNotBlank() }
                ?: return Result.failure(Exception("Thiếu userId để hoàn lại mã giảm giá"))
            val response = pricingApi.rollbackVoucher(
                code = trimmedCode,
                userId = userId,
                orderId = orderId
            )
            val body = response.body()
            if (response.isSuccessful && (body == null || body.success)) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(extractError(response, body?.message, "Không thể hoàn lại mã giảm giá")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMyVoucherUsages(): Result<List<VoucherUsage>> {
        return try {
            val userId = sessionManager.getUserId()?.toString()?.takeIf { it.isNotBlank() }
                ?: return Result.success(emptyList())
            val response = pricingApi.getMyVoucherUsages(userId = userId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                val list = body.data?.map(VoucherMapper::toUsageDomain) ?: emptyList()
                Result.success(list)
            } else {
                Result.failure(Exception(extractError(response, body?.message, "Không thể tải lịch sử dùng mã")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractError(response: Response<*>, bodyMessage: String?, fallback: String): String {
        if (!bodyMessage.isNullOrBlank()) return bodyMessage
        val raw = runCatching { response.errorBody()?.string() }.getOrNull()
        if (!raw.isNullOrBlank()) {
            val parsed = runCatching { JSONObject(raw).optString("message") }.getOrNull()
            if (!parsed.isNullOrBlank()) return parsed
        }
        return fallback
    }
}
