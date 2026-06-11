package com.example.ohmypc.client.render;

import com.example.ohmypc.client.image.ImageCache;
import com.example.ohmypc.projector.CinemaProjectorBlock;
import com.example.ohmypc.projector.CinemaProjectorBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.List;

/**
 * Рендерер кино-проектора.
 *
 * Визуально:
 *  1. Конус света от линзы проектора до экрана
 *  2. Изображение / текст на поверхности экрана
 *  3. Лёгкое свечение вокруг экрана (halo)
 *
 * Отличие от голограммы: тёплый жёлтый свет, проекция НА поверхность,
 * никакого синего sci-fi эффекта.
 */
public class CinemaProjectorRenderer implements BlockEntityRenderer<CinemaProjectorBlockEntity> {

    private static final float TEXT_SCALE = 0.045f / 16f;

    // Тёплый свет — как у плёночного проектора
    private static final int BEAM_R = 255, BEAM_G = 245, BEAM_B = 210;
    private static final int SCREEN_R = 255, SCREEN_G = 250, SCREEN_B = 230;

    public CinemaProjectorRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(CinemaProjectorBlockEntity be, float partial, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {

        BlockState state = be.getLevel() != null
                ? be.getLevel().getBlockState(be.getBlockPos()) : null;
        if (state == null) return;

        Direction facing;
        try { facing = state.getValue(CinemaProjectorBlock.FACING); }
        catch (Exception e) { facing = Direction.NORTH; }

        float dist = be.getProjectionDistance();
        float sw   = be.getScreenWidth();
        float sh   = be.getScreenHeight();

        pose.pushPose();
        applyFacingTransform(pose, facing);

        // 1. Конус света (луч проектора)
        renderBeam(pose, buffers, dist, sw, sh, be.getBeamAlpha());

        // 2. Экран на поверхности на расстоянии dist
        pose.pushPose();
        pose.translate(-sw / 2f, -sh / 2f, dist);

        String url = be.getMediaUrl();
        if (!url.isEmpty()) {
            renderProjectedImage(pose, buffers, url, sw, sh);
        } else {
            renderProjectedBg(pose, buffers, sw, sh);
            renderProjectedText(pose, buffers, be.getDisplayLines(), sw, sh);
        }
        // Свечение по краям экрана
        renderScreenGlow(pose, buffers, sw, sh);

        pose.popPose();
        pose.popPose();
    }

    // ── Трансформация по направлению ─────────────────────────────────────────
    private void applyFacingTransform(PoseStack pose, Direction facing) {
        pose.translate(0.5, 0.5, 0.5); // центр блока
        switch (facing) {
            case NORTH -> {
                pose.mulPose(new Quaternionf().rotationY((float) Math.PI));
                pose.translate(0, 0, 0.5);
            }
            case SOUTH -> pose.translate(0, 0, -0.5);
            case EAST  -> {
                pose.mulPose(new Quaternionf().rotationY((float)(-Math.PI / 2)));
                pose.translate(0, 0, -0.5);
            }
            case WEST  -> {
                pose.mulPose(new Quaternionf().rotationY((float)(Math.PI / 2)));
                pose.translate(0, 0, -0.5);
            }
            default -> {}
        }
    }

    // ── Конус света ──────────────────────────────────────────────────────────
    private void renderBeam(PoseStack pose, MultiBufferSource buffers,
                             float dist, float sw, float sh, float alpha) {
        var buf = buffers.getBuffer(RenderType.translucent());
        Matrix4f mat = pose.last().pose();

        // Полуразмеры экрана и источника
        float ex = sw / 2f, ey = sh / 2f;
        float sx = 0.06f,   sy = 0.06f; // маленькое отверстие линзы

        int a = (int)(alpha * 35);
        if (a <= 0) return;

        // Верхняя грань конуса
        quad(buf, mat, -sx, sy, 0,  sx, sy, 0,  ex, ey, dist,  -ex, ey, dist,
                BEAM_R, BEAM_G, BEAM_B, a);
        // Нижняя
        quad(buf, mat, -sx, -sy, 0,  sx, -sy, 0,  ex, -ey, dist,  -ex, -ey, dist,
                BEAM_R, BEAM_G, BEAM_B, a);
        // Левая
        quad(buf, mat, -sx, -sy, 0,  -sx, sy, 0,  -ex, ey, dist,  -ex, -ey, dist,
                BEAM_R, BEAM_G, BEAM_B, a);
        // Правая
        quad(buf, mat, sx, -sy, 0,  sx, sy, 0,  ex, ey, dist,  ex, -ey, dist,
                BEAM_R, BEAM_G, BEAM_B, a);
    }

    // ── Фон экрана ───────────────────────────────────────────────────────────
    private void renderProjectedBg(PoseStack pose, MultiBufferSource buffers,
                                    float w, float h) {
        var buf = buffers.getBuffer(RenderType.translucent());
        Matrix4f mat = pose.last().pose();
        quad(buf, mat, 0,0,0,  w,0,0,  w,h,0,  0,h,0,
                10, 10, 12, 200);
    }

    // ── Текст ────────────────────────────────────────────────────────────────
    private void renderProjectedText(PoseStack pose, MultiBufferSource buffers,
                                      List<String> lines, float sw, float sh) {
        if (lines.isEmpty()) return;
        Font font = Minecraft.getInstance().font;

        pose.pushPose();
        pose.translate(0.05f, sh - 0.1f, -0.002f);
        pose.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);

        int maxVis = (int)((sh - 0.15f) / (TEXT_SCALE * 11));
        int start = Math.max(0, lines.size() - maxVis);
        for (int i = start; i < lines.size(); i++) {
            font.drawInBatch(lines.get(i), 0, (i - start) * 11f,
                    0xFFFFFFDD, false, pose.last().pose(),
                    buffers, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        }
        pose.popPose();
    }

    // ── Изображение ──────────────────────────────────────────────────────────
    private void renderProjectedImage(PoseStack pose, MultiBufferSource buffers,
                                       String url, float sw, float sh) {
        ImageCache.Entry entry = ImageCache.get(url);

        if (entry == null || entry.state() == ImageCache.State.LOADING) {
            renderProjectedBg(pose, buffers, sw, sh);
            renderCenteredText(pose, buffers, "§eLoading...", sw, sh);
            return;
        }
        if (entry.state() == ImageCache.State.ERROR) {
            renderProjectedBg(pose, buffers, sw, sh);
            renderCenteredText(pose, buffers, "§cError: " + entry.error(), sw, sh);
            return;
        }

        // Рисуем изображение с тёплым тонированием (тёплый свет проектора)
        ResourceLocation tex = entry.location();
        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        Matrix4f mat = pose.last().pose();

        // Тёплый оттенок проекции
        buf.vertex(mat, 0,  0,  -0.001f).uv(0,1).color(SCREEN_R,SCREEN_G,SCREEN_B,250).endVertex();
        buf.vertex(mat, sw, 0,  -0.001f).uv(1,1).color(SCREEN_R,SCREEN_G,SCREEN_B,250).endVertex();
        buf.vertex(mat, sw, sh, -0.001f).uv(1,0).color(SCREEN_R,SCREEN_G,SCREEN_B,250).endVertex();
        buf.vertex(mat, 0,  sh, -0.001f).uv(0,0).color(SCREEN_R,SCREEN_G,SCREEN_B,250).endVertex();

        Tesselator.getInstance().end();
        RenderSystem.disableBlend();
    }

    // ── Свечение краёв экрана ─────────────────────────────────────────────────
    private void renderScreenGlow(PoseStack pose, MultiBufferSource buffers,
                                   float w, float h) {
        var buf = buffers.getBuffer(RenderType.translucent());
        Matrix4f mat = pose.last().pose();
        float g = 0.15f; // толщина свечения
        int ar = 255, ag = 240, ab = 180, aa = 60;

        // Верхняя полоса
        quad(buf,mat, -g,-g,-0.003f,  w+g,-g,-0.003f,  w+g,0,-0.003f,  -g,0,-0.003f, ar,ag,ab,aa);
        // Нижняя
        quad(buf,mat, -g,h,-0.003f,  w+g,h,-0.003f,  w+g,h+g,-0.003f,  -g,h+g,-0.003f, ar,ag,ab,aa);
        // Левая
        quad(buf,mat, -g,0,-0.003f,  0,0,-0.003f,  0,h,-0.003f,  -g,h,-0.003f, ar,ag,ab,aa);
        // Правая
        quad(buf,mat, w,0,-0.003f,  w+g,0,-0.003f,  w+g,h,-0.003f,  w,h,-0.003f, ar,ag,ab,aa);
    }

    // ── Утилиты ───────────────────────────────────────────────────────────────
    private void quad(VertexConsumer buf, Matrix4f mat,
                       float x1,float y1,float z1,
                       float x2,float y2,float z2,
                       float x3,float y3,float z3,
                       float x4,float y4,float z4,
                       int r, int g, int b, int a) {
        buf.vertex(mat,x1,y1,z1).color(r,g,b,a).uv(0,0).overlayCoords(0).uv2(0xF000F0).normal(0,0,1).endVertex();
        buf.vertex(mat,x2,y2,z2).color(r,g,b,a).uv(1,0).overlayCoords(0).uv2(0xF000F0).normal(0,0,1).endVertex();
        buf.vertex(mat,x3,y3,z3).color(r,g,b,a).uv(1,1).overlayCoords(0).uv2(0xF000F0).normal(0,0,1).endVertex();
        buf.vertex(mat,x4,y4,z4).color(r,g,b,a).uv(0,1).overlayCoords(0).uv2(0xF000F0).normal(0,0,1).endVertex();
    }

    private void renderCenteredText(PoseStack pose, MultiBufferSource buffers,
                                     String text, float w, float h) {
        Font font = Minecraft.getInstance().font;
        pose.pushPose();
        pose.translate(w/2f, h/2f, -0.002f);
        pose.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
        float tw = font.width(text);
        font.drawInBatch(text, -tw/2f, -5f, 0xFFFFEEAA, false, pose.last().pose(),
                buffers, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        pose.popPose();
    }

    @Override public boolean shouldRenderOffScreen(CinemaProjectorBlockEntity be) { return true; }
    @Override public int getViewDistance() { return 96; }
}
