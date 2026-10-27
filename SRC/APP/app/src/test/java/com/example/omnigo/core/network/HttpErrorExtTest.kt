package com.example.omnigo.core.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class HttpErrorExtTest {

    private fun httpException(code: Int, body: String): HttpException =
        HttpException(Response.error<Any>(code, body.toResponseBody("application/json".toMediaType())))

    @Test
    fun `returns message field from backend error body`() {
        val e = httpException(409, """{"success":false,"message":"Customer already has an active booking","data":null}""")
        assertEquals("Customer already has an active booking", e.toBackendMessage())
    }

    @Test
    fun `returns null when body has no message and reason phrase is blank`() {
        val e = httpException(500, """{"success":false,"data":null}""")
        assertNull(e.toBackendMessage())
    }

    @Test
    fun `returns null when body message is blank`() {
        val e = httpException(400, """{"success":false,"message":"   "}""")
        assertNull(e.toBackendMessage())
    }

    @Test
    fun `returns null for non json body without crashing`() {
        val e = httpException(502, "<html>Bad gateway</html>")
        assertNull(e.toBackendMessage())
    }
}
