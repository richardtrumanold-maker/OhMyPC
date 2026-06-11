package com.example.ohmypc.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WirelessModemItem extends Item {
    private static final String TAG_CHANNEL = "ModemChannel";
    private static final String TAG_ACTIVE = "ModemActive";

    public WirelessModemItem(Properties properties) {
        super(properties);
    }

    public static void setChannel(ItemStack stack, int channel) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_CHANNEL, Math.max(1, Math.min(65535, channel)));
    }

    public static int getChannel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_CHANNEL)) {
            return tag.getInt(TAG_CHANNEL);
        }
        return 1;
    }

    public static void setActive(ItemStack stack, boolean active) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(TAG_ACTIVE, active);
    }

    public static boolean isActive(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_ACTIVE)) {
            return tag.getBoolean(TAG_ACTIVE);
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("Channel: " + getChannel(stack)).withStyle(ChatFormatting.AQUA));
        if (isActive(stack)) {
            tooltip.add(Component.literal("Active").withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.literal("Inactive").withStyle(ChatFormatting.RED));
        }
    }
}