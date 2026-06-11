package com.example.ohmypc.gui;

import com.example.ohmypc.menu.LuaTerminalMenu;
import com.example.ohmypc.network.ModPackets;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * Lua-терминал: тёмный фон, монохромный текст, поле ввода снизу.
 * Размер: 320×240 (чуть больше стандарта для удобства).
 */
public class LuaTerminalScreen extends AbstractContainerScreen<LuaTerminalMenu> {

    private EditBox inputBox;
    private static final int BG_COLOR   = 0xFF0A0A0A;
    private static final int TEXT_COLOR = 0xFF33FF33; // зелёный как OC
    private static final int LINE_HEIGHT = 9;
    private static final int VISIBLE_LINES = 24;

    public LuaTerminalScreen(LuaTerminalMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth  = 320;
        this.imageHeight = 240;
    }

    @Override
    protected void init() {
        super.init();
        int x = this.leftPos;
        int y = this.topPos;

        inputBox = new EditBox(this.font, x + 2, y + this.imageHeight - 14, this.imageWidth - 4, 12,
                Component.literal(""));
        inputBox.setMaxLength(256);
        inputBox.setBordered(true);
        inputBox.setFocus(true);
        this.addWidget(inputBox);
        this.setInitialFocus(inputBox);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Enter — отправить команду
        if (keyCode == 257 || keyCode == 335) { // ENTER / NUMPAD_ENTER
            String cmd = inputBox.getValue().trim();
            if (!cmd.isEmpty()) {
                ModPackets.sendLuaCommand(menu.getBlockEntity().getBlockPos(), cmd);
                inputBox.setValue("");
            }
            return true;
        }
        if (inputBox.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        return inputBox.charTyped(c, mods) || super.charTyped(c, mods);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
        int x = this.leftPos, y = this.topPos;
        // Фон
        g.fill(x, y, x + imageWidth, y + imageHeight, BG_COLOR);
        // Рамка
        g.renderOutline(x, y, imageWidth, imageHeight, 0xFF22AA22);
        // Линия над строкой ввода
        g.fill(x, y + imageHeight - 16, x + imageWidth, y + imageHeight - 15, 0xFF22AA22);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        // Заголовок
        g.drawString(this.font, "§2OhMyPC Terminal", 4, -10, 0x22AA22, false);

        // Линии терминала
        var be = menu.getBlockEntity();
        if (be == null) return;
        List<String> lines = be.getTerminalLines();
        int start = Math.max(0, lines.size() - VISIBLE_LINES);
        for (int i = 0; i < VISIBLE_LINES && (start + i) < lines.size(); i++) {
            String line = lines.get(start + i);
            g.drawString(this.font, line, 2, 4 + i * LINE_HEIGHT, 0x33FF33, false);
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        this.renderBackground(g);
        super.render(g, mx, my, partial);
        inputBox.render(g, mx, my, partial);
        // Курсор-подсказка
        g.drawString(this.font, ">", this.leftPos + 2, this.topPos + imageHeight - 12, 0x33FF33, false);
    }

    @Override public boolean isPauseScreen() { return false; }
}
