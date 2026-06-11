package com.example.ohmypc.block.entity;

import com.example.ohmypc.filesystem.FloppyFileSystem;
import com.example.ohmypc.item.FloppyDiskItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Дисковод.
 * Слот 0 = флоппи-диск.
 * При вставке монтирует FloppyFileSystem в подключённый компьютер (/disk/).
 * При извлечении — размонтирует.
 */
public class DiskDriveBlockEntity extends BlockEntity {

    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override protected void onContentsChanged(int slot) {
            setChanged();
            updateMount();
        }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof FloppyDiskItem;
        }
        @Override public int getSlotLimit(int slot) { return 1; }
    };
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> inventory);

    // Подключённый компьютер
    private BlockPos connectedComputer = null;

    // Текущий смонтированный диск
    private FloppyFileSystem mounted = null;

    public DiskDriveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DISK_DRIVE.get(), pos, state);
    }

    // ── Маунт ────────────────────────────────────────────────────────────────
    private void updateMount() {
        ItemStack disk = inventory.getStackInSlot(0);
        if (disk.getItem() instanceof FloppyDiskItem floppy) {
            mounted = new FloppyFileSystem(floppy.getColorName());
        } else {
            mounted = null;
        }
        notifyComputer();
    }

    private void notifyComputer() {
        if (level == null || connectedComputer == null) return;
        if (level.getBlockEntity(connectedComputer) instanceof ComputerBlockEntity computer) {
            if (mounted != null) computer.mountFloppy(mounted);
            else                 computer.unmountFloppy();
        }
    }

    /** ПКМ — подключить/отключить компьютер */
    public void connectTo(BlockPos computerPos) {
        this.connectedComputer = computerPos;
        notifyComputer();
        setChanged();
    }

    public void disconnect() {
        if (connectedComputer != null && level != null &&
                level.getBlockEntity(connectedComputer) instanceof ComputerBlockEntity computer) {
            computer.unmountFloppy();
        }
        connectedComputer = null;
        setChanged();
    }

    public boolean hasDisk()   { return !inventory.getStackInSlot(0).isEmpty(); }
    public String  getDiskColor() {
        ItemStack s = inventory.getStackInSlot(0);
        return (s.getItem() instanceof FloppyDiskItem f) ? f.getColorName() : "none";
    }
    public FloppyFileSystem getMounted() { return mounted; }
    public ItemStackHandler getInventory() { return inventory; }

    // ── NBT ──────────────────────────────────────────────────────────────────
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inv", inventory.serializeNBT());
        if (connectedComputer != null) tag.putLong("comp", connectedComputer.asLong());
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("inv")) inventory.deserializeNBT(tag.getCompound("inv"));
        if (tag.contains("comp")) connectedComputer = BlockPos.of(tag.getLong("comp"));
        // Восстановить маунт
        ItemStack disk = inventory.getStackInSlot(0);
        if (disk.getItem() instanceof FloppyDiskItem f) {
            mounted = new FloppyFileSystem(f.getColorName());
        }
    }

    // ── Capabilities ─────────────────────────────────────────────────────────
    @Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return handler.cast();
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); handler.invalidate(); }
}
