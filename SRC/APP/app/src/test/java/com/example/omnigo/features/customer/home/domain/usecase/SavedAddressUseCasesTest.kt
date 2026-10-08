package com.example.omnigo.features.customer.home.domain.usecase

import app.cash.turbine.test
import com.example.omnigo.features.customer.home.domain.model.SavedAddress
import com.example.omnigo.features.customer.home.domain.repository.SavedAddressRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SavedAddressUseCasesTest {

    private lateinit var repository: SavedAddressRepository
    private lateinit var getSavedAddressesUseCase: GetSavedAddressesUseCase
    private lateinit var saveAddressUseCase: SaveAddressUseCase
    private lateinit var deleteSavedAddressUseCase: DeleteSavedAddressUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        getSavedAddressesUseCase = GetSavedAddressesUseCase(repository)
        saveAddressUseCase = SaveAddressUseCase(repository)
        deleteSavedAddressUseCase = DeleteSavedAddressUseCase(repository)
    }

    @Test
    fun `GetSavedAddressesUseCase emits addresses from repository`() = runTest {
        val list = listOf(SavedAddress("1", "Nhà", "14D Nguyễn Hy Quang", 21.0, 105.8))
        coEvery { repository.getSavedAddresses() } returns flowOf(list)

        getSavedAddressesUseCase().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Nhà", result[0].addressName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SaveAddressUseCase delegates to repository`() = runTest {
        val address = SavedAddress("1", "Nhà", "14D Nguyễn Hy Quang", 21.0, 105.8)
        saveAddressUseCase(address)
        coVerify { repository.saveAddress(address) }
    }

    @Test
    fun `DeleteSavedAddressUseCase delegates to repository`() = runTest {
        deleteSavedAddressUseCase("1")
        coVerify { repository.deleteAddress("1") }
    }
}
