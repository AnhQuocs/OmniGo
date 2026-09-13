package com.example.omnigo.core.network

import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.features.auth.domain.repository.AuthRepository
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [TokenAuthenticator] chịu trách nhiệm tự động làm mới Access Token khi Server trả về lỗi 401.
 * Sử dụng cơ chế khóa [synchronized] để đảm bảo chỉ có một luồng thực hiện refresh tại một thời điểm.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val sessionManager: SessionManager,
    private val authRepositoryProvider: Lazy<AuthRepository>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Chỉ xử lý nếu mã phản hồi là 401 Unauthorized
        if (response.code != 401) return null

        synchronized(this) {
            // Lấy token hiện tại từ SessionManager
            val currentToken = runBlocking { sessionManager.getAccessToken() }
            
            // Lấy token từ request vừa bị lỗi 401
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            /**
             * Kiểm tra xem token đã được thay đổi (refresh) bởi một luồng khác chưa.
             * Nếu token hiện tại khác với token trong request, nghĩa là đã có luồng khác
             * refresh thành công -> Chỉ cần dùng token mới và thử lại request.
             */
            if (currentToken != null && currentToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // Nếu token chưa được refresh, tiến hành gọi API Refresh Token
            val isRefreshed = runBlocking {
                authRepositoryProvider.get().refreshToken()
            }

            if (isRefreshed) {
                // Lấy token mới sau khi refresh thành công
                val newToken = runBlocking { sessionManager.getAccessToken() }
                if (!newToken.isNullOrBlank()) {
                    return response.request.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                }
            }
        }

        // Nếu refresh thất bại hoặc không có token mới, trả về null để OkHttp dừng retry
        return null
    }
}
