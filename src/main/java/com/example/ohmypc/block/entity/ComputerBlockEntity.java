package com.example.ohmypc.block.entity;

import com.example.ohmypc.block.ComputerBlock;
import com.example.ohmypc.block.ComputerState;
import com.example.ohmypc.filesystem.FileSystem;
import com.example.ohmypc.filesystem.FloppyFileSystem;
import com.example.ohmypc.item.ComponentItem;
import com.example.ohmypc.projector.CinemaProjectorBlockEntity;
import com.example.ohmypc.lua.LuaAPI;
import com.example.ohmypc.lua.LuaEngine;
import com.example.ohmypc.network.NetworkBus;
import com.example.ohmypc.network.NetworkBus.ConnectionType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ComputerBlockEntity v3
 *
 * Слоты: 0=CPU, 1=RAM1, 2=RAM2, 3=GPU, 4=Storage, 5=Network
 *
 * Новое:
 *  - Монтирование флоппи-диска (/disk/)
 *  - Сетевой адрес (UUID), подключение к NetworkBus
 *  - BSOD при краше Lua
 *  - Пароль (SHA-256)
 *  - GPU-тиры (0=нет, 1=T1, 2=T2)
 *  - connectedMonitor
 */
public class ComputerBlockEntity extends BlockEntity implements Container {

    // ── Слоты ─────────────────────────────────────────────────────────────────
    public static final int SLOT_CPU     = 0;
    public static final int SLOT_RAM1    = 1;
    public static final int SLOT_RAM2    = 2;
    public static final int SLOT_GPU     = 3;
    public static final int SLOT_STORAGE = 4;
    public static final int SLOT_NETWORK = 5;
    public static final int SLOT_COUNT   = 6;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override protected void onContentsChanged(int slot) {
            setChanged();
            validatePowerState();
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> inventory);

    // ── Состояние ─────────────────────────────────────────────────────────────
    private boolean powered    = false;
    private boolean crashed    = false;
    private String  crashMsg   = "";

    // ── Имя и пароль ─────────────────────────────────────────────────────────
    private Component customName  = null;
    private String    passwordHash = ""; // SHA-256 хеш, пусто = нет пароля
    private boolean   locked       = false;
    private String    ownerUUID    = "";

    // ── Сеть ─────────────────────────────────────────────────────────────────
    private String networkAddress = ""; // UUID строкой

    // ── Подключения ───────────────────────────────────────────────────────────
    private BlockPos connectedMonitor    = null;
    private BlockPos connectedProjector   = null;

    // ── Lua и ФС ──────────────────────────────────────────────────────────────
    private LuaEngine       luaEngine    = null;
    private FileSystem      fileSystem   = null;
    private FloppyFileSystem floppy      = null; // смонтированный флоппи

    // ── Терминал ──────────────────────────────────────────────────────────────
    private final List<String> terminalLines = new ArrayList<>();
    private static final int MAX_LINES = 200;

