package com.example.ohmypc.client.render;

import com.example.ohmypc.block.MonitorBlock;
import com.example.ohmypc.block.entity.MonitorBlockEntity;
import com.example.ohmypc.client.image.ImageCache;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.List;

/**
 * BEWLR (Block Entity With Level Renderer) для MonitorBlockEntity.
 *
 * Что рисует:
 *  1. Если isMaster() → отрисовывает весь прямоугольник мультиблока
 *  2. Если mediaUrl не пуст → текстура из ImageCache поверх экрана
 *  3. Иначе → строки текста из getDisplayLines()
 *  4. Слейв-блоки → ничего не рисуют сами (мастер рисует за всех)
 *
 * Координатная система:
 *  Начало в worldPosition блока. PoseStack уже переведён в мировые координаты.
 *  Плоскость экрана — перпендикулярно FACING, смещена на 15/16 вперёд
 *  (чтобы чуть выступать перед геометрией блока).
 */
public class MonitorRenderer implements BlockEntityRenderer<MonitorBlockEntity> {

    // Немного выдвигаем плоскость вперёд чтобы не Z-файтилось с моделью
    private static final float SCREEN_DEPTH  = 15.01f / 16f;
    private static final float SCREEN_MARGIN = 0.5f / 16f;
    // Цвет фона экрана (тёмно-серый, полупрозрачный)
    private static final int   BG_COLOR      = 0xE0050505;
    private static final int   TEXT_COLOR    = 0xFF33FF33; // зелёный как OC
    private static final float TEXT_SCALE    = 0.45f / 16f; // масштаб шрифта в блок-единицах

    public MonitorRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(MonitorBlockEntity be, float partial, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {

        // Рисует только мастер
        if (!be.isMaster()) return;

        BlockState state = be.getLevel() != null
                ? be.getLevel().getBlockState(be.getBlockPos())
                : null;
        if (state == null) return;

        Direction facing = state.getValue(MonitorBlock.FACING);

        int totalW = be.getWidth();
        int totalH = be.getHeight();

        // === Настройка матрицы ===
        pose.pushPose();

        // Переводим в центр блока-мастера (мы уже в локальных координатах блока)
        // Поворачиваем так чтобы +X шёл вправо (по ширине) а +Y — вверх
        applyFacingTransform(pose, facing);

        // Размеры экрана в блок-единицах
        float sw = totalW;        // ширина в блоках
        float sh = totalH;        // высота в блоках

        String url = be.getMediaUrl();

        if (!url.isEmpty()) {
            renderImage(pose, buffers, url, sw, sh, light);
        } else {
            renderBackground(pose, buffers, sw, sh);
            renderText(pose, buffers, be.getDisplayLines(), sw, sh);
        }

        pose.popPose();
    }

    // ── Трансформация по направлению ──────────────────────────────────────────
    private void applyFacingTransform(PoseStack pose, Direction facing) {
        // Сдвигаем начало координат в угол блока, где экран «выходит»
        switch (facing) {
            case NORTH -> {
                pose.translate(1, 0, SCREEN_DEPTH);
                pose.mulPose(new Quaternionf().rotationY((float) Math.PI));
            }
            case SOUTH -> pose.translate(0, 0, 1 - SCREEN_DEPTH);
            case WEST  -> {
                pose.translate(SCREEN_DEPTH, 0, 1);
                pose.mulPose(new Quaternionf().rotationY((float)(-Math.PI / 2)));
            }
            case EAST  -> {
                pose.translate(1 - SCREEN_DEPTH, 0, 0);
                pose.mulPose(new Quaternionf().rotationY((float)(Math.PI / 2)));
            }
            default -> {}
        }
    }

    // ── Фон экрана ────────────────────────────────────────────────────────────
    private void renderBackground(PoseStack pose, MultiBufferSource buffers,
                                   float w, float h) {
        var buf = buffers.getBuffer(net.minecraft.client.renderer.RenderType.gui());
        Matrix4f mat = pose.last().pose();

        int a = (BG_COLOR >> 24) & 0xFF;
        int r = (BG_COLOR >> 16) & 0xFF;
        int g = (BG_COLOR >>  8) & 0xFF;
        int b =  BG_COLOR        & 0xFF;

        buf.vertex(mat,   0,   0, 0).color(r,g,b,a).uv(0,1).endVertex();
        buf.vertex(mat,   w,   0, 0).color(r,g,b,a).uv(1,1).endVertex();
        buf.vertex(mat,   w,   h, 0).color(r,g,b,a).uv(1,0).endVertex();
        buf.vertex(mat,   0,   h, 0).color(r,g,b,a).uv(0,0).endVertex();
    }

    // ── Текстовый рендер ──────────────────────────────────────────────────────
    private void renderText(PoseStack pose, MultiBufferSource buffers,
                             List<String> lines, float sw, float sh) {
        if (lines.isEmpty()) return;

        Font font = Minecraft.getInstance().font;
        float margin = SCREEN_MARGIN;

        pose.pushPose();
        pose.translate(margin, sh - margin, -0.001f); // начинаем с верхней строки
        pose.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE); // Y инвертирован

        int visibleLines = (int)((sh - 2 * margin) / (TEXT_SCALE * 10));
        int start = Math.max(0, lines.size() - visibleLines);

        for (int i = start; i < lines.size(); i++) {
            font.drawInBatch(lines.get(i), 0, (i - start) * 10f, TEXT_COLOR,
                    false, pose.last().pose(), buffers,
                    Font.DisplayMode.NORMAL, 0, 0xF000F0);
        }
        pose.popPose();
    }

