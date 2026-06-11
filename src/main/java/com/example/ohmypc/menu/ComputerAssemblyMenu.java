package com.example.ohmypc.menu;

import com.example.ohmypc.block.entity.ComputerBlockEntity;
import com.example.ohmypc.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * GUI сборки компьютера: 6 слотов (CPU, RAM×2, GPU, Storage, Network)
 * + кнопки Boot / Shutdown (отправляются пакетом).
 */
public class ComputerAssemblyMenu extends AbstractContainerMenu {

    private final ComputerBlockEntity blockEntity;
    private final Level level;

    // Конструктор с BlockPos — вызывается с сервера
    public ComputerAssemblyMenu(int id, Inventory playerInv, BlockPos pos, Level level) {
        super(ModMenus.COMPUTER_ASSEMBLY.get(), id);
        this.level = level;
        this.blockEntity = (ComputerBlockEntity) level.getBlockEntity(pos);

        IItemHandler handler = blockEntity.getInventory();

        // Слот 0: CPU
        this.addSlot(new FilteredSlot(handler, ComputerBlockEntity.SLOT_CPU, 62, 20,
                ModItems.CPU_TIER_1.get(), ModItems.CPU_TIER_2.get()));

        // Слот 1-2: RAM
        this.addSlot(new FilteredSlot(handler, ComputerBlockEntity.SLOT_RAM1, 26, 44,
                ModItems.MEMORY.get()));
        this.addSlot(new FilteredSlot(handler, ComputerBlockEntity.SLOT_RAM2, 44, 44,
                ModItems.MEMORY.get()));

        // Слот 3: GPU
        this.addSlot(new FilteredSlot(handler, ComputerBlockEntity.SLOT_GPU, 80, 44,
                ModItems.FLASH_MEMORY.get())); // placeholder

        // Слот 4: Storage
        this.addSlot(new FilteredSlot(handler, ComputerBlockEntity.SLOT_STORAGE, 62, 68,
                ModItems.HARD_DRIVE.get()));

        // Слот 5: Network
        this.addSlot(new FilteredSlot(handler, ComputerBlockEntity.SLOT_NETWORK, 26, 20,
                ModItems.NETWORK_CARD_BASIC.get(),
                ModItems.NETWORK_CARD_ADVANCED.get(),
                ModItems.NETWORK_CARD_ELITE.get(),
                ModItems.WIRELESS_MODEM.get(),
                ModItems.WIRELESS_MODEM_ADVANCED.get()));

        // Инвентарь игрока
        int startX = 8, startY = 102;
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, startX + col * 18, startY + row * 18));
        for (int col = 0; col < 9; col++)
            this.addSlot(new Slot(playerInv, col, startX + col * 18, startY + 58));
    }

    // Клиентский конструктор (без данных)
    public ComputerAssemblyMenu(int id, Inventory playerInv, net.minecraft.network.FriendlyByteBuf buf) {
        this(id, playerInv, buf.readBlockPos(), playerInv.player.level());
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
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < 6) {
                if (!this.moveItemStackTo(stack, 6, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(stack, 0, 6, false)) return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return result;
    }

    /** Слот с фильтрацией по типу предмета */
    private static class FilteredSlot extends SlotItemHandler {
        private final Item[] allowed;
        FilteredSlot(IItemHandler handler, int index, int x, int y, Item... allowed) {
            super(handler, index, x, y);
            this.allowed = allowed;
        }
        @Override public boolean mayPlace(ItemStack stack) {
            for (Item item : allowed) if (stack.is(item)) return true;
            return false;
        }
        @Override public int getMaxStackSize() { return 1; }
    }
}
