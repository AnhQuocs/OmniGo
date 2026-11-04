package com.example.omnigo.core.network.idempotency

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IdempotencyKeyManagerTest {

    private val context: Context = mockk(relaxed = true)
    private val prefs: SharedPreferences = mockk(relaxed = true)
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)
    private val storage = mutableMapOf<String, String>()

    private lateinit var idempotencyKeyManager: IdempotencyKeyManager

    @Before
    fun setUp() {
        storage.clear()
        every { context.getSharedPreferences(any(), any()) } returns prefs
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } answers {
            val k = firstArg<String>()
            val v = secondArg<String>()
            storage[k] = v
            editor
        }
        every { editor.remove(any()) } answers {
            val k = firstArg<String>()
            storage.remove(k)
            editor
        }
        every { editor.clear() } answers {
            storage.clear()
            editor
        }
        every { editor.apply() } returns Unit
        every { prefs.getString(any(), any()) } answers {
            val k = firstArg<String>()
            val default = secondArg<String?>()
            storage[k] ?: default
        }

        idempotencyKeyManager = IdempotencyKeyManager(context)
    }

    @Test
    fun `getOrCreateKey generates valid UUID and persists it`() {
        val key = idempotencyKeyManager.getOrCreateKey(
            scope = IdempotencyKeyManager.SCOPE_FOOD_ORDER,
            fingerprint = "order_123_payload_abc"
        )

        assertNotNull(key)
        assertEquals(36, key.length)
        assertTrue(key.contains("-"))
    }

    @Test
    fun `getOrCreateKey with same scope and fingerprint returns the same key on retry`() {
        val key1 = idempotencyKeyManager.getOrCreateKey(
            scope = IdempotencyKeyManager.SCOPE_FOOD_ORDER,
            fingerprint = "order_123_payload_abc"
        )
        val key2 = idempotencyKeyManager.getOrCreateKey(
            scope = IdempotencyKeyManager.SCOPE_FOOD_ORDER,
            fingerprint = "order_123_payload_abc"
        )

        assertEquals("Key must be reused for the same fingerprint (retry)", key1, key2)
    }

    @Test
    fun `getOrCreateKey with different fingerprint produces new key for new intent`() {
        val key1 = idempotencyKeyManager.getOrCreateKey(
            scope = IdempotencyKeyManager.SCOPE_FOOD_ORDER,
            fingerprint = "order_123_payload_abc"
        )
        val key2 = idempotencyKeyManager.getOrCreateKey(
            scope = IdempotencyKeyManager.SCOPE_FOOD_ORDER,
            fingerprint = "order_456_payload_xyz"
        )

        assertNotEquals("Different payloads must produce different keys", key1, key2)
    }

    @Test
    fun `clearKey removes saved key so subsequent call generates fresh key`() {
        val key1 = idempotencyKeyManager.getOrCreateKey(
            scope = IdempotencyKeyManager.SCOPE_BOOKING,
            fingerprint = "ride_start_end_bike"
        )
        idempotencyKeyManager.clearKey(IdempotencyKeyManager.SCOPE_BOOKING)

        val key2 = idempotencyKeyManager.getOrCreateKey(
            scope = IdempotencyKeyManager.SCOPE_BOOKING,
            fingerprint = "ride_start_end_bike"
        )

        assertNotEquals("Cleared key should result in fresh key generation", key1, key2)
    }
}

