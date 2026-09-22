package com.example.omnigo.features.customer.home.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import com.example.omnigo.features.customer.home.domain.model.SavedAddress
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SavedAddressRepositoryImplTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: SavedAddressRepositoryImpl
    private val gson = Gson()
    private val key = stringPreferencesKey("saved_addresses_list_json")

    @Before
    fun setUp() {
        dataStore = mockk(relaxed = true)
        repository = SavedAddressRepositoryImpl(dataStore, gson)
    }

    @Test
    fun `getSavedAddresses returns empty list when no data is stored`() = runTest {
        val emptyPrefs = emptyPreferences()
        coEvery { dataStore.data } returns flowOf(emptyPrefs)

        repository.getSavedAddresses().test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getSavedAddresses returns parsed list sorted by updatedAt descending`() = runTest {
        val list = listOf(
            SavedAddress("1", "Nhà", "14D Nguyễn Hy Quang", 21.0, 105.8, updatedAt = 1000L),
            SavedAddress("2", "Công ty", "Đại học Phenikaa", 20.9, 105.7, updatedAt = 2000L)
        )
        val json = gson.toJson(list)
        val prefs = mockk<Preferences>()
        coEvery { prefs[key] } returns json
        coEvery { dataStore.data } returns flowOf(prefs)

        repository.getSavedAddresses().test {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertEquals("2", items[0].id)
            assertEquals("1", items[1].id)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
