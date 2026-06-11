package com.example.ohmypc.projector;

import com.example.ohmypc.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CinemaProjectorBlockEntity extends BlockEntity {

    private final List<String> displayLines = new ArrayList<>();
    private static final int MAX_LINES = 30;

    private String  mediaUrl    = "";
    private boolean isVideo     = false;

    private int   distance  = 3;
    private float scaleW    = 3f;
    private float scaleH    = 2f;
    private float opacity   = 0.85f;

    private BlockPos connectedComputer = null;

    public CinemaProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CINEMA_PROJECTOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CinemaProjectorBlockEntity be) {
        // будущее: анимация голограммы
    }

    // ── Контент ──────────────────────────────────────────────────────────────
    public void setLines(List<String> lines) {
        displayLines.clear();
        displayLines.addAll(lines.subList(0, Math.min(lines.size(), MAX_LINES)));
        sync();
    }

    public void pushLine(String line) {
        displayLines.add(line);
        while (displayLines.size() > MAX_LINES) displayLines.remove(0);
        sync();
    }

    public void setMediaUrl(String url, boolean video) {
        this.mediaUrl = url;
        this.isVideo = video;
        sync();
    }

    public List<String> getDisplayLines() { return displayLines; }
    public String  getMediaUrl()          { return mediaUrl; }
    public boolean isVideo()              { return isVideo; }

    // ── Параметры ────────────────────────────────────────────────────────────
    public void setProjection(int dist, float w, float h, float op) {
        this.distance = Math.max(2, Math.min(16, dist));
        this.scaleW   = Math.max(1, Math.min(8, w));
        this.scaleH   = Math.max(1, Math.min(8, h));
        this.opacity  = Math.max(0.1f, Math.min(1f, op));
        sync();
    }

    public int   getDistance() { return distance; }
    public float getScaleW()   { return scaleW; }
    public float getScaleH()   { return scaleH; }
    public float getOpacity()  { return opacity; }

    // ── Алиасы для рендерера ─────────────────────────────────────────────────
    public float getProjectionDistance() { return distance; }
    public float getScreenWidth()        { return scaleW; }
    public float getScreenHeight()       { return scaleH; }
    public float getBeamAlpha()          { return opacity; }

    // ── Подключение ──────────────────────────────────────────────────────────
    public void connectTo(BlockPos computer) { this.connectedComputer = computer; sync(); }
    public void disconnect()                 { this.connectedComputer = null; sync(); }
    public boolean isConnected()             { return connectedComputer != null; }
    public BlockPos getConnectedComputer()   { return connectedComputer; }

    // ── Sync ─────────────────────────────────────────────────────────────────
    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide())
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    // ── NBT ──────────────────────────────────────────────────────────────────
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("mediaUrl", mediaUrl);
        tag.putBoolean("isVideo", isVideo);
        tag.putInt("distance",  distance);
        tag.putFloat("scaleW",  scaleW);
        tag.putFloat("scaleH",  scaleH);
        tag.putFloat("opacity", opacity);
        if (connectedComputer != null) tag.putLong("comp", connectedComputer.asLong());
        var lt = new net.minecraft.nbt.ListTag();
        for (String l : displayLines) lt.add(net.minecraft.nbt.StringTag.valueOf(l));
        tag.put("lines", lt);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        mediaUrl = tag.getString("mediaUrl");
        isVideo  = tag.getBoolean("isVideo");
        distance = tag.getInt("distance");
        if (distance < 2) distance = 3;
        scaleW   = tag.getFloat("scaleW");
        if (scaleW < 1) scaleW = 3;
        scaleH   = tag.getFloat("scaleH");
        if (scaleH < 1) scaleH = 2;
        opacity  = tag.getFloat("opacity");
        if (opacity < 0.1f) opacity = 0.85f;
        if (tag.contains("comp")) connectedComputer = BlockPos.of(tag.getLong("comp"));
        displayLines.clear();
        var lt = tag.getList("lines", 8);
        for (int i = 0; i < lt.size(); i++) displayLines.add(lt.getString(i));
    }
}
