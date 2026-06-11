package com.example.ohmypc.menu;

import com.example.ohmypc.block.entity.ComputerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/** Lua-терминал — не имеет слотов, всё через текст */
public class LuaTerminalMenu extends AbstractContainerMenu {

    private final ComputerBlockEntity blockEntity;

    public LuaTerminalMenu(int id, Inventory playerInv, BlockPos pos) {
        super(ModMenus.LUA_TERMINAL.get(), id);
        this.blockEntity = (ComputerBlockEntity) playerInv.player.level().getBlockEntity(pos);
    }

    // Клиентский конструктор
    public LuaTerminalMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, buf.readBlockPos());
    }

    public ComputerBlockEntity getBlockEntity() { return blockEntity; }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null &&
               player.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5,
                                     blockEntity.getBlockPos().getY() + 0.5,
                                     blockEntity.getBlockPos().getZ() + 0.5) < 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
}
