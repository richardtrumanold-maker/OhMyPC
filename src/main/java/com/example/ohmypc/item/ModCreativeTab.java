package com.example.ohmypc.item;

import com.example.ohmypc.Ohmypc;
import com.example.ohmypc.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Ohmypc.MOD_ID);

    public static final RegistryObject<CreativeModeTab> OHMYPC_TAB =
            CREATIVE_TABS.register("ohmypc_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.ohmypc"))
                    .icon(() -> new ItemStack(ModBlocks.COMPUTER.get()))
                    .displayItems((params, output) -> {
                        // ── Руководство и стартовый набор ────────────────────
                        output.accept(ModItems.MANUAL.get());
                        output.accept(ModItems.STARTER_KIT.get());
                        output.accept(ModItems.ACTIVATION_KEY.get());
                        // ── Блоки ────────────────────────────────────────────
                        output.accept(ModBlocks.COMPUTER.get());
                        output.accept(ModBlocks.MONITOR.get());
                        output.accept(ModBlocks.NETWORK_CABLE.get());
                        output.accept(ModBlocks.DISK_DRIVE.get());
                        output.accept(ModBlocks.NETWORK_HUB.get());
                        output.accept(ModBlocks.CINEMA_PROJECTOR.get());
                        // ── CPU ──────────────────────────────────────────────
                        output.accept(ModItems.CPU_TIER_1.get());
                        output.accept(ModItems.CPU_TIER_2.get());
                        // ── RAM ──────────────────────────────────────────────
                        output.accept(ModItems.MEMORY.get());
                        // ── GPU ──────────────────────────────────────────────
                        output.accept(ModItems.VIDEO_CARD_T1.get());
                        output.accept(ModItems.VIDEO_CARD_T2.get());
                        // ── Storage ──────────────────────────────────────────
                        output.accept(ModItems.HARD_DRIVE.get());
                        output.accept(ModItems.FLASH_MEMORY.get());
                        // ── Network ──────────────────────────────────────────
                        output.accept(ModItems.NETWORK_CARD_BASIC.get());
                        output.accept(ModItems.NETWORK_CARD_ADVANCED.get());
                        output.accept(ModItems.NETWORK_CARD_ELITE.get());
                        output.accept(ModItems.WIRELESS_MODEM.get());
                        output.accept(ModItems.WIRELESS_MODEM_ADVANCED.get());
                        // ── Материалы ────────────────────────────────────────
                        output.accept(ModItems.TRANSISTOR.get());
                        output.accept(ModItems.CIRCUIT_BOARD.get());
                        output.accept(ModItems.BUS_CABLE.get());
                        // ── Черепашки ────────────────────────────────────────
                        output.accept(ModItems.TURTLE.get());
                        output.accept(ModItems.ADVANCED_TURTLE.get());
                        // ── Флоппи-диски (16 цветов) ─────────────────────────
                        output.accept(ModItems.FLOPPY_WHITE.get());
                        output.accept(ModItems.FLOPPY_ORANGE.get());
                        output.accept(ModItems.FLOPPY_MAGENTA.get());
                        output.accept(ModItems.FLOPPY_LIGHT_BLUE.get());
                        output.accept(ModItems.FLOPPY_YELLOW.get());
                        output.accept(ModItems.FLOPPY_LIME.get());
                        output.accept(ModItems.FLOPPY_PINK.get());
                        output.accept(ModItems.FLOPPY_GRAY.get());
                        output.accept(ModItems.FLOPPY_LIGHT_GRAY.get());
                        output.accept(ModItems.FLOPPY_CYAN.get());
                        output.accept(ModItems.FLOPPY_PURPLE.get());
                        output.accept(ModItems.FLOPPY_BLUE.get());
                        output.accept(ModItems.FLOPPY_BROWN.get());
                        output.accept(ModItems.FLOPPY_GREEN.get());
                        output.accept(ModItems.FLOPPY_RED.get());
                        output.accept(ModItems.FLOPPY_BLACK.get());
                    })
                    .build());
}
