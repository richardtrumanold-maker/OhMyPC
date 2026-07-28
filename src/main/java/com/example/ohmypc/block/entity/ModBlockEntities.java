package com.example.ohmypc.block.entity;

import com.example.ohmypc.Ohmypc;
import com.example.ohmypc.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Ohmypc.MOD_ID);

    public static final RegistryObject<BlockEntityType<ComputerBlockEntity>> COMPUTER =
            BLOCK_ENTITIES.register("computer", () ->
                    BlockEntityType.Builder.of(ComputerBlockEntity::new, ModBlocks.COMPUTER.get()).build(null));

    public static final RegistryObject<BlockEntityType<MonitorBlockEntity>> MONITOR =
            BLOCK_ENTITIES.register("monitor", () ->
                    BlockEntityType.Builder.of(MonitorBlockEntity::new, ModBlocks.MONITOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<CableBlockEntity>> CABLE =
            BLOCK_ENTITIES.register("cable", () ->
                    BlockEntityType.Builder.of(CableBlockEntity::new, ModBlocks.NETWORK_CABLE.get()).build(null));

    public static final RegistryObject<BlockEntityType<DiskDriveBlockEntity>> DISK_DRIVE =
            BLOCK_ENTITIES.register("disk_drive", () ->
                    BlockEntityType.Builder.of(DiskDriveBlockEntity::new, ModBlocks.DISK_DRIVE.get()).build(null));

    public static final RegistryObject<BlockEntityType<NetworkHubBlockEntity>> NETWORK_HUB =
            BLOCK_ENTITIES.register("network_hub", () ->
                    BlockEntityType.Builder.of(NetworkHubBlockEntity::new, ModBlocks.NETWORK_HUB.get()).build(null));
}
