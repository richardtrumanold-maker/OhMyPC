package com.example.ohmypc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Экран гайд-бука OhMyPC.
 *
 * Layout (360×240):
 *  ┌──────────┬──────────────────────┐
 *  │  Боковая │  Заголовок страницы  │
 *  │ панель   ├──────────────────────┤
 *  │ катего-  │  Текст страницы      │
 *  │ рий      │  (скроллинг)         │
 *  │          ├──────────────────────┤
 *  │          │  ← Пред  След →      │
 *  └──────────┴──────────────────────┘
 */
public class ManualScreen extends Screen {

    // Размеры GUI
    private static final int GUI_W = 360;
    private static final int GUI_H = 230;
    private static final int SIDEBAR_W = 90;
    private static final int PADDING = 8;
    private static final int LINE_H = 10;

    // Цвета
    private static final int COL_BG       = 0xFF1A1A1A;
    private static final int COL_SIDEBAR  = 0xFF111111;
    private static final int COL_BORDER   = 0xFF2A6A2A;
    private static final int COL_HEADER   = 0xFF0D2A0D;
    private static final int COL_SEL      = 0xFF1D4D1D;
    private static final int COL_TEXT     = 0xFFDDDDDD;
    private static final int COL_DIM      = 0xFF888888;

    private int selectedCat  = 0;
    private int selectedPage = 0;
    private int scrollOffset = 0;

    private int guiLeft, guiTop;

    private final List<ManualContent.Category> cats = ManualContent.CATEGORIES;

    public ManualScreen() {
        super(Component.literal("OhMyPC Manual"));
    }

    @Override
    protected void init() {
        super.init();
        guiLeft = (this.width  - GUI_W) / 2;
        guiTop  = (this.height - GUI_H) / 2;
        rebuildButtons();
    }

