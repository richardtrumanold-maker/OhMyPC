package com.example.ohmypc.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Синхронизирует список строк терминала с клиентом */
public record TerminalSyncPacket(long pos, List<String> lines) {

    public static void encode(TerminalSyncPacket p, FriendlyByteBuf buf) {
        buf.writeLong(p.pos());
        buf.writeCollection(p.lines(), FriendlyByteBuf::writeUtf);
    }

    public static TerminalSyncPacket decode(FriendlyByteBuf buf) {
        long pos = buf.readLong();
        List<String> lines = buf.readList(FriendlyByteBuf::readUtf);
        return new TerminalSyncPacket(pos, lines);
    }

    public static void handle(TerminalSyncPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Клиентская сторона: обновить кеш терминала
            com.example.ohmypc.client.TerminalCache.update(p.pos(), p.lines());
        });
        ctx.get().setPacketHandled(true);
    }
}
