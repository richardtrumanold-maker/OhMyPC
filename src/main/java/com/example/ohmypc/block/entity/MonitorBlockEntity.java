package com.example.ohmypc.block.entity;

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

/**
 * MonitorBlockEntity v2 — поддержка мультиблока, медиа и текста.
 *
 * Роли:
 *  MASTER  — рисует весь мультиблок, хранит контент
 *  SLAVE   — знает мастера, сам ничего не рисует
 *  SINGLE  — 1×1, сам себе мастер
 */
public class MonitorBlockEntity extends BlockEntity {

    // ── Мультиблок ──────────────────────────────────────────────────────────
    private BlockPos masterPos   = null;   // null = сам мастер
    private int      width       = 1;
    private int      height      = 1;
    private boolean  isMaster    = true;
    private int      localX      = 0;     // координата этого блока внутри мультиблока
    private int      localY      = 0;

    // ── Подключение к компьютеру ─────────────────────────────────────────────
    private BlockPos connectedComputer = null;

    // ── Контент (только у мастера) ────────────────────────────────────────────
    private final List<String> displayLines = new ArrayList<>();
    private static final int MAX_LINES = 50;

    private String  mediaUrl = "";
    private boolean isVideo  = false;

    public MonitorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MONITOR.get(), pos, state);
    }

    // ── Тик ─────────────────────────────────────────────────────────────────
    public static void tick(Level level, BlockPos pos, BlockState state, MonitorBlockEntity be) {
        // Видеокадры — будущее расширение
    }

    // ── Мультиблок API ───────────────────────────────────────────────────────

    /** Настраивает роль блока в мультиблоке */
    public void setMultiblock(BlockPos master, int w, int h, boolean master_, int[] local) {
        this.masterPos = master_ ? null : this.worldPosition;
        this.width     = w;
        this.height    = h;
        this.isMaster  = master_;
        this.localX    = local[0];
        this.localY    = local[1];
        sendClientUpdate();
    }

    /** Сброс в одиночный монитор */
    public void resetMultiblock() {
        masterPos = null;
        width     = 1;
        height    = 1;
        isMaster  = true;
        localX    = 0;
        localY    = 0;
        sendClientUpdate();
    }

    public boolean isMaster()       { return isMaster || masterPos == null; }
    public BlockPos getMasterPos()  { return masterPos != null ? masterPos : worldPosition; }
    public int getWidth()           { return width; }
    public int getHeight()          { return height; }
    public int getLocalX()          { return localX; }
    public int getLocalY()          { return localY; }

    /** Разрешение экрана в условных пикселях */
    public int[] getResolution() {
        if (width >= 12 && height >= 24) return new int[]{3840, 2160};
        if (width >= 6  && height >= 12) return new int[]{2560, 1440};
        if (width >= 6  && height >= 9)  return new int[]{1920, 1080};
        if (width >= 4  && height >= 6)  return new int[]{1280, 720};
        return new int[]{640, 360};
    }

    // ── Подключение ──────────────────────────────────────────────────────────
    public void connectTo(BlockPos computerPos) {
        this.connectedComputer = computerPos;
        sendClientUpdate();
    }
    public void disconnect() {
        connectedComputer = null;
        displayLines.clear();
        mediaUrl = "";
        sendClientUpdate();
    }
    public boolean isConnected()           { return connectedComputer != null; }
    public BlockPos getConnectedComputer() { return connectedComputer; }

    // ── Контент ──────────────────────────────────────────────────────────────
    public void setLines(List<String> lines) {
        displayLines.clear();
        displayLines.addAll(lines.subList(0, Math.min(lines.size(), MAX_LINES)));
        sendClientUpdate();
    }
    public void pushLine(String line) {
        displayLines.add(line);
        while (displayLines.size() > MAX_LINES) displayLines.remove(0);
        sendClientUpdate();
        if (level instanceof net.minecraft.server.level.ServerLevel sl) {
            com.example.ohmypc.network.ModPackets.broadcastMonitorText(
                    worldPosition, java.util.List.copyOf(displayLines), sl);
        }
    }
    public List<String> getDisplayLines() { return displayLines; }

    public void setMediaUrl(String url, boolean video) {
        this.mediaUrl = url;
        this.isVideo  = video;
        sendClientUpdate();
        // Бродкастим всем игрокам в чанке
        if (level instanceof net.minecraft.server.level.ServerLevel sl) {
            com.example.ohmypc.network.ModPackets.broadcastMonitorMedia(
                    worldPosition, url, video, sl);
        }
    }
    public String  getMediaUrl() { return mediaUrl; }
    public boolean isVideo()     { return isVideo; }

    // ── Синхронизация клиента (важно для рендера!) ────────────────────────────
    @Nullable @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override
    public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }

    private void sendClientUpdate() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // ── NBT ──────────────────────────────────────────────────────────────────
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        // Мультиблок
        if (masterPos != null) tag.putLong("masterPos", masterPos.asLong());
        tag.putInt("mbW", width);
        tag.putInt("mbH", height);
        tag.putBoolean("isMaster", isMaster);
        tag.putInt("lx", localX);
        tag.putInt("ly", localY);
        // Подключение
        if (connectedComputer != null) tag.putLong("connected", connectedComputer.asLong());
        // Контент
        tag.putString("mediaUrl", mediaUrl);
        tag.putBoolean("isVideo",  isVideo);
        var linesTag = new net.minecraft.nbt.ListTag();
        for (String l : displayLines) {
            linesTag.add(net.minecraft.nbt.StringTag.valueOf(l));
        }
        tag.put("lines", linesTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        masterPos = tag.contains("masterPos") ? BlockPos.of(tag.getLong("masterPos")) : null;
        width     = tag.getInt("mbW");  if (width  < 1) width  = 1;
        height    = tag.getInt("mbH");  if (height < 1) height = 1;
        isMaster  = tag.getBoolean("isMaster");
        localX    = tag.getInt("lx");
        localY    = tag.getInt("ly");
        connectedComputer = tag.contains("connected") ? BlockPos.of(tag.getLong("connected")) : null;
        mediaUrl  = tag.getString("mediaUrl");
        isVideo   = tag.getBoolean("isVideo");
        displayLines.clear();
        var linesTag = tag.getList("lines", 8);
        for (int i = 0; i < linesTag.size(); i++) displayLines.add(linesTag.getString(i));
    }
}
