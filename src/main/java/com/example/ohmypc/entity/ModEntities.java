package com.example.ohmypc.entity;

import com.example.ohmypc.Ohmypc;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Ohmypc.MOD_ID);

    public static final RegistryObject<EntityType<TurtleEntity>> TURTLE = ENTITIES.register("turtle",
            () -> EntityType.Builder.of(TurtleEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .build("turtle"));
}