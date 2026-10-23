package com.example.omnigo.features.auth.data.mapper

import com.example.omnigo.features.auth.data.remote.dto.response.UserResponse
import com.example.omnigo.features.auth.domain.model.RegisterDriver
import com.example.omnigo.features.auth.domain.model.RegisterUser
import com.example.omnigo.features.auth.domain.model.UserRole
import com.example.omnigo.features.auth.domain.model.UserStatus
import com.example.omnigo.features.auth.domain.model.VehicleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AuthMapperTest {

    @Test
    fun `toDriverRequest maps all document and vehicle fields accurately`() {
        val registerDriver = RegisterDriver(
            user = RegisterUser(
                fullName = "Nguyễn Văn Tài",
                email = "driver@omnigo.com",
                phoneNumber = "0987654321",
                password = "Password123@"
            ),
            vehicleType = VehicleType.CAR_4_SEAT,
            licensePlate = "51A-999.99",
            vehicleModel = "Toyota Vios 2023",
            cccdNumber = "012345678901",
            cccdFrontImage = "https://res.cloudinary.com/omnigo/cccd_front.jpg",
            cccdBackImage = "https://res.cloudinary.com/omnigo/cccd_back.jpg",
            gplxNumber = "B2-12345678",
            gplxFrontImage = "https://res.cloudinary.com/omnigo/gplx_front.jpg",
            gplxBackImage = "https://res.cloudinary.com/omnigo/gplx_back.jpg"
        )

        val request = registerDriver.toDriverRequest(firebaseToken = "firebase_token_abc")

        assertEquals("driver@omnigo.com", request.email)
        assertEquals("Password123@", request.password)
        assertEquals("Nguyễn Văn Tài", request.fullName)
        assertEquals("0987654321", request.phoneNumber)
        assertEquals("firebase_token_abc", request.firebaseToken)
        assertEquals("CAR_4_SEAT", request.vehicleType)
        assertEquals("51A-999.99", request.licensePlate)
        assertEquals("Toyota Vios 2023", request.vehicleModel)
        assertEquals("012345678901", request.cccdNumber)
        assertEquals("https://res.cloudinary.com/omnigo/cccd_front.jpg", request.cccdFrontImage)
        assertEquals("https://res.cloudinary.com/omnigo/cccd_back.jpg", request.cccdBackImage)
        assertEquals("B2-12345678", request.gplxNumber)
        assertEquals("https://res.cloudinary.com/omnigo/gplx_front.jpg", request.gplxFrontImage)
        assertEquals("https://res.cloudinary.com/omnigo/gplx_back.jpg", request.gplxBackImage)
        assertEquals(false, request.autoApprove)
    }

    @Test
    fun `toDriverRequest maps blank document fields to null`() {
        val registerDriver = RegisterDriver(
            user = RegisterUser(
                fullName = "Tài xế Không Giấy Tờ",
                email = "driver2@omnigo.com",
                phoneNumber = "0987654322",
                password = "Password123@"
            ),
            vehicleType = VehicleType.BIKE,
            licensePlate = "59B1-12345",
            vehicleModel = "Honda Wave Alpha",
            cccdNumber = "",
            cccdFrontImage = "",
            cccdBackImage = "",
            gplxNumber = "",
            gplxFrontImage = "",
            gplxBackImage = ""
        )

        val request = registerDriver.toDriverRequest()

        assertNull(request.cccdNumber)
        assertNull(request.cccdFrontImage)
        assertNull(request.cccdBackImage)
        assertNull(request.gplxNumber)
        assertNull(request.gplxFrontImage)
        assertNull(request.gplxBackImage)
        assertNull(request.firebaseToken)
        assertEquals(false, request.autoApprove)
    }

    @Test
    fun `toDomain maps driver UserResponse correctly with driverInfo`() {
        val userResponse = UserResponse(
            id = 42L,
            phoneNumber = "0987654321",
            fullName = "Tài Xế Hạng A",
            email = "driver@omnigo.com",
            role = "DRIVER",
            status = "PENDING",
            vehicleType = "CAR_4_SEAT",
            licensePlate = "51F-12345",
            vehicleModel = "Hyundai Accent"
        )

        val domain = userResponse.toDomain()

        assertEquals(42L, domain.id)
        assertEquals("0987654321", domain.phoneNumber)
        assertEquals("Tài Xế Hạng A", domain.fullName)
        assertEquals("driver@omnigo.com", domain.email)
        assertEquals(UserRole.DRIVER, domain.role)
        assertEquals(UserStatus.PENDING, domain.status)
        assertNotNull(domain.driverInfo)
        assertEquals(VehicleType.CAR_4_SEAT, domain.driverInfo?.vehicleType)
        assertEquals("51F-12345", domain.driverInfo?.licensePlate)
        assertEquals("Hyundai Accent", domain.driverInfo?.vehicleModel)
    }
}
