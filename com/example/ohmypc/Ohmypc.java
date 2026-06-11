package com.example.ohmypc;

import com.example.ohmypc.block.ModBlocks;
import com.example.ohmypc.block.entity.ModBlockEntities;
import com.example.ohmypc.client.render.CinemaProjectorRenderer;
import com.example.ohmypc.client.render.MonitorRenderer;
import com.example.ohmypc.gui.ComputerAssemblyScreen;
import com.example.ohmypc.gui.LuaTerminalScreen;
import com.example.ohmypc.item.ModCreativeTab;
import com.example.ohmypc.item.ModItems;
import com.example.ohmypc.menu.ModMenus;
import com.example.ohmypc.network.ModPackets;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;

@Mod(Ohmypc.MOD_ID)
public class Ohmypc {

    public static final String MOD_ID = "ohmypc";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Ohmypc() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(bus);
        ModBlocks.BLOCKS.register(bus);
        ModBlockEntities.BLOCK_ENTITIES.register(bus);
        ModCreativeTab.CREATIVE_TABS.register(bus);
        ModMenus.MENUS.register(bus);

        bus.addListener(this::commonSetup);
        bus.addListener(this::clientSetup);

        LOGGER.info("[OhMyPC] Initialised — version 2.3.0");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModPackets.register();
            // Создаём корневую папку floppy при старте
            try {
                Files.createDirectories(FMLPaths.GAMEDIR.get().resolve("floppy"));
                LOGGER.info("[OhMyPC] Floppy disk folder ready: {}/floppy/",
                        FMLPaths.GAMEDIR.get());
            } catch (IOException e) {
                LOGGER.error("[OhMyPC] Failed to create floppy folder", e);
            }
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // GUI
            MenuScreens.register(ModMenus.COMPUTER_ASSEMBLY.get(), ComputerAssemblyScreen::new);
            MenuScreens.register(ModMenus.LUA_TERMINAL.get(),      LuaTerminalScreen::new);

            // BEWLR рендереры
            BlockEntityRenderers.register(ModBlockEntities.MONITOR.get(),           MonitorRenderer::new);
            BlockEntityRenderers.register(ModBlockEntities.CINEMA_PROJECTOR.get(), CinemaProjectorRenderer::new);
        });
    }
}
