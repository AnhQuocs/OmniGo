package com.example.omnigo.core.location.tracker

import android.content.Context
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlacesSearchServiceTest {

    private lateinit var context: Context
    private lateinit var service: PlacesSearchService

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any()) } returns 0

        context = mockk(relaxed = true)
        service = PlacesSearchService(context)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `searchPlaces with query shorter than 2 chars returns empty list`() = runTest {
        val result = service.searchPlaces("a")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `searchPlaces with blank query returns empty list`() = runTest {
        val result = service.searchPlaces("   ")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `retrievePlaceCoordinates with blank mapboxId returns null`() = runTest {
        val result = service.retrievePlaceCoordinates("", "session_123")
        assertNull(result)
    }

    @Test
    fun `retrievePlaceCoordinates with whitespace mapboxId returns null`() = runTest {
        val result = service.retrievePlaceCoordinates("   ", "session_123")
        assertNull(result)
    }
}
