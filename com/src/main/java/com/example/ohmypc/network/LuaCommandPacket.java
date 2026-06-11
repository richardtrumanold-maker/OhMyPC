package com.example.ohmypc.network;

import com.example.ohmypc.block.entity.ComputerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record LuaCommandPacket(BlockPos pos, String command) {

    public static void encode(LuaCommandPacket p, FriendlyByteBuf buf) {
        buf.writeBlockPos(p.pos());
        buf.writeUtf(p.command(), 512);
    }

    public static LuaCommandPacket decode(FriendlyByteBuf buf) {
        return new LuaCommandPacket(buf.readBlockPos(), buf.readUtf(512));
    }

    public static void handle(LuaCommandPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            BlockEntity be = player.level().getBlockEntity(p.pos());
            if (be instanceof ComputerBlockEntity cbe) {
                cbe.executeCommand(p.command());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
