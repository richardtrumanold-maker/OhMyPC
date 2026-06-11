package com.example.ohmypc.network;

import com.example.ohmypc.block.entity.ComputerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record BootPacket(BlockPos pos, boolean boot) {

    public static void encode(BootPacket p, FriendlyByteBuf buf) {
        buf.writeBlockPos(p.pos());
        buf.writeBoolean(p.boot());
    }

    public static BootPacket decode(FriendlyByteBuf buf) {
        return new BootPacket(buf.readBlockPos(), buf.readBoolean());
    }

    public static void handle(BootPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            BlockEntity be = player.level().getBlockEntity(p.pos());
            if (be instanceof ComputerBlockEntity cbe) {
                if (p.boot()) cbe.boot();
                else          cbe.shutdown();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
