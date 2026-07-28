package com.example.ohmypc.item;

import com.example.ohmypc.block.ModBlocks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class StarterKitItem extends Item {

    public StarterKitItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            ItemStack stack = player.getItemInHand(hand);

            give(player, level, new ItemStack(ModBlocks.COMPUTER.get()));
            give(player, level, new ItemStack(ModItems.CPU_TIER_1.get()));
            give(player, level, new ItemStack(ModItems.MEMORY.get()));
            give(player, level, new ItemStack(ModItems.VIDEO_CARD_T1.get()));
            give(player, level, new ItemStack(ModBlocks.MONITOR.get()));
            give(player, level, new ItemStack(ModBlocks.NETWORK_CABLE.get(), 4));
            give(player, level, new ItemStack(ModItems.FLOPPY_WHITE.get()));
            give(player, level, new ItemStack(ModBlocks.DISK_DRIVE.get()));

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    private void give(Player player, Level level, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
