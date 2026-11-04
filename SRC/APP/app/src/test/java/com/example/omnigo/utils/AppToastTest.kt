package com.example.omnigo.utils

import android.content.Context
import android.widget.Toast
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class AppToastTest {

    private lateinit var mockContext: Context
    private lateinit var mockAppContext: Context
    private lateinit var mockToast1: Toast
    private lateinit var mockToast2: Toast

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        mockAppContext = mockk(relaxed = true)
        every { mockContext.applicationContext } returns mockAppContext
        every { mockContext.getString(any()) } returns "Test string from res"

        mockToast1 = mockk(relaxed = true)
        mockToast2 = mockk(relaxed = true)

        mockkStatic(Toast::class)
        every { Toast.makeText(mockAppContext, any<CharSequence>(), any()) } returns mockToast1
        AppToast.cancel()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun show_cancelsPreviousToast_beforeShowingNew() {
        every { Toast.makeText(mockAppContext, "First", any()) } returns mockToast1
        every { Toast.makeText(mockAppContext, "Second", any()) } returns mockToast2

        AppToast.show(mockContext, "First")
        verify(exactly = 1) { mockToast1.show() }

        AppToast.show(mockContext, "Second")
        verify(exactly = 1) { mockToast1.cancel() }
        verify(exactly = 1) { mockToast2.show() }
    }

    @Test
    fun show_ignoresBlankMessage() {
        AppToast.show(mockContext, "   ")
        verify(exactly = 0) { mockToast1.show() }
    }

    @Test
    fun show_throttlesIdenticalMessages() {
        AppToast.show(mockContext, "SpamMessage")
        AppToast.show(mockContext, "SpamMessage")
        AppToast.show(mockContext, "SpamMessage")

        // Only first call triggers makeText & show
        verify(exactly = 1) { mockToast1.show() }
    }

    @Test
    fun cancel_cancelsCurrentToast() {
        AppToast.show(mockContext, "Message")
        AppToast.cancel()
        verify(exactly = 1) { mockToast1.cancel() }
    }
}

