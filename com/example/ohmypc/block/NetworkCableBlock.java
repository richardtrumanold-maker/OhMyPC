package com.example.ohmypc.block;

import com.example.ohmypc.block.entity.CableBlockEntity;
import com.example.ohmypc.block.entity.ComputerBlockEntity;
import com.example.ohmypc.block.entity.ModBlockEntities;
import com.example.ohmypc.block.entity.MonitorBlockEntity;
import com.example.ohmypc.projector.CinemaProjectorBlockEntity;
import com.example.ohmypc.monitor.MultiblockMonitor;
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

/**
 * Кабель — соединяет компьютер с монитором.
 *
 * Алгоритм подключения (ПКМ по кабелю):
 *  1. Если один конец не задан — запоминаем как endpointA
 *  2. Если endpointA задан — смотрим на него:
 *     - если это компьютер → второй конец должен быть монитор (и наоборот)
 *     - записываем связь в оба BlockEntity
 *
 * Кабель можно прокладывать через несколько блоков — каждый блок кабеля
 * является частью цепочки, но для простоты мы проверяем только соседство
 * с компьютером/монитором (поиск по BFS не больше 16 блоков).
 */
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

        // Находим ближайший компьютер или монитор рядом с кабелем (BFS)
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
            // Пытаемся подключить
            if (tryConnect(level, epA, found, player)) {
                player.sendSystemMessage(Component.literal("§aConnected!"));
            }
        } else {
            // Сброс
            tryDisconnect(level, epA, epB);
            cable.setEndpoints(null, null);
            cable.setChanged();
            player.sendSystemMessage(Component.literal("§7Cable disconnected."));
        }
        return InteractionResult.CONSUME;
    }

    /** BFS поиск компьютера или монитора рядом с кабелем */
    private BlockPos findNearbyDevice(Level level, BlockPos start, int maxDist) {
        for (int dx = -maxDist; dx <= maxDist; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -maxDist; dz <= maxDist; dz++) {
                    if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > maxDist) continue;
                    BlockPos check = start.offset(dx, dy, dz);
                    var be = level.getBlockEntity(check);
                    if (be instanceof ComputerBlockEntity || be instanceof MonitorBlockEntity || be instanceof CinemaProjectorBlockEntity) {
                        return check;
                    }
                }
            }
        }
        return null;
    }

    /** Подключить два устройства. Возвращает true при успехе */
    private boolean tryConnect(Level level, BlockPos a, BlockPos b, Player player) {
        var beA = level.getBlockEntity(a);
        var beB = level.getBlockEntity(b);

        ComputerBlockEntity        computer  = null;
        MonitorBlockEntity         monitor   = null;
        CinemaProjectorBlockEntity projector = null;

        // Компьютер + Монитор
        if (beA instanceof ComputerBlockEntity c && beB instanceof MonitorBlockEntity m) {
            computer = c; monitor = m;
        } else if (beA instanceof MonitorBlockEntity m && beB instanceof ComputerBlockEntity c) {
            computer = c; monitor = m;
        }
        // Компьютер + Кино проектор
        else if (beA instanceof ComputerBlockEntity c && beB instanceof CinemaProjectorBlockEntity h) {
            computer = c; projector = h;
        } else if (beA instanceof CinemaProjectorBlockEntity h && beB instanceof ComputerBlockEntity c) {
            computer = c; projector = h;
        }
        else {
            player.sendSystemMessage(Component.literal(
                    "§eNeed: Computer + Monitor, or Computer + Cinema Projector.\n§7Got: " +
                    beA.getClass().getSimpleName() + " + " + beB.getClass().getSimpleName()));
            return false;
        }

        if (monitor != null) {
            computer.setConnectedMonitor(monitor.getBlockPos());
            monitor.connectTo(computer.getBlockPos());
            player.sendSystemMessage(Component.literal("§aMonitor connected!"));
        } else if (projector != null) {
            computer.setConnectedProjector(projector.getBlockPos());
            projector.connectTo(computer.getBlockPos());
            player.sendSystemMessage(Component.literal("§5Cinema Projector connected!"));
        }
        return true;
    }

    private void tryDisconnect(Level level, BlockPos a, BlockPos b) {
        for (BlockPos pos : new BlockPos[]{a, b}) {
            if (pos == null) continue;
            var be = level.getBlockEntity(pos);
            if (be instanceof ComputerBlockEntity c) {
                c.setConnectedMonitor(null);
                c.setConnectedProjector(null);
            }
            if (be instanceof MonitorBlockEntity m)             m.disconnect();
            if (be instanceof CinemaProjectorBlockEntity h)   h.disconnect();
        }
    }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableBlockEntity(pos, state);
    }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                    BlockEntityType<T> type) {
        return null; // Кабель не тикает
    }
}
