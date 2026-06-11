package com.example.ohmypc.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class TurtleEntity extends Mob {
    private final ItemStack[] inventory = new ItemStack[16];
    private int fuelLevel = 0;

    protected TurtleEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    public boolean moveForward() {
        if (!consumeFuel(1)) return false;
        return true;
    }

    public boolean moveBack() {
        if (!consumeFuel(1)) return false;
        return true;
    }

    public boolean moveUp() {
        if (!consumeFuel(1)) return false;
        return true;
    }

    public boolean moveDown() {
        if (!consumeFuel(1)) return false;
        return true;
    }

    public boolean turnLeft() {
        return true;
    }

    public boolean turnRight() {
        return true;
    }

    public boolean dig() {
        if (!consumeFuel(1)) return false;
        BlockPos targetPos = this.blockPosition().relative(this.getDirection());
        this.level().destroyBlock(targetPos, true);
        return true;
    }

    public boolean place() {
        if (!consumeFuel(1)) return false;
        for (int i = 0; i < inventory.length; i++) {
            ItemStack stack = inventory[i];
            if (!stack.isEmpty() && stack.getItem() instanceof net.minecraft.world.item.BlockItem) {
                return true;
            }
        }
        return false;
    }

    public int getFuelLevel() {
        return fuelLevel;
    }

    public boolean consumeFuel(int amount) {
        if (fuelLevel >= amount) {
            fuelLevel -= amount;
            return true;
        }
        for (int i = 0; i < inventory.length; i++) {
            ItemStack stack = inventory[i];
            if (!stack.isEmpty() && isFuel(stack)) {
                fuelLevel += getFuelValue(stack);
                stack.shrink(1);
                return consumeFuel(amount);
            }
        }
        return false;
    }

    private boolean isFuel(ItemStack stack) {
        return stack.getItem() == net.minecraft.world.item.Items.COAL ||
               stack.getItem() == net.minecraft.world.item.Items.CHARCOAL;
    }

    private int getFuelValue(ItemStack stack) {
        if (stack.getItem() == net.minecraft.world.item.Items.COAL) return 80;
        if (stack.getItem() == net.minecraft.world.item.Items.CHARCOAL) return 80;
        return 0;
    }

    public String inspect() {
        BlockPos targetPos = this.blockPosition().relative(this.getDirection());
        Block block = this.level().getBlockState(targetPos).getBlock();
        return block.getName().getString();
    }
}