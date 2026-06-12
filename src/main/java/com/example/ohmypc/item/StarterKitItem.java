package com.example.ohmypc.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Стартовый набор — одноразовый предмет.
 *
 * Крафт: Ключ активации + Провод + Корпус (computer item)
 *
 * При использовании ПКМ — выдаёт полный комплект для начала работы:
 *   • Системный блок
 *   • CPU T1
 *   • RAM × 2
 *   • Жёсткий диск
 *   • Монитор
 *   • Сетевой кабель × 4
 *   • Белый флоппи-диск
 *   • Руководство пользователя
 */
public class StarterKitItem extends Item {

    public StarterKitItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResultHolder.success(player.getItemInHand(hand));

        ItemStack held = player.getItemInHand(hand);

        // Выдаём набор — сначала в инвентарь, при нехватке места — дроп
        give(player, level, new ItemStack(com.example.ohmypc.block.ModBlocks.COMPUTER.get()));
        give(player, level, new ItemStack(ModItems.CPU_TIER_1.get()));
        give(player, level, new ItemStack(ModItems.MEMORY.get(), 2));
        give(player, level, new ItemStack(ModItems.HARD_DRIVE.get()));
        give(player, level, new ItemStack(com.example.ohmypc.block.ModBlocks.MONITOR.get()));
        give(player, level, new ItemStack(com.example.ohmypc.block.ModBlocks.NETWORK_CABLE.get(), 4));
        give(player, level, new ItemStack(ModItems.FLOPPY_WHITE.get()));
        give(player, level, new ItemStack(ModItems.MANUAL.get()));

        player.sendSystemMessage(Component.literal(
                "§aStarter Kit opened! Check your inventory."));

        // Уничтожаем предмет
        held.shrink(1);
        return InteractionResultHolder.consume(held);
    }

    private void give(Player player, Level level, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            // Дроп у ног игрока если инвентарь полон
            ItemEntity drop = new ItemEntity(level,
                    player.getX(), player.getY(), player.getZ(), stack);
            drop.setPickUpDelay(0);
            level.addFreshEntity(drop);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Right-click to unpack"));
        tooltip.add(Component.literal("§8Contains: Computer, CPU, RAM, HDD,"));
        tooltip.add(Component.literal("§8Monitor, Cables, Floppy, Manual"));
        tooltip.add(Component.literal("§c⚠ One use only!"));
    }
}
