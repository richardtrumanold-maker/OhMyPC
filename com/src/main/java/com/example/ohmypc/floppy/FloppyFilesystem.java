package com.example.ohmypc.floppy;

import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Физическая файловая система флоппи-диска.
 * Хранится в: <gamedir>/floppy/<colorName>/
 *
 * Два диска одного цвета на любом сервере/мире читают одну и ту же папку.
 * Чтобы перенести диск — скопируй папку floppy/<color>/ в другую установку.
 *
 * Ограничения:
 *  - Максимум 512 КБ на диск (суммарно)
 *  - Только текстовые файлы (UTF-8)
 *  - Sandbox: нельзя выйти за пределы корневой папки диска
 */
public class FloppyFilesystem {

    private static final Logger LOGGER = LogManager.getLogger("OhMyPC/Floppy");
    private static final long MAX_DISK_SIZE = 512 * 1024; // 512 KB

    private final Path    root;
    private final String  colorId;

    public FloppyFilesystem(FloppyColor color) {
        this.colorId = color.id;
        this.root = FMLPaths.GAMEDIR.get()
                .resolve("floppy")
                .resolve(color.id);
        try {
            Files.createDirectories(root);
            LOGGER.info("[Floppy] Mounted disk '{}' at {}", color.id, root);
        } catch (IOException e) {
            throw new RuntimeException("[Floppy] Cannot create disk dir: " + root, e);
        }
    }

    // ── Sandbox helper ────────────────────────────────────────────────────────
    private Path safe(String path) {
        Path full = root.resolve(path).normalize();
        if (!full.startsWith(root)) throw new SecurityException("Sandbox violation: " + path);
        return full;
    }

    // ── Quota ────────────────────────────────────────────────────────────────
    private long usedBytes() {
        try (Stream<Path> s = Files.walk(root)) {
            return s.filter(Files::isRegularFile)
                    .mapToLong(p -> { try { return Files.size(p); } catch (IOException e) { return 0; } })
                    .sum();
        } catch (IOException e) { return 0; }
    }

    // ── API ──────────────────────────────────────────────────────────────────
    public String read(String path) {
        try { return Files.readString(safe(path)); }
        catch (Exception e) { return null; }
    }

    public boolean write(String path, String content) {
        try {
            long contentSize = content.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
            if (usedBytes() + contentSize > MAX_DISK_SIZE) return false;
            Path full = safe(path);
            Files.createDirectories(full.getParent());
            Files.writeString(full, content);
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean exists(String path) {
        try { return Files.exists(safe(path)); } catch (Exception e) { return false; }
    }

    public boolean delete(String path) {
        try { return Files.deleteIfExists(safe(path)); } catch (Exception e) { return false; }
    }

    public List<String> list(String dir) {
        try (Stream<Path> s = Files.list(safe(dir.isEmpty() ? "." : dir))) {
            return s.map(p -> p.getFileName().toString()).collect(Collectors.toList());
        } catch (Exception e) { return List.of(); }
    }

    public boolean mkdir(String path) {
        try { Files.createDirectories(safe(path)); return true; }
        catch (Exception e) { return false; }
    }

    public long freeBytes()  { return MAX_DISK_SIZE - usedBytes(); }
    public long totalBytes() { return MAX_DISK_SIZE; }

    public String getColorId() { return colorId; }
    public Path   getRoot()    { return root; }
}
