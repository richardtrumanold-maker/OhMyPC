package com.example.ohmypc.network;

import com.example.ohmypc.block.entity.MonitorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Сервер → Клиент: обновить URL медиа монитора.
 * Клиент применяет данные и запускает загрузку через ImageCache.
 */
public record MonitorMediaPacket(BlockPos pos, String url, boolean isVideo) {

    public static void encode(MonitorMediaPacket p, FriendlyByteBuf buf) {
        buf.writeBlockPos(p.pos());
        buf.writeUtf(p.url(), 512);
        buf.writeBoolean(p.isVideo());
    }

    public static MonitorMediaPacket decode(FriendlyByteBuf buf) {
        return new MonitorMediaPacket(buf.readBlockPos(), buf.readUtf(512), buf.readBoolean());
    }

    public static void handle(MonitorMediaPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var mc = Minecraft.getInstance();
            if (mc.level == null) return;
            BlockEntity be = mc.level.getBlockEntity(p.pos());
            if (be instanceof MonitorBlockEntity mbe) {
                mbe.setMediaUrl(p.url(), p.isVideo());
                // ImageCache начнёт загрузку при первом рендер-тике
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
