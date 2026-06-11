package com.example.ohmypc.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NetworkCardItem extends Item {
    private final int range;
    private final int speed;

    public NetworkCardItem(Properties properties, int range, int speed) {
        super(properties);
        this.range = range;
        this.speed = speed;
    }

    public int getRange() {
        return range;
    }

    public int getSpeed() {
        return speed;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("Range: " + range + " blocks").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Speed: " + speed + " bytes/tick").withStyle(ChatFormatting.GRAY));
    }
}