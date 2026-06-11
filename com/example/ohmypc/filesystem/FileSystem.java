package com.example.ohmypc.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSystem {

    private final Path rootPath;

    public FileSystem(String computerId) {
        this.rootPath = Paths.get(System.getProperty("user.dir"), "ohmypc", computerId);
        try {
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new RuntimeException("[OhMyPC] Failed to create filesystem folder: " + rootPath, e);
        }
    }

    public String readFile(String filePath) {
        try {
            Path fullPath = rootPath.resolve(filePath).normalize();
            if (!fullPath.startsWith(rootPath)) {
                System.err.println("[OhMyPC] Sandbox violation: " + filePath);
                return null;
            }
            return Files.readString(fullPath);
        } catch (IOException e) {
            System.err.println("[OhMyPC] Error reading file '" + filePath + "': " + e.getMessage());
            return null;
        }
    }

    public void writeFile(String filePath, String content) {
        try {
            Path fullPath = rootPath.resolve(filePath).normalize();
            if (!fullPath.startsWith(rootPath)) {
                System.err.println("[OhMyPC] Sandbox violation: " + filePath);
                return;
            }
            Files.createDirectories(fullPath.getParent());
            Files.writeString(fullPath, content);
        } catch (IOException e) {
            System.err.println("[OhMyPC] Error writing file '" + filePath + "': " + e.getMessage());
        }
    }

    public boolean deleteFile(String filePath) {
        try {
            Path fullPath = rootPath.resolve(filePath).normalize();
            if (!fullPath.startsWith(rootPath)) return false;
            return Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            return false;
        }
    }

    public List<String> listFiles(String directoryPath) {
        try {
            Path fullPath = rootPath.resolve(directoryPath).normalize();
            if (!fullPath.startsWith(rootPath)) return List.of();
            try (Stream<Path> stream = Files.list(fullPath)) {
                return stream.map(p -> p.getFileName().toString()).collect(Collectors.toList());
            }
        } catch (IOException e) {
            return List.of();
        }
    }

    public boolean fileExists(String filePath) {
        Path fullPath = rootPath.resolve(filePath).normalize();
        return fullPath.startsWith(rootPath) && Files.exists(fullPath);
    }

    public void createDirectory(String directoryPath) {
        try {
            Path fullPath = rootPath.resolve(directoryPath).normalize();
            if (!fullPath.startsWith(rootPath)) return;
            Files.createDirectories(fullPath);
        } catch (IOException e) {
            System.err.println("[OhMyPC] Error creating directory '" + directoryPath + "': " + e.getMessage());
        }
    }

    public Path getRootPath() {
        return rootPath;
    }
}