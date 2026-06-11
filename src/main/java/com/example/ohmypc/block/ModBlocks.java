package com.example.ohmypc.block;

import com.example.ohmypc.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;
import com.example.ohmypc.Ohmypc;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Ohmypc.MOD_ID);

    public static final RegistryObject<Block> COMPUTER = registerBlock("computer", ComputerBlock::new);
    public static final RegistryObject<Block> MONITOR  = registerBlock("monitor",  MonitorBlock::new);

    public static final RegistryObject<Block> NETWORK_CABLE = registerBlock("network_cable",
            () -> new NetworkCableBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BARS).strength(0.5F).noOcclusion()));

    /** Дисковод — полноценный BaseEntityBlock */
    public static final RegistryObject<Block> DISK_DRIVE = registerBlock("disk_drive", DiskDriveBlock::new);

    /** Сетевой хаб */
    public static final RegistryObject<Block> NETWORK_HUB = registerBlock("network_hub",
            () -> new com.example.ohmypc.block.NetworkHubBlock(
                    BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(2.0F)));

    /** Кино проектор */
    public static final RegistryObject<Block> CINEMA_PROJECTOR = registerBlock("cinema_projector",
            () -> new com.example.ohmypc.projector.CinemaProjectorBlock(
                    BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(2.0F).noOcclusion()
                            .lightLevel(s -> 4)));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> reg = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(reg.get(), new Item.Properties()));
        return reg;
    }
}
