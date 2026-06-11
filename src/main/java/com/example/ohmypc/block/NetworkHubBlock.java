package com.example.ohmypc.block;

import com.example.ohmypc.block.entity.ModBlockEntities;
import com.example.ohmypc.block.entity.NetworkHubBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class NetworkHubBlock extends BaseEntityBlock {
    public NetworkHubBlock(BlockBehaviour.Properties props) { super(props); }
    @Override public RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NetworkHubBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) { return null; }
}
