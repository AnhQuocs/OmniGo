package com.example.omnigo.core.websocket

data class StompFrame(
    val command: StompCommand,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null
) {
    fun toRawString(): String {
        val builder = StringBuilder()
        builder.append(command.name).append("\n")
        for ((key, value) in headers) {
            builder.append(escapeHeader(key)).append(":").append(escapeHeader(value)).append("\n")
        }
        builder.append("\n")
        if (!body.isNullOrEmpty()) {
            builder.append(body)
        }
        builder.append(NULL_CHAR)
        return builder.toString()
    }

    fun toWire(): String = toRawString()

    companion object {
        const val NULL_CHAR = "\u0000"

        fun connect(
            acceptVersion: String = "1.1,1.2",
            host: String = "localhost",
            extraHeaders: Map<String, String> = emptyMap()
        ): StompFrame {
            val headers = mutableMapOf(
                "accept-version" to acceptVersion,
                "host" to host
            )
            headers.putAll(extraHeaders)
            return StompFrame(command = StompCommand.CONNECT, headers = headers)
        }

        fun subscribe(
            destination: String,
            id: String,
            extraHeaders: Map<String, String> = emptyMap()
        ): StompFrame {
            val headers = mutableMapOf(
                "id" to id,
                "destination" to destination
            )
            headers.putAll(extraHeaders)
            return StompFrame(command = StompCommand.SUBSCRIBE, headers = headers)
        }

        fun unsubscribe(id: String): StompFrame {
            return StompFrame(
                command = StompCommand.UNSUBSCRIBE,
                headers = mapOf("id" to id)
            )
        }

        fun send(
            destination: String,
            body: String,
            extraHeaders: Map<String, String> = emptyMap()
        ): StompFrame {
            val headers = mutableMapOf(
                "destination" to destination,
                "content-length" to body.toByteArray(Charsets.UTF_8).size.toString()
            )
            headers.putAll(extraHeaders)
            return StompFrame(command = StompCommand.SEND, headers = headers, body = body)
        }

        fun parse(rawText: String): List<StompFrame> {
            val frames = mutableListOf<StompFrame>()
            if (rawText.isBlank()) return frames

            val rawFrames = rawText.split(NULL_CHAR)
            for (rawFrame in rawFrames) {
                val trimmed = rawFrame.trim { it == '\r' || it == '\n' || it == ' ' }
                if (trimmed.isEmpty()) continue

                val lines = trimmed.lines()
                if (lines.isEmpty()) continue

                val commandStr = lines[0].trim()
                val command = StompCommand.fromString(commandStr)
                if (command == StompCommand.UNKNOWN) continue

                val headers = mutableMapOf<String, String>()
                var bodyStartIndex = -1

                for (i in 1 until lines.size) {
                    val line = lines[i]
                    if (line.isEmpty() || line == "\r") {
                        bodyStartIndex = i + 1
                        break
                    }
                    val colonIndex = line.indexOf(':')
                    if (colonIndex != -1) {
                        val headerKey = unescapeHeader(line.substring(0, colonIndex).trim())
                        val headerValue = unescapeHeader(line.substring(colonIndex + 1).trim())
                        if (!headers.containsKey(headerKey)) {
                            headers[headerKey] = headerValue
                        }
                    }
                }

                val body = if (bodyStartIndex != -1 && bodyStartIndex < lines.size) {
                    lines.subList(bodyStartIndex, lines.size).joinToString("\n")
                } else null

                frames.add(StompFrame(command = command, headers = headers, body = body?.takeIf { it.isNotBlank() }))
            }
            return frames
        }

        private fun escapeHeader(value: String): String {
            return value
                .replace("\\", "\\\\")
                .replace(":", "\\c")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
        }

        private fun unescapeHeader(value: String): String {
            return value
                .replace("\\r", "\r")
                .replace("\\n", "\n")
                .replace("\\c", ":")
                .replace("\\\\", "\\")
        }
    }
}