    public ComputerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPUTER.get(), pos, state);
    }

    // ── Тик ──────────────────────────────────────────────────────────────────
    public static void tick(Level level, BlockPos pos, BlockState state, ComputerBlockEntity be) {}

    // ── GPU-тир ──────────────────────────────────────────────────────────────
    public int getGpuTier() {
        ItemStack gpu = inventory.getStackInSlot(SLOT_GPU);
        if (gpu.isEmpty()) return 0;
        if (gpu.getItem() instanceof ComponentItem ci) return ci.getTier();
        return 1;
    }
    public boolean canUseImages() { return getGpuTier() >= 1; }
    public boolean canUseVideo()  { return getGpuTier() >= 2; }

    // ── Сеть ─────────────────────────────────────────────────────────────────
    public String getNetworkAddress() {
        if (networkAddress.isEmpty()) {
            networkAddress = UUID.randomUUID().toString().substring(0, 8);
            setChanged();
        }
        return networkAddress;
    }

    private boolean hasNetworkCard() {
        return !inventory.getStackInSlot(SLOT_NETWORK).isEmpty();
    }

    private ConnectionType getConnectionType() {
        ItemStack net = inventory.getStackInSlot(SLOT_NETWORK);
        if (net.isEmpty()) return null;
        if (net.getItem() instanceof ComponentItem ci && ci.getType().equals("wireless")) {
            return ConnectionType.WIRELESS;
        }
        return ConnectionType.WIRED;
    }

    // ── Питание ──────────────────────────────────────────────────────────────
    private void validatePowerState() {
        if (!inventory.getStackInSlot(SLOT_CPU).isEmpty()) return;
        if (inventory.getStackInSlot(SLOT_RAM1).isEmpty() &&
            inventory.getStackInSlot(SLOT_RAM2).isEmpty()) {
            shutdown();
        }
    }

    public boolean canBoot() {
        return !inventory.getStackInSlot(SLOT_CPU).isEmpty()
            && (!inventory.getStackInSlot(SLOT_RAM1).isEmpty()
             || !inventory.getStackInSlot(SLOT_RAM2).isEmpty());
    }

    public void boot() {
        if (powered || level == null) return;
        if (!canBoot()) { printLine("§cError: CPU or RAM not installed!"); return; }

        String id = "comp_" + worldPosition.toShortString()
                .replace(", ", "_").replace("(", "").replace(")", "");
        fileSystem = new FileSystem(id);
        luaEngine  = new LuaEngine();
        crashed    = false;
        crashMsg   = "";

        // Регистрация в сети
        ConnectionType ct = getConnectionType();
        if (ct != null) NetworkBus.register(getNetworkAddress(), this, ct);

        LuaAPI.register(luaEngine, this);
        registerFsAPI();

        powered = true;
        setChanged();

        ComputerBlock.setState(level, worldPosition, ComputerState.ON);

        ComputerBlock.setState(level, worldPosition, ComputerState.BOOTING);
        printLine("§aOhMyPC v2.3.0 — booting...");
        if (level != null) com.example.ohmypc.block.ComputerBlock.setState(level, worldPosition, com.example.ohmypc.block.ComputerState.BOOTING);
        printLine("§7Address: §b" + getNetworkAddress());
        if (floppy != null) printLine("§7Disk: §a" + floppy.getColorName() + " mounted at /disk/");

        String startup = fileSystem.readFile("startup.lua");
        if (level != null) com.example.ohmypc.block.ComputerBlock.setState(level, worldPosition, com.example.ohmypc.block.ComputerState.ON);
        if (startup != null) {
            luaEngine.executeScript(startup, this);
        } else {
            String def = "print(\"Hello from OhMyPC!\")\nprint(\"Type help() for commands.\")";
            fileSystem.writeFile("startup.lua", def);
            luaEngine.executeScript(def, this);
        }
    }

    public void shutdown() {
    if (!powered) return;
    powered = false;
    NetworkBus.unregister(getNetworkAddress());
    luaEngine = null;
    if (level != null) {
        level.setBlock(worldPosition, level.getBlockState(worldPosition).setValue(ComputerBlock.POWERED, false), 3);
    }
    setChanged();
    printLine("§7System halted.");
}
    /** Вызывается при краше Lua-скрипта */
    public void crash(String message) {
        crashed  = true;
        crashMsg = message;
        if (level != null) ComputerBlock.setState(level, worldPosition, ComputerState.CRASHED);
        if (level != null) com.example.ohmypc.block.ComputerBlock.setState(level, worldPosition, com.example.ohmypc.block.ComputerState.CRASHED);
        printLine("§4[CRASH] " + message);
        // Показать BSOD на мониторе
        pushBSODToMonitor();
        shutdown();
    }

    private void pushBSODToMonitor() {
        if (level == null || connectedMonitor == null) return;
        if (level.getBlockEntity(connectedMonitor) instanceof MonitorBlockEntity mbe) {
            List<String> bsod = List.of(
                "§0                                ",
                "§0  §4■■■■■■■■■■■■■■■■■■■■■■■■■■■■",
                "§0  §4■  §c OhMyPC  §4■",
                "§0  §4■  §fA fatal error occurred.  §4■",
                "§0  §4■                              §4■",
                "§0  §4■  §e" + crashMsg.substring(0, Math.min(crashMsg.length(), 26)) + "  §4■",
                "§0  §4■                              §4■",
                "§0  §4■  §7Reboot to continue.       §4■",
                "§0  §4■■■■■■■■■■■■■■■■■■■■■■■■■■■■"
            );
            mbe.setLines(bsod);
            mbe.setMediaUrl("", false);
        }
    }

    // ── Флоппи ───────────────────────────────────────────────────────────────
    public void mountFloppy(FloppyFileSystem fs) {
        this.floppy = fs;
        if (powered) printLine("§aDisk inserted: §f" + fs.getColorName());
        setChanged();
    }
    public void unmountFloppy() {
        if (floppy != null && powered) printLine("§7Disk ejected.");
        this.floppy = null;
        setChanged();
    }
    public FloppyFileSystem getFloppy() { return floppy; }

    // ── Пароль ───────────────────────────────────────────────────────────────
    public boolean hasPassword()    { return !passwordHash.isEmpty(); }
    public boolean isLocked()       { return locked && hasPassword(); }
    public void lock()              { if (hasPassword()) { locked = true; setChanged(); } }
    public void unlock()            { locked = false; setChanged(); }
    public void clearPassword()     { passwordHash = ""; locked = false; setChanged(); }

    public void setPassword(String raw) {
        passwordHash = sha256(raw);
        locked = false;
        setChanged();
    }
    public boolean checkPassword(String raw) {
        return !passwordHash.isEmpty() && passwordHash.equals(sha256(raw));
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return ""; }
    }

    // ── Терминал ─────────────────────────────────────────────────────────────
    public void printLine(String line) {
        terminalLines.add(line);
        while (terminalLines.size() > MAX_LINES) terminalLines.remove(0);
        setChanged();
    }
    public List<String> getTerminalLines() { return terminalLines; }

    public String executeCommand(String cmd) {
        if (!powered) return "§cComputer is off.";
        if (luaEngine == null) return "§cLua engine not ready.";
        if (isLocked()) return "§cLocked. Enter password first.";
        printLine("§e> " + cmd);
        String result = luaEngine.executeScript(cmd, this);
        if (result != null && !result.isEmpty() && !result.startsWith("Error:"))
            printLine(result);
        else if (result != null && result.startsWith("Error:"))
            crash(result.substring(6).trim());
        return result != null ? result : "";
    }

    // ── Внутренние Lua FS API ─────────────────────────────────────────────────
    private void registerFsAPI() {
        if (luaEngine == null || fileSystem == null) return;
        var g = luaEngine.getGlobals();
        // Регистрируется через LuaAPI — здесь просто маркер
        // (полный fs API и floppy API регистрируется в LuaAPI.register)
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public boolean     isPowered()          { return powered; }
    public boolean     isCrashed()          { return crashed; }
    public String      getCrashMsg()        { return crashMsg; }
    public LuaEngine   getLuaEngine()       { return luaEngine; }
    public FileSystem  getFileSystem()      { return fileSystem; }
    public ItemStackHandler getInventory()  { return inventory; }
    public String      getComputerName()    { return customName != null ? customName.getString() : "Computer"; }
    public void        setCustomName(Component n) { customName = n; }
    public BlockPos    getConnectedMonitor()  { return connectedMonitor; }
    public void        setConnectedMonitor(BlockPos p) { connectedMonitor = p; setChanged(); }
    public BlockPos    getConnectedProjector() { return connectedProjector; }
    public void        setConnectedProjector(BlockPos p) { connectedProjector = p; setChanged(); }

    // ── NBT ──────────────────────────────────────────────────────────────────
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", inventory.serializeNBT());
        tag.putBoolean("powered",  powered);
        tag.putBoolean("crashed",  crashed);
        tag.putString("crashMsg",  crashMsg);
        tag.putString("netAddr",   networkAddress);
        tag.putString("pwHash",    passwordHash);
        tag.putBoolean("locked",   locked);
        tag.putString("owner",     ownerUUID);
        if (customName    != null)  tag.putString("name",       Component.Serializer.toJson(customName));
        if (connectedMonitor  != null) tag.putLong("connMon",   connectedMonitor.asLong());
        if (connectedProjector != null) tag.putLong("connHolo",  connectedProjector.asLong());
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("inventory"))  inventory.deserializeNBT(tag.getCompound("inventory"));
        powered          = tag.getBoolean("powered");
        crashed          = tag.getBoolean("crashed");
        crashMsg         = tag.getString("crashMsg");
        networkAddress   = tag.getString("netAddr");
        passwordHash     = tag.getString("pwHash");
        locked           = tag.getBoolean("locked");
        ownerUUID        = tag.getString("owner");
        if (tag.contains("name"))     customName       = Component.Serializer.fromJson(tag.getString("name"));
        if (tag.contains("connMon"))  connectedMonitor  = BlockPos.of(tag.getLong("connMon"));
        if (tag.contains("connHolo")) connectedProjector = BlockPos.of(tag.getLong("connHolo"));
    }

    // ── Capabilities ─────────────────────────────────────────────────────────
    @Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemHandler.cast();
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); itemHandler.invalidate(); }

    // ── Container (для дропа) ─────────────────────────────────────────────────
    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override public boolean isEmpty() {
        for (int i = 0; i < SLOT_COUNT; i++) if (!inventory.getStackInSlot(i).isEmpty()) return false;
        return true;
    }
    @Override public ItemStack getItem(int s)               { return inventory.getStackInSlot(s); }
    @Override public ItemStack removeItem(int s, int a)     { return inventory.extractItem(s, a, false); }
    @Override public ItemStack removeItemNoUpdate(int s)    { var i = inventory.getStackInSlot(s); inventory.setStackInSlot(s, ItemStack.EMPTY); return i; }
    @Override public void setItem(int s, ItemStack st)      { inventory.setStackInSlot(s, st); }
    @Override public boolean stillValid(Player p)           { return true; }
    @Override public void clearContent()                    { for (int i = 0; i < SLOT_COUNT; i++) inventory.setStackInSlot(i, ItemStack.EMPTY); }
}
