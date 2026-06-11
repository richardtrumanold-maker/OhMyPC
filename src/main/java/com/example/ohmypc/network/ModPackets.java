package com.example.ohmypc.network;

import com.example.ohmypc.Ohmypc;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.List;

public class ModPackets {

    private static final String PROTOCOL = "2";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Ohmypc.MOD_ID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private static int id = 0;

    public static void register() {
        // C→S
        CHANNEL.messageBuilder(BootPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(BootPacket::decode).encoder(BootPacket::encode)
                .consumerMainThread(BootPacket::handle).add();

        CHANNEL.messageBuilder(LuaCommandPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(LuaCommandPacket::decode).encoder(LuaCommandPacket::encode)
                .consumerMainThread(LuaCommandPacket::handle).add();

        // S→C
        CHANNEL.messageBuilder(TerminalSyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(TerminalSyncPacket::decode).encoder(TerminalSyncPacket::encode)
                .consumerMainThread(TerminalSyncPacket::handle).add();

        CHANNEL.messageBuilder(MonitorMediaPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(MonitorMediaPacket::decode).encoder(MonitorMediaPacket::encode)
                .consumerMainThread(MonitorMediaPacket::handle).add();

        CHANNEL.messageBuilder(MonitorTextPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(MonitorTextPacket::decode).encoder(MonitorTextPacket::encode)
                .consumerMainThread(MonitorTextPacket::handle).add();
    }

    // ── Клиент → сервер ───────────────────────────────────────────────────────
    public static void sendBootPacket(BlockPos pos, boolean boot) {
        CHANNEL.sendToServer(new BootPacket(pos, boot));
    }
    public static void sendLuaCommand(BlockPos pos, String command) {
        CHANNEL.sendToServer(new LuaCommandPacket(pos, command));
    }

    // ── Сервер → клиент (вызывать только с серверного потока) ─────────────────
    public static void sendMonitorMedia(BlockPos pos, String url, boolean video,
                                        ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new MonitorMediaPacket(pos, url, video));
    }
    public static void broadcastMonitorMedia(BlockPos pos, String url, boolean video,
                                              net.minecraft.server.level.ServerLevel level) {
        CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(
                () -> level.getChunkAt(pos)),
                new MonitorMediaPacket(pos, url, video));
    }
    public static void broadcastMonitorText(BlockPos pos, List<String> lines,
                                             net.minecraft.server.level.ServerLevel level) {
        CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(
                () -> level.getChunkAt(pos)),
                new MonitorTextPacket(pos, lines));
    }
}
