package com.example.ohmypc.network;

import com.example.ohmypc.block.entity.MonitorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/** Сервер → Клиент: синхронизировать текстовые строки монитора */
public record MonitorTextPacket(BlockPos pos, List<String> lines) {

    public static void encode(MonitorTextPacket p, FriendlyByteBuf buf) {
        buf.writeBlockPos(p.pos());
        buf.writeCollection(p.lines(), FriendlyByteBuf::writeUtf);
    }

    public static MonitorTextPacket decode(FriendlyByteBuf buf) {
        return new MonitorTextPacket(buf.readBlockPos(), buf.readList(FriendlyByteBuf::readUtf));
    }

    public static void handle(MonitorTextPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var mc = Minecraft.getInstance();
            if (mc.level == null) return;
            BlockEntity be = mc.level.getBlockEntity(p.pos());
            if (be instanceof MonitorBlockEntity mbe) {
                mbe.setLines(p.lines());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
