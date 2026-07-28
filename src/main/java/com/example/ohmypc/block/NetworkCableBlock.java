package com.example.ohmypc.block;

import com.example.ohmypc.block.entity.CableBlockEntity;
import com.example.ohmypc.block.entity.ComputerBlockEntity;
import com.example.ohmypc.block.entity.MonitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class NetworkCableBlock extends BaseEntityBlock {

    public NetworkCableBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                  Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof CableBlockEntity cable)) return InteractionResult.PASS;

        BlockPos found = findNearbyDevice(level, pos, 16);
        if (found == null) {
            player.sendSystemMessage(Component.literal("§cNo computer or monitor nearby (max 16 blocks)"));
            return InteractionResult.CONSUME;
        }

        BlockPos epA = cable.getEndpointA();
        BlockPos epB = cable.getEndpointB();

        if (epA == null) {
            cable.setEndpoints(found, null);
            cable.setChanged();
            player.sendSystemMessage(Component.literal("§aFirst endpoint set: " + found.toShortString()));
        } else if (epB == null && !found.equals(epA)) {
            cable.setEndpoints(epA, found);
            cable.setChanged();
            if (tryConnect(level, epA, found, player)) {
                player.sendSystemMessage(Component.literal("§aConnected!"));
            }
        } else {
            tryDisconnect(level, epA, epB);
            cable.setEndpoints(null, null);
            cable.setChanged();
            player.sendSystemMessage(Component.literal("§7Cable disconnected."));
        }
        return InteractionResult.CONSUME;
    }

    private BlockPos findNearbyDevice(Level level, BlockPos start, int maxDist) {
        for (int dx = -maxDist; dx <= maxDist; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -maxDist; dz <= maxDist; dz++) {
                    if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > maxDist) continue;
                    BlockPos check = start.offset(dx, dy, dz);
                    var be = level.getBlockEntity(check);
                    if (be instanceof ComputerBlockEntity || be instanceof MonitorBlockEntity) {
                        return check;
                    }
                }
            }
        }
        return null;
    }

    private boolean tryConnect(Level level, BlockPos a, BlockPos b, Player player) {
        var beA = level.getBlockEntity(a);
        var beB = level.getBlockEntity(b);

        ComputerBlockEntity computer = null;
        MonitorBlockEntity monitor = null;

        if (beA instanceof ComputerBlockEntity c && beB instanceof MonitorBlockEntity m) {
            computer = c; monitor = m;
        } else if (beA instanceof MonitorBlockEntity m && beB instanceof ComputerBlockEntity c) {
            computer = c; monitor = m;
        } else {
            player.sendSystemMessage(Component.literal(
                    "§eNeed: Computer + Monitor.\n§7Got: " +
                    beA.getClass().getSimpleName() + " + " + beB.getClass().getSimpleName()));
            return false;
        }

        if (monitor != null && computer != null) {
            computer.setConnectedMonitor(monitor.getBlockPos());
            monitor.connectTo(computer.getBlockPos());
            player.sendSystemMessage(Component.literal("§aMonitor connected!"));
            return true;
        }
        return false;
    }

    private void tryDisconnect(Level level, BlockPos a, BlockPos b) {
        for (BlockPos pos : new BlockPos[]{a, b}) {
            if (pos == null) continue;
            var be = level.getBlockEntity(pos);
            if (be instanceof ComputerBlockEntity c) {
                c.setConnectedMonitor(null);
            }
            if (be instanceof MonitorBlockEntity m) {
                m.disconnect();
            }
        }
    }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableBlockEntity(pos, state);
    }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                    BlockEntityType<T> type) {
        return null;
    }
}