    // ── Изображение ──────────────────────────────────────────────────────────
    private void renderImage(PoseStack pose, MultiBufferSource buffers,
                              String url, float sw, float sh, int light) {
        ImageCache.Entry entry = ImageCache.get(url);

        if (entry == null) {
            // Ещё грузится — рисуем «загрузка»
            renderBackground(pose, buffers, sw, sh);
            renderCenteredText(pose, buffers, "§7Loading...", sw, sh);
            return;
        }
        if (entry.state() == ImageCache.State.ERROR) {
            renderBackground(pose, buffers, sw, sh);
            renderCenteredText(pose, buffers, "§cError: " + entry.error(), sw, sh);
            return;
        }
        if (entry.state() == ImageCache.State.LOADING) {
            renderBackground(pose, buffers, sw, sh);
            renderCenteredText(pose, buffers, "§eLoading...", sw, sh);
            return;
        }

        // READY — рисуем текстуру
        ResourceLocation tex = entry.location();
        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Используем immediate-буфер для текстур
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        Matrix4f mat = pose.last().pose();

        buf.vertex(mat, 0,  0,  0).uv(0, 1).endVertex();
        buf.vertex(mat, sw, 0,  0).uv(1, 1).endVertex();
        buf.vertex(mat, sw, sh, 0).uv(1, 0).endVertex();
        buf.vertex(mat, 0,  sh, 0).uv(0, 0).endVertex();

        Tesselator.getInstance().end();
        RenderSystem.disableBlend();
    }

    /** Текст по центру экрана */
    private void renderCenteredText(PoseStack pose, MultiBufferSource buffers,
                                     String text, float sw, float sh) {
        Font font = Minecraft.getInstance().font;
        pose.pushPose();
        float cx = sw / 2f;
        float cy = sh / 2f;
        pose.translate(cx, cy, -0.001f);
        pose.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
        float tw = font.width(text);
        font.drawInBatch(text, -tw / 2f, -5f, 0xFFAAAAAA,
                false, pose.last().pose(), buffers,
                Font.DisplayMode.NORMAL, 0, 0xF000F0);
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(MonitorBlockEntity be) {
        // Мультиблоки могут выходить за frustum одного блока
        return be.isMaster() && (be.getWidth() > 1 || be.getHeight() > 1);
    }

    @Override
    public int getViewDistance() { return 128; }
}
