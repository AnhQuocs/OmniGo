package com.example.omnigo.features.customer.ride.data.repository

import com.example.omnigo.features.customer.ride.data.mapper.toDomain
import com.example.omnigo.features.customer.ride.data.remote.api.RideBookingApi
import com.example.omnigo.features.customer.ride.data.remote.dto.BookingReviewRequestDto
import com.example.omnigo.features.customer.ride.data.remote.dto.BookingReviewResponseDto
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingReview
import com.example.omnigo.features.customer.ride.domain.model.DriverBookingReviewsSummary
import com.example.omnigo.features.customer.ride.domain.repository.RideReviewRepository
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RideReviewRepositoryImpl @Inject constructor(
    private val rideBookingApi: RideBookingApi
) : RideReviewRepository {

    override suspend fun getBookingDetail(bookingId: Long): Result<Booking> {
        return try {
            val response = rideBookingApi.getBookingDetail(bookingId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data.toDomain())
            } else {
                Result.failure(Exception(extractError(response, body?.message, "Không thể tải chi tiết chuyến đi")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitBookingReview(
        bookingId: Long,
        rating: Int,
        comment: String?
    ): Result<BookingReview> {
        if (rating !in 1..5) {
            return Result.failure(Exception("Số sao đánh giá phải từ 1 đến 5"))
        }
        return try {
            val response = rideBookingApi.submitBookingReview(
                bookingId = bookingId,
                request = BookingReviewRequestDto(
                    rating = rating,
                    comment = comment?.trim()?.takeIf { it.isNotBlank() }
                )
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(mapReview(body.data, bookingId))
            } else {
                Result.failure(Exception(extractError(response, body?.message, "Không thể gửi đánh giá chuyến đi")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBookingReview(bookingId: Long): Result<BookingReview?> {
        return try {
            val response = rideBookingApi.getBookingReview(bookingId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                if (body.success && body.data != null) {
                    Result.success(mapReview(body.data, bookingId))
                } else {
                    Result.success(null)
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDriverBookingReviews(driverId: Long): Result<DriverBookingReviewsSummary> {
        return try {
            val response = rideBookingApi.getDriverBookingReviews(driverId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                val data = body.data
                val reviews = data.reviews?.map { mapReview(it, it.bookingId ?: 0L) } ?: emptyList()
                Result.success(
                    DriverBookingReviewsSummary(
                        driverId = data.driverId ?: driverId,
                        totalReviews = data.totalReviews ?: reviews.size,
                        averageRating = data.averageRating ?: 0.0,
                        reviews = reviews
                    )
                )
            } else {
                Result.failure(Exception(extractError(response, body?.message, "Không thể tải danh sách đánh giá tài xế")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapReview(dto: BookingReviewResponseDto, fallbackBookingId: Long): BookingReview {
        return BookingReview(
            id = dto.id ?: 0L,
            bookingId = dto.bookingId ?: fallbackBookingId,
            customerId = dto.customerId ?: 0L,
            driverId = dto.driverId ?: 0L,
            rating = dto.rating ?: 5,
            comment = dto.comment.orEmpty(),
            createdAt = dto.createdAt.orEmpty()
        )
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
