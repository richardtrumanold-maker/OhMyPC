package com.example.ohmypc.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

public class FloppyDiskItem extends Item {

    private final DyeColor color;

    public FloppyDiskItem(DyeColor color, Properties props) {
        super(props.stacksTo(1));
        this.color = color;
    }

    public DyeColor getDiskColor() { return color; }
    public DyeColor getFloppyColor() { return color; }  // ← алиас для совместимости
    public String   getColorName() { return color.getName(); }

    public Path getDiskPath() {
        Path root = FMLPaths.GAMEDIR.get().resolve("floppy").resolve(color.getName());
        try { Files.createDirectories(root); } catch (IOException ignored) {}
        return root;
    }

    public String getSectionColor() {
        return switch (color) {
            case WHITE      -> "§f";
            case ORANGE     -> "§6";
            case MAGENTA    -> "§d";
            case LIGHT_BLUE -> "§b";
            case YELLOW     -> "§e";
            case LIME       -> "§a";
            case PINK       -> "§d";
            case GRAY       -> "§7";
            case LIGHT_GRAY -> "§7";
            case CYAN       -> "§3";
            case PURPLE     -> "§5";
            case BLUE       -> "§9";
            case BROWN      -> "§4";
            case GREEN      -> "§2";
            case RED        -> "§c";
            case BLACK      -> "§8";
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(getSectionColor() + "● " + color.getName().replace("_", " ") + " disk"));
        tooltip.add(Component.literal("§8floppy/" + getColorName() + "/").withStyle(ChatFormatting.DARK_GRAY));

        try {
            Path diskPath = getDiskPath();
            List<String> files = Files.list(diskPath)
                    .filter(p -> Files.isRegularFile(p))
                    .map(p -> p.getFileName().toString())
                    .limit(5)
                    .collect(Collectors.toList());
            if (!files.isEmpty()) {
                tooltip.add(Component.literal("§7Files: §8" + String.join(", ", files)));
            } else {
                tooltip.add(Component.literal("§8[empty]"));
            }
        } catch (Exception ignored) {}
    }
}
