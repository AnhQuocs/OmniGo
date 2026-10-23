package com.example.omnigo.features.auth.domain.usecase

import com.example.omnigo.features.auth.domain.error.RegisterError
import com.example.omnigo.features.auth.domain.model.AuthUser
import com.example.omnigo.features.auth.domain.model.DriverInfo
import com.example.omnigo.features.auth.domain.model.RegisterDriver
import com.example.omnigo.features.auth.domain.model.RegisterUser
import com.example.omnigo.features.auth.domain.model.UserRole
import com.example.omnigo.features.auth.domain.model.UserStatus
import com.example.omnigo.features.auth.domain.model.VehicleType
import com.example.omnigo.features.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class RegisterDriverUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: RegisterDriverUseCase

    private val validDriver = RegisterDriver(
        user = RegisterUser(
            fullName = "Trần Văn Bác Tài",
            email = "driver@omnigo.com",
            phoneNumber = "0987654321",
            password = "Password123@"
        ),
        vehicleType = VehicleType.BIKE,
        licensePlate = "59-X1 12345",
        vehicleModel = "Honda Wave",
        cccdNumber = "012345678901",
        cccdFrontImage = "https://res.cloudinary.com/omnigo/cccd_front.jpg",
        cccdBackImage = "https://res.cloudinary.com/omnigo/cccd_back.jpg",
        gplxNumber = "123456789",
        gplxFrontImage = "https://res.cloudinary.com/omnigo/gplx_front.jpg",
        gplxBackImage = "https://res.cloudinary.com/omnigo/gplx_back.jpg"
    )

    @Before
    fun setUp() {
        authRepository = mockk()
        useCase = RegisterDriverUseCase(authRepository)
    }

    @Test
    fun `invoke with missing cccd front image returns MISSING_CCCD_FRONT_IMAGE error`() = runTest {
        val driver = validDriver.copy(cccdFrontImage = "")
        val result = useCase(driver)

        assertTrue(result is RegisterResult.Error)
        assertEquals(RegisterError.MISSING_CCCD_FRONT_IMAGE, (result as RegisterResult.Error).error)
        coVerify(exactly = 0) { authRepository.registerDriver(any()) }
    }

    @Test
    fun `invoke with missing cccd back image returns MISSING_CCCD_BACK_IMAGE error`() = runTest {
        val driver = validDriver.copy(cccdBackImage = "   ")
        val result = useCase(driver)

        assertTrue(result is RegisterResult.Error)
        assertEquals(RegisterError.MISSING_CCCD_BACK_IMAGE, (result as RegisterResult.Error).error)
        coVerify(exactly = 0) { authRepository.registerDriver(any()) }
    }

    @Test
    fun `invoke with missing gplx front image returns MISSING_GPLX_FRONT_IMAGE error`() = runTest {
        val driver = validDriver.copy(gplxFrontImage = "")
        val result = useCase(driver)

        assertTrue(result is RegisterResult.Error)
        assertEquals(RegisterError.MISSING_GPLX_FRONT_IMAGE, (result as RegisterResult.Error).error)
        coVerify(exactly = 0) { authRepository.registerDriver(any()) }
    }

    @Test
    fun `invoke with missing gplx back image returns MISSING_GPLX_BACK_IMAGE error`() = runTest {
        val driver = validDriver.copy(gplxBackImage = "")
        val result = useCase(driver)

        assertTrue(result is RegisterResult.Error)
        assertEquals(RegisterError.MISSING_GPLX_BACK_IMAGE, (result as RegisterResult.Error).error)
        coVerify(exactly = 0) { authRepository.registerDriver(any()) }
    }

    @Test
    fun `invoke with empty fullName returns EMPTY_FULL_NAME error`() = runTest {
        val driver = validDriver.copy(user = validDriver.user.copy(fullName = ""))
        val result = useCase(driver)

        assertTrue(result is RegisterResult.Error)
        assertEquals(RegisterError.EMPTY_FULL_NAME, (result as RegisterResult.Error).error)
        coVerify(exactly = 0) { authRepository.registerDriver(any()) }
    }

    @Test
    fun `invoke with invalid phone returns INVALID_PHONE error`() = runTest {
        val driver = validDriver.copy(user = validDriver.user.copy(phoneNumber = "12345"))
        val result = useCase(driver)

        assertTrue(result is RegisterResult.Error)
        assertEquals(RegisterError.INVALID_PHONE, (result as RegisterResult.Error).error)
        coVerify(exactly = 0) { authRepository.registerDriver(any()) }
    }

    @Test
    fun `invoke with short password returns PASSWORD_TOO_SHORT error`() = runTest {
        val driver = validDriver.copy(user = validDriver.user.copy(password = "123"))
        val result = useCase(driver)

        assertTrue(result is RegisterResult.Error)
        assertEquals(RegisterError.PASSWORD_TOO_SHORT, (result as RegisterResult.Error).error)
        coVerify(exactly = 0) { authRepository.registerDriver(any()) }
    }

    @Test
    fun `invoke with empty vehicle license plate returns EMPTY_LICENSE_PLATE error`() = runTest {
        val driver = validDriver.copy(licensePlate = "")
        val result = useCase(driver)

        assertTrue(result is RegisterResult.Error)
        assertEquals(RegisterError.EMPTY_LICENSE_PLATE, (result as RegisterResult.Error).error)
        coVerify(exactly = 0) { authRepository.registerDriver(any()) }
    }

    @Test
    fun `invoke with valid driver data delegates to repository and returns success`() = runTest {
        val expectedUser = AuthUser(
            id = 100L,
            phoneNumber = "0987654321",
            email = "driver@omnigo.com",
            fullName = "Trần Văn Bác Tài",
            role = UserRole.DRIVER,
            status = UserStatus.PENDING,
            driverInfo = DriverInfo(
                vehicleType = VehicleType.BIKE,
                licensePlate = "59-X1 12345",
                vehicleModel = "Honda Wave"
            ),
            createdAt = LocalDateTime.now()
        )
        coEvery { authRepository.registerDriver(validDriver) } returns RegisterResult.Success(expectedUser)

        val result = useCase(validDriver)

        assertTrue(result is RegisterResult.Success)
        assertEquals(expectedUser, (result as RegisterResult.Success).user)
        coVerify(exactly = 1) { authRepository.registerDriver(validDriver) }
    }
}
