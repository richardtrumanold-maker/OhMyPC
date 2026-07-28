package com.example.ohmypc.block;

import com.example.ohmypc.block.entity.CableBlockEntity;
import com.example.ohmypc.block.entity.ComputerBlockEntity;
import com.example.ohmypc.block.entity.MonitorBlockEntity;
import com.example.ohmypc.block.entity.NetworkHubBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class NetworkCableBlock extends Block implements EntityBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    private static final VoxelShape SHAPE = Block.box(5, 5, 5, 11, 11, 11);

    public NetworkCableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST, false).setValue(WEST, false)
                .setValue(UP, false).setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    private boolean canConnectTo(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof ComputerBlockEntity
                || be instanceof MonitorBlockEntity
                || be instanceof NetworkHubBlockEntity
                || be instanceof CableBlockEntity;
    }

    public void updateConnections(Level level, BlockPos pos) {
        if (level.isClientSide) return;
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof NetworkCableBlock)) return;

        state = state.setValue(NORTH, canConnectTo(level, pos.north()))
                     .setValue(SOUTH, canConnectTo(level, pos.south()))
                     .setValue(EAST, canConnectTo(level, pos.east()))
                     .setValue(WEST, canConnectTo(level, pos.west()))
                     .setValue(UP, canConnectTo(level, pos.above()))
                     .setValue(DOWN, canConnectTo(level, pos.below()));
        level.setBlock(pos, state, 3);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        updateConnections(level, pos);
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            if (level.getBlockState(neighbor).getBlock() instanceof NetworkCableBlock cable) {
                cable.updateConnections(level, neighbor);
            }
        }
        tryConnectDevices(level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                BlockEntity be = level.getBlockEntity(neighbor);
                if (be instanceof ComputerBlockEntity c) c.disconnect();
                if (be instanceof MonitorBlockEntity m) m.disconnect();
                if (be instanceof NetworkHubBlockEntity h) h.disconnect();
                if (level.getBlockState(neighbor).getBlock() instanceof NetworkCableBlock cable) {
                    cable.updateConnections(level, neighbor);
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    private void tryConnectDevices(Level level, BlockPos pos) {
        ComputerBlockEntity computer = null;
        MonitorBlockEntity monitor = null;
        NetworkHubBlockEntity hub = null;

        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            BlockEntity be = level.getBlockEntity(neighbor);
            if (be instanceof ComputerBlockEntity c) computer = c;
            else if (be instanceof MonitorBlockEntity m) monitor = m;
            else if (be instanceof NetworkHubBlockEntity h) hub = h;
        }

        if (computer != null && monitor != null) {
            computer.connectMonitor(monitor.getBlockPos());
            monitor.connectComputer(computer.getBlockPos());
        }
        if (computer != null && hub != null) {
            computer.connectHub(hub.getBlockPos());
        }
        if (monitor != null && hub != null) {
            monitor.connectHub(hub.getBlockPos());
        }
    }
}
