package com.example.omnigo.core.websocket

enum class StompCommand {
    CONNECT,
    CONNECTED,
    SUBSCRIBE,
    UNSUBSCRIBE,
    SEND,
    MESSAGE,
    RECEIPT,
    ERROR,
    DISCONNECT,
    UNKNOWN;

    companion object {
        fun fromString(command: String): StompCommand {
            return entries.firstOrNull { it.name.equals(command.trim(), ignoreCase = true) } ?: UNKNOWN
        }
    }
}
