package com.example.ohmypc.item;

import com.example.ohmypc.Ohmypc;
import com.example.ohmypc.block.ModBlocks;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Ohmypc.MOD_ID);

    private static RegistryObject<Item> simple(String id) {
        return ITEMS.register(id, () -> new Item(new Item.Properties()));
    }
    private static RegistryObject<Item> comp(String id, String type, int tier) {
        return ITEMS.register(id, () -> new ComponentItem(type, tier, new Item.Properties().stacksTo(1)));
    }

    // ── Компоненты ────────────────────────────────────────────────────────────
    public static final RegistryObject<Item> CPU_TIER_1 = comp("cpu_tier_1", "cpu", 1);
    public static final RegistryObject<Item> CPU_TIER_2 = comp("cpu_tier_2", "cpu", 2);
    public static final RegistryObject<Item> MEMORY     = comp("memory",     "ram", 1);
    public static final RegistryObject<Item> HARD_DRIVE = ITEMS.register("hard_drive",
            () -> new ComponentItem("storage", 1, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FLASH_MEMORY = ITEMS.register("flash_memory",
            () -> new ComponentItem("storage", 2, new Item.Properties().stacksTo(1)));

    // GPU
    public static final RegistryObject<Item> VIDEO_CARD_T1 = comp("video_card_t1", "video_card", 1);
    public static final RegistryObject<Item> VIDEO_CARD_T2 = comp("video_card_t2", "video_card", 2);

    // Сеть
    public static final RegistryObject<Item> NETWORK_CARD_BASIC    = comp("network_card_basic",    "network", 1);
    public static final RegistryObject<Item> NETWORK_CARD_ADVANCED  = comp("network_card_advanced",  "network", 2);
    public static final RegistryObject<Item> NETWORK_CARD_ELITE     = comp("network_card_elite",     "network", 3);
    public static final RegistryObject<Item> WIRELESS_MODEM         = comp("wireless_modem",         "wireless", 1);
    public static final RegistryObject<Item> WIRELESS_MODEM_ADVANCED= comp("wireless_modem_advanced","wireless", 2);

    // Материалы
    public static final RegistryObject<Item> TRANSISTOR    = simple("transistor");
    public static final RegistryObject<Item> CIRCUIT_BOARD = simple("circuit_board");
    public static final RegistryObject<Item> BUS_CABLE     = simple("bus_cable");

    // Черепашки
    public static final RegistryObject<Item> TURTLE          = simple("turtle");
    public static final RegistryObject<Item> ADVANCED_TURTLE = simple("advanced_turtle");


    // ── Блок-предметы (ссылки для StarterKit) ────────────────────────────────
    public static final RegistryObject<Item> COMPUTER_ITEM =
            ITEMS.register("computer_blockitem",
                    () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MONITOR_ITEM =
            ITEMS.register("monitor_blockitem",
                    () -> new Item(new Item.Properties().stacksTo(8)));
    public static final RegistryObject<Item> NETWORK_CABLE_ITEM =
            ITEMS.register("network_cable_item",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // ── Стартовый набор ───────────────────────────────────────────────────────
    public static final RegistryObject<Item> ACTIVATION_KEY =
            ITEMS.register("wrench",
                    () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> STARTER_KIT =
            ITEMS.register("starter_kit",
                    () -> new StarterKitItem(new Item.Properties().stacksTo(1)));

    // ── Руководство ───────────────────────────────────────────────────────────
    public static final RegistryObject<Item> MANUAL =
            ITEMS.register("manual",
                    () -> new ManualItem(new Item.Properties().stacksTo(1)));

    // ── Флоппи-диски (16 цветов) ─────────────────────────────────────────────
    public static final RegistryObject<Item> FLOPPY_WHITE      = floppy(DyeColor.WHITE);
    public static final RegistryObject<Item> FLOPPY_ORANGE     = floppy(DyeColor.ORANGE);
    public static final RegistryObject<Item> FLOPPY_MAGENTA    = floppy(DyeColor.MAGENTA);
    public static final RegistryObject<Item> FLOPPY_LIGHT_BLUE = floppy(DyeColor.LIGHT_BLUE);
    public static final RegistryObject<Item> FLOPPY_YELLOW     = floppy(DyeColor.YELLOW);
    public static final RegistryObject<Item> FLOPPY_LIME       = floppy(DyeColor.LIME);
    public static final RegistryObject<Item> FLOPPY_PINK       = floppy(DyeColor.PINK);
    public static final RegistryObject<Item> FLOPPY_GRAY       = floppy(DyeColor.GRAY);
    public static final RegistryObject<Item> FLOPPY_LIGHT_GRAY = floppy(DyeColor.LIGHT_GRAY);
    public static final RegistryObject<Item> FLOPPY_CYAN       = floppy(DyeColor.CYAN);
    public static final RegistryObject<Item> FLOPPY_PURPLE     = floppy(DyeColor.PURPLE);
    public static final RegistryObject<Item> FLOPPY_BLUE       = floppy(DyeColor.BLUE);
    public static final RegistryObject<Item> FLOPPY_BROWN      = floppy(DyeColor.BROWN);
    public static final RegistryObject<Item> FLOPPY_GREEN      = floppy(DyeColor.GREEN);
    public static final RegistryObject<Item> FLOPPY_RED        = floppy(DyeColor.RED);
    public static final RegistryObject<Item> FLOPPY_BLACK      = floppy(DyeColor.BLACK);

    private static RegistryObject<Item> floppy(DyeColor color) {
        return ITEMS.register("floppy_" + color.getName(),
                () -> new FloppyDiskItem(color, new Item.Properties()));
    }
}
