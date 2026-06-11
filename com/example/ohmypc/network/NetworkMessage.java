package com.example.ohmypc.network;

/** Одно сообщение в шине NetworkBus */
public record NetworkMessage(
        String from,
        String to,
        String channel,
        String message,
        long timestamp
) {}
