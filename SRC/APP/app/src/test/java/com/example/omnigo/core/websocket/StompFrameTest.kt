package com.example.omnigo.core.websocket

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StompFrameTest {

    @Test
    fun `parse single CONNECTED frame with headers and null terminator`() {
        val raw = "CONNECTED\nversion:1.2\nsession:sess-123\n\n\u0000"
        val frames = StompFrame.parse(raw)

        assertEquals(1, frames.size)
        val frame = frames[0]
        assertEquals(StompCommand.CONNECTED, frame.command)
        assertEquals("1.2", frame.headers["version"])
        assertEquals("sess-123", frame.headers["session"])
        assertNull(frame.body)
    }

    @Test
    fun `parse MESSAGE frame with JSON body`() {
        val jsonBody = """{"orderId":101,"status":"CONFIRMED"}"""
        val raw = "MESSAGE\nsubscription:sub-0\nmessage-id:msg-1\ndestination:/topic/food-orders/101/status\n\n$jsonBody\u0000"
        val frames = StompFrame.parse(raw)

        assertEquals(1, frames.size)
        val frame = frames[0]
        assertEquals(StompCommand.MESSAGE, frame.command)
        assertEquals("sub-0", frame.headers["subscription"])
        assertEquals("msg-1", frame.headers["message-id"])
        assertEquals("/topic/food-orders/101/status", frame.headers["destination"])
        assertEquals(jsonBody, frame.body)
    }

    @Test
    fun `parse empty raw string or pure whitespace returns empty list`() {
        assertTrue(StompFrame.parse("").isEmpty())
        assertTrue(StompFrame.parse("\n\n").isEmpty())
        assertTrue(StompFrame.parse("\r\n\r\n").isEmpty())
    }

    @Test
    fun `parse multiple frames concatenated in single string`() {
        val raw = "MESSAGE\nsubscription:sub-1\n\nBody1\u0000MESSAGE\nsubscription:sub-2\n\nBody2\u0000"
        val frames = StompFrame.parse(raw)

        assertEquals(2, frames.size)
        assertEquals("sub-1", frames[0].headers["subscription"])
        assertEquals("Body1", frames[0].body)
        assertEquals("sub-2", frames[1].headers["subscription"])
        assertEquals("Body2", frames[1].body)
    }

    @Test
    fun `parse header escape sequences correctly`() {
        val raw = "MESSAGE\nspecial\\cvalue:colon\\nnewline\\rreturn\\\\slash\n\n\u0000"
        val frames = StompFrame.parse(raw)

        assertEquals(1, frames.size)
        val value = frames[0].headers["special:value"]
        assertNotNull(value)
        assertEquals("colon\nnewline\rreturn\\slash", value)
    }

    @Test
    fun `serialize CONNECT frame produces valid STOMP wire format`() {
        val frame = StompFrame.connect(
            acceptVersion = "1.1,1.2",
            host = "localhost",
            extraHeaders = mapOf("Authorization" to "Bearer test-token", "userId" to "42")
        )
        val wire = frame.toWire()

        assertTrue(wire.startsWith("CONNECT\n"))
        assertTrue(wire.contains("accept-version:1.1,1.2\n"))
        assertTrue(wire.contains("host:localhost\n"))
        assertTrue(wire.contains("Authorization:Bearer test-token\n"))
        assertTrue(wire.contains("userId:42\n"))
        assertTrue(wire.endsWith("\n\n\u0000"))
    }

    @Test
    fun `serialize SUBSCRIBE frame with unique id and destination`() {
        val frame = StompFrame.subscribe(
            destination = "/topic/driver-location/99",
            id = "sub-42"
        )
        val wire = frame.toWire()

        assertTrue(wire.startsWith("SUBSCRIBE\n"))
        assertTrue(wire.contains("destination:/topic/driver-location/99\n"))
        assertTrue(wire.contains("id:sub-42\n"))
        assertTrue(wire.endsWith("\n\n\u0000"))
    }

    @Test
    fun `serialize UNSUBSCRIBE frame with subscription id`() {
        val frame = StompFrame.unsubscribe("sub-42")
        val wire = frame.toWire()

        assertTrue(wire.startsWith("UNSUBSCRIBE\n"))
        assertTrue(wire.contains("id:sub-42\n"))
        assertTrue(wire.endsWith("\n\n\u0000"))
    }

    @Test
    fun `serialize SEND frame with destination and payload body`() {
        val frame = StompFrame.send(
            destination = "/app/location",
            body = """{"lat":10.5,"lng":106.5}"""
        )
        val wire = frame.toWire()

        assertTrue(wire.startsWith("SEND\n"))
        assertTrue(wire.contains("destination:/app/location\n"))
        assertTrue(wire.contains("content-length:24\n"))
        assertTrue(wire.contains("""{"lat":10.5,"lng":106.5}"""))
        assertTrue(wire.endsWith("\u0000"))
    }

    @Test
    fun `parse ERROR frame with message header and body`() {
        val raw = "ERROR\nmessage:Malformed frame received\n\nInvalid header syntax\u0000"
        val frames = StompFrame.parse(raw)

        assertEquals(1, frames.size)
        assertEquals(StompCommand.ERROR, frames[0].command)
        assertEquals("Malformed frame received", frames[0].headers["message"])
        assertEquals("Invalid header syntax", frames[0].body)
    }
}
