package com.example.ohmypc.filesystem;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ФС флоппи-диска — [gameDir]/floppy/[color]/
 *
 * Реальные файлы на диске. Общие для всех миров и серверов одного gameDir.
 * Лимит: 512 КБ на диск (виртуальный, не проверяется ОС).
 */
public class FloppyFileSystem {

    public static final long DISK_CAPACITY = 512L * 1024; // 512 КБ

    private final Path   root;
    private final String colorName;

    public FloppyFileSystem(String colorName) {
        this.colorName = colorName;
        this.root = FMLPaths.GAMEDIR.get()
                .resolve("floppy")
                .resolve(sanitizeName(colorName));
        try { Files.createDirectories(root); } catch (IOException ignored) {}
    }

    public String getColorName() { return colorName; }
    public Path   getRoot()      { return root; }

    // ── Санитизация ───────────────────────────────────────────────────────────
    private static String sanitizeName(String name) {
        return name.replaceAll("[^a-z0-9_\\-]", "").toLowerCase();
    }

    private Path resolve(String path) {
        String clean = path.replaceAll("\\.\\.", "").replaceAll("^/+", "");
        return root.resolve(clean).normalize();
    }

    private boolean inBounds(Path p) {
        return p.startsWith(root);
    }

    // ── Операции ──────────────────────────────────────────────────────────────
    public String readFile(String path) {
        Path target = resolve(path);
        if (!inBounds(target) || !Files.isRegularFile(target)) return null;
        try { return Files.readString(target); }
        catch (IOException e) { return null; }
    }

    public boolean writeFile(String path, String content) {
        Path target = resolve(path);
        if (!inBounds(target)) return false;
        if (content.length() > DISK_CAPACITY) return false;
        try {
            Files.createDirectories(target.getParent());
            Files.writeString(target, content);
            return true;
        } catch (IOException e) { return false; }
    }

    public boolean fileExists(String path) {
        Path target = resolve(path);
        return inBounds(target) && Files.exists(target);
    }

    public List<String> listFiles(String dir) {
        Path target = dir.isBlank() ? root : resolve(dir);
        if (!inBounds(target) || !Files.isDirectory(target)) target = root;
        try {
            return Files.list(target)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) { return List.of(); }
    }

    public boolean deleteFile(String path) {
        Path target = resolve(path);
        if (!inBounds(target)) return false;
        try { return Files.deleteIfExists(target); }
        catch (IOException e) { return false; }
    }

    public void createDirectory(String path) {
        Path target = resolve(path);
        if (!inBounds(target)) return;
        try { Files.createDirectories(target); } catch (IOException ignored) {}
    }

    /** Занятое место в байтах */
    public long getUsedSpace() {
        try {
            return Files.walk(root)
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> { try { return Files.size(p); } catch (IOException e) { return 0; } })
                    .sum();
        } catch (IOException e) { return 0; }
    }

    public long getFreeSpace() {
        return Math.max(0, DISK_CAPACITY - getUsedSpace());
    }
}
