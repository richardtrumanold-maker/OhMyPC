package com.example.ohmypc.block;

import net.minecraft.util.StringRepresentable;

/**
 * Состояния компьютера для blockstate/индикатора.
 * OFF      → тёмный индикатор
 * BOOTING  → мигающий жёлтый (анимация через mcmeta)
 * ON       → зелёный
 * CRASHED  → мигающий красный
 */
public enum ComputerState implements StringRepresentable {
    OFF("off"),
    BOOTING("booting"),
    ON("on"),
    CRASHED("crashed");

    private final String name;

    ComputerState(String name) { this.name = name; }

    @Override
    public String getSerializedName() { return name; }

    public static ComputerState byName(String name) {
        for (ComputerState s : values()) if (s.name.equals(name)) return s;
        return OFF;
    }
}
