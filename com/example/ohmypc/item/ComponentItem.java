package com.example.ohmypc.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Компонент компьютера: CPU, RAM, GPU, Storage, Network, Wireless.
 * Хранит тип и тир. Используется для определения возможностей компьютера.
 */
public class ComponentItem extends Item {

    private final String type;
    private final int    tier;

    // Описания по типу и тиру
    private static final Map<String, String[]> DESC = Map.of(
        "cpu",      new String[]{"100 MHz · basic ops",   "500 MHz · networking",   "1 GHz · multithreading"},
        "ram",      new String[]{"256 MB",                "512 MB",                  "1 GB"},
        "video_card",      new String[]{"Text mode only",        "Images (PNG/JPEG/GIF)",   "Video + all formats"},
        "storage",  new String[]{"HDD · 256 GB",          "SSD · 1 TB",              "NVMe · 4 TB"},
        "network",  new String[]{"100 Mbit/s",            "1 Gbit/s",                "10 Gbit/s"},
        "wireless", new String[]{"Wi-Fi · 64 blocks",     "Wi-Fi 5G · 128 blocks",   ""}
    );

    public ComponentItem(String type, int tier, Properties props) {
        super(props.stacksTo(1));
        this.type = type;
        this.tier = tier;
    }

    public String getType() { return type; }
    public int    getTier() { return tier; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> tooltip, TooltipFlag flag) {
        String tierStr = switch (tier) {
            case 1 -> "§7Tier §aI";
            case 2 -> "§7Tier §bII";
            case 3 -> "§7Tier §dIII";
            default -> "§7Tier " + tier;
        };
        tooltip.add(Component.literal(tierStr));

        String[] descs = DESC.get(type);
        if (descs != null && tier - 1 < descs.length) {
            tooltip.add(Component.literal("§8" + descs[tier - 1]).withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(Component.literal("§7Type: §f" + type).withStyle(ChatFormatting.GRAY));
    }
}
