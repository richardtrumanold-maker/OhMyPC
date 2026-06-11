package com.example.ohmypc.gui;

import com.example.ohmypc.Ohmypc;
import com.example.ohmypc.block.entity.ComputerBlockEntity;
import com.example.ohmypc.menu.ComputerAssemblyMenu;
import com.example.ohmypc.network.ModPackets;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ComputerAssemblyScreen extends AbstractContainerScreen<ComputerAssemblyMenu> {

    private static final ResourceLocation BG =
            new ResourceLocation(Ohmypc.MOD_ID, "textures/gui/computer_assembly.png");

    // Размер GUI: 176×196
    public ComputerAssemblyScreen(ComputerAssemblyMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth  = 176;
        this.imageHeight = 196;
    }

    @Override
    protected void init() {
        super.init();
        int bx = this.leftPos + 8;
        int by = this.topPos  + 152;

        // Кнопка Boot
        this.addRenderableWidget(Button.builder(Component.literal("Boot"),
                btn -> ModPackets.sendBootPacket(menu.getBlockEntity().getBlockPos(), true))
                .bounds(bx, by, 76, 18).build());

        // Кнопка Shutdown
        this.addRenderableWidget(Button.builder(Component.literal("Shutdown"),
                btn -> ModPackets.sendBootPacket(menu.getBlockEntity().getBlockPos(), false))
                .bounds(bx + 82, by, 76, 18).build());
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
        RenderSystem.setShaderTexture(0, BG);
        int x = (this.width  - this.imageWidth)  / 2;
        int y = (this.height - this.imageHeight) / 2;
        g.blit(BG, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        // Заголовок
        g.drawString(this.font, this.title, 8, 6, 0x404040, false);
        // Состояние
        ComputerBlockEntity be = menu.getBlockEntity();
        if (be != null) {
            String status = be.isPowered() ? "§aONLINE" : "§cOFFLINE";
            g.drawString(this.font, status, 8, 140, 0xFFFFFF, false);
            String ready = be.canBoot() ? "§aReady to boot" : "§eNeed CPU + RAM";
            g.drawString(this.font, ready, 60, 140, 0xFFFFFF, false);
        }
        // Подписи слотов
        g.drawString(this.font, "CPU",     50, 10, 0x808080, false);
        g.drawString(this.font, "NET",     14, 10, 0x808080, false);
        g.drawString(this.font, "RAM",     14, 34, 0x808080, false);
        g.drawString(this.font, "GPU",     68, 34, 0x808080, false);
        g.drawString(this.font, "Storage", 50, 58, 0x808080, false);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        this.renderBackground(g);
        super.render(g, mx, my, partial);
        this.renderTooltip(g, mx, my);
    }

    @Override public boolean isPauseScreen() { return false; }
}
