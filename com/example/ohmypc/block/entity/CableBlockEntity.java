package com.example.ohmypc.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Кабель — хранит ссылки на оба конца соединения */
public class CableBlockEntity extends BlockEntity {

    private BlockPos endpointA = null;
    private BlockPos endpointB = null;

    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CABLE.get(), pos, state);
    }

    public void setEndpoints(BlockPos a, BlockPos b) {
        this.endpointA = a;
        this.endpointB = b;
        setChanged();
    }

    public BlockPos getEndpointA() { return endpointA; }
    public BlockPos getEndpointB() { return endpointB; }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (endpointA != null) tag.putLong("epA", endpointA.asLong());
        if (endpointB != null) tag.putLong("epB", endpointB.asLong());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("epA")) endpointA = BlockPos.of(tag.getLong("epA"));
        if (tag.contains("epB")) endpointB = BlockPos.of(tag.getLong("epB"));
    }
}
