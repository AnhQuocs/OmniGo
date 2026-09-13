package com.example.omnigo.core.network

import kotlinx.coroutines.flow.Flow

/**
 * Interface để theo dõi trạng thái kết nối mạng của thiết bị.
 */
interface ConnectivityObserver {

    /**
     * Quan sát trạng thái kết nối mạng dưới dạng một luồng dữ liệu (Flow).
     */
    fun observe(): Flow<Status>

    /**
     * Các trạng thái kết nối mạng có thể có.
     */
    enum class Status {
        Available, Unavailable, Losing, Lost
    }
}