    private void rebuildButtons() {
        clearWidgets();
        int bx = guiLeft + SIDEBAR_W + PADDING;
        int by = guiTop  + GUI_H - 22;
        int bw = (GUI_W - SIDEBAR_W - PADDING * 3) / 2;

        // Навигация по страницам
        addRenderableWidget(Button.builder(Component.literal("◀ Пред"), btn -> prevPage())
                .bounds(bx, by, bw, 16).build());
        addRenderableWidget(Button.builder(Component.literal("След ▶"), btn -> nextPage())
                .bounds(bx + bw + PADDING, by, bw, 16).build());

        // Кнопки категорий в сайдбаре
        int catY = guiTop + 24;
        for (int i = 0; i < cats.size(); i++) {
            final int idx = i;
            ManualContent.Category cat = cats.get(i);
            addRenderableWidget(Button.builder(
                    Component.literal(stripColor(cat.title()).substring(0, Math.min(stripColor(cat.title()).length(), 10))),
                    btn -> { selectedCat = idx; selectedPage = 0; scrollOffset = 0; rebuildButtons(); })
                    .bounds(guiLeft + 2, catY + i * 18, SIDEBAR_W - 4, 16).build());
        }

        // Кнопки страниц текущей категории
        if (selectedCat < cats.size()) {
            var pages = cats.get(selectedCat).pages();
            int px = guiLeft + SIDEBAR_W + PADDING;
            int py = guiTop + 20;
            for (int i = 0; i < Math.min(pages.size(), 6); i++) {
                final int pi = i;
                addRenderableWidget(Button.builder(Component.literal(String.valueOf(i + 1)),
                        btn -> { selectedPage = pi; scrollOffset = 0; })
                        .bounds(px + i * 18, py, 16, 12).build());
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g);

        // Основной фон
        g.fill(guiLeft, guiTop, guiLeft + GUI_W, guiTop + GUI_H, COL_BG);
        // Сайдбар
        g.fill(guiLeft, guiTop, guiLeft + SIDEBAR_W, guiTop + GUI_H, COL_SIDEBAR);
        // Рамка
        g.renderOutline(guiLeft, guiTop, GUI_W, GUI_H, COL_BORDER);
        // Разделитель сайдбара
        g.fill(guiLeft + SIDEBAR_W, guiTop, guiLeft + SIDEBAR_W + 1, guiTop + GUI_H, COL_BORDER);

        // Заголовок сайдбара
        g.fill(guiLeft, guiTop, guiLeft + SIDEBAR_W, guiTop + 18, COL_HEADER);
        g.drawString(font, "§a OhMyPC", guiLeft + 4, guiTop + 5, 0xFFFFFF, false);

        // Выделение активной категории
        if (selectedCat < cats.size()) {
            g.fill(guiLeft + 2, guiTop + 24 + selectedCat * 18 - 1,
                   guiLeft + SIDEBAR_W - 2, guiTop + 24 + selectedCat * 18 + 15,
                   COL_SEL);
        }

        // Контент страницы
        renderPageContent(g);

        super.render(g, mx, my, partial);
    }

    private void renderPageContent(GuiGraphics g) {
        if (selectedCat >= cats.size()) return;
        var cat   = cats.get(selectedCat);
        if (selectedPage >= cat.pages().size()) return;
        var page  = cat.pages().get(selectedPage);

        int cx = guiLeft + SIDEBAR_W + PADDING;
        int cy = guiTop  + PADDING;

        // Заголовок контент-области
        g.fill(cx - PADDING, cy, guiLeft + GUI_W, cy + 16, COL_HEADER);
        g.drawString(font, cat.title() + " §7/ §f" + page.title(),
                cx, cy + 4, 0xFFFFFF, false);

        // Страница X/N
        String pageNum = (selectedPage + 1) + "/" + cat.pages().size();
        g.drawString(font, pageNum, guiLeft + GUI_W - font.width(pageNum) - PADDING, cy + 4, 0x888888, false);

        // Линии текста с учётом скроллинга
        int textTop = cy + 20;
        int textBot = guiTop + GUI_H - 28;
        int maxVisible = (textBot - textTop) / LINE_H;

        var lines = page.lines();
        int total = lines.size();
        int startLine = Math.max(0, Math.min(scrollOffset, total - maxVisible));

        // Ножка скроллбара
        if (total > maxVisible) {
            int sbH = textBot - textTop;
            int thumbH = Math.max(10, sbH * maxVisible / total);
            int thumbY = textTop + (sbH - thumbH) * startLine / Math.max(1, total - maxVisible);
            g.fill(guiLeft + GUI_W - 4, textTop, guiLeft + GUI_W - 2, textBot, 0xFF222222);
            g.fill(guiLeft + GUI_W - 4, thumbY, guiLeft + GUI_W - 2, thumbY + thumbH, COL_BORDER);
        }

        // Текст
        for (int i = 0; i < maxVisible && (startLine + i) < total; i++) {
            String line = lines.get(startLine + i);
            g.drawString(font, line, cx, textTop + i * LINE_H, COL_TEXT, false);
        }

        // Рецепт если есть
        if (page.recipe() != null && lines.size() < maxVisible + startLine + 1) {
            int ry = textTop + (Math.min(total, maxVisible) - startLine) * LINE_H + 2;
            if (ry < textBot - 20) {
                g.drawString(font, "§8Рецепт:", cx, ry, COL_DIM, false);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        scrollOffset = Math.max(0, scrollOffset - (int) delta);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scan, int mods) {
        if (keyCode == 256) { onClose(); return true; } // ESC
        if (keyCode == 265) { scrollOffset = Math.max(0, scrollOffset - 1); return true; } // UP
        if (keyCode == 264) { scrollOffset++; return true; } // DOWN
        if (keyCode == 263) { prevPage(); return true; } // LEFT
        if (keyCode == 262) { nextPage(); return true; } // RIGHT
        return super.keyPressed(keyCode, scan, mods);
    }

    private void nextPage() {
        if (selectedCat >= cats.size()) return;
        var pages = cats.get(selectedCat).pages();
        if (selectedPage < pages.size() - 1) {
            selectedPage++;
            scrollOffset = 0;
        } else if (selectedCat < cats.size() - 1) {
            selectedCat++;
            selectedPage = 0;
            scrollOffset = 0;
        }
        rebuildButtons();
    }

    private void prevPage() {
        if (selectedPage > 0) {
            selectedPage--;
            scrollOffset = 0;
        } else if (selectedCat > 0) {
            selectedCat--;
            selectedPage = cats.get(selectedCat).pages().size() - 1;
            scrollOffset = 0;
        }
        rebuildButtons();
    }

    private static String stripColor(String s) {
        return s.replaceAll("§.", "");
    }

    @Override public boolean isPauseScreen() { return false; }
}
