package com.example.ohmypc.block;

import com.example.ohmypc.block.entity.ModBlockEntities;
import com.example.ohmypc.block.entity.MonitorBlockEntity;
import com.example.ohmypc.monitor.MultiblockMonitor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

public class MonitorBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty   ACTIVE = BlockStateProperties.POWERED;

    public MonitorBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .strength(1.5F, 4.0F)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .lightLevel(state -> state.getValue(ACTIVE) ? 7 : 0));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(ACTIVE, false);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    // Поставили блок — перестраиваем мультиблок
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos,
                        BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (!level.isClientSide()) MultiblockMonitor.reform(level, pos);
    }

    // Убрали блок — соседи перестраиваются
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                          BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MonitorBlockEntity mbe) mbe.disconnect();
            if (!level.isClientSide()) MultiblockMonitor.dissolve(level, pos);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MonitorBlockEntity(pos, state);
    }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                    BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.MONITOR.get(), MonitorBlockEntity::tick);
    }
}
