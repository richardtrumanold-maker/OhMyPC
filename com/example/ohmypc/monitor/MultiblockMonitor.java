package com.example.ohmypc.monitor;

import com.example.ohmypc.block.ModBlocks;
import com.example.ohmypc.block.entity.MonitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Мультиблочная система монитора.
 *
 * Правила:
 *  - Все блоки монитора в прямоугольнике должны смотреть в одну сторону (FACING).
 *  - Максимум 12 (ширина) × 24 (высота) блоков.
 *  - «Мастер» — блок в левом нижнем углу (с точки зрения смотрящего на экран).
 *  - Все остальные блоки («слейвы») знают позицию мастера.
 *  - Рендеринг делает только мастер — он рисует прямоугольник на весь мультиблок.
 *
 * Алгоритм сканирования:
 *  1. От размещённого/изменённого блока идём влево/вниз до границы.
 *  2. Растём вправо и вверх, пока все ячейки заняты мониторами с тем же facing.
 *  3. Назначаем мастера, записываем в каждый BlockEntity его роль и позицию мастера.
 */
public class MultiblockMonitor {

    public static final int MAX_WIDTH  = 12;
    public static final int MAX_HEIGHT = 24;

    // ── Публичный вход ────────────────────────────────────────────────────────

    /** Вызывать при установке/изменении монитора */
    public static void reform(Level level, BlockPos changed) {
        if (level.isClientSide()) return;

        MonitorBlockEntity origin = getMonitor(level, changed);
        if (origin == null) return;

        // Сначала разбиваем все мультиблоки, которые касаются данного блока
        dissolveAround(level, changed, origin.getMasterPos());

        // Затем собираем новый мультиблок
        Direction facing = level.getBlockState(changed).getValue(
                com.example.ohmypc.block.MonitorBlock.FACING);

        // Находим нижний левый угол прямоугольника
        BlockPos bottomLeft = findBottomLeft(level, changed, facing);
        // Находим размеры
        int[] size = measureRect(level, bottomLeft, facing);
        int w = size[0], h = size[1];

        // Назначаем мастера и слейвов
        List<BlockPos> members = collectRect(bottomLeft, facing, w, h);
        BlockPos masterPos = bottomLeft; // нижний левый = мастер

        for (BlockPos pos : members) {
            MonitorBlockEntity mbe = getMonitor(level, pos);
            if (mbe == null) continue;
            boolean isMaster = pos.equals(masterPos);
            mbe.setMultiblock(masterPos, w, h, isMaster,
                    getLocalCoords(pos, masterPos, facing));
            mbe.setChanged();
        }
    }

    /** Вызывать при удалении монитора */
    public static void dissolve(Level level, BlockPos removed) {
        if (level.isClientSide()) return;
        // Соседей бывшего мультиблока — перестроить
        for (Direction d : Direction.values()) {
            BlockPos nb = removed.relative(d);
            MonitorBlockEntity mbe = getMonitor(level, nb);
            if (mbe != null) reform(level, nb);
        }
    }

    // ── Вспомогательные методы ────────────────────────────────────────────────

    /** Идём влево и вниз (в системе координат экрана) до упора */
    private static BlockPos findBottomLeft(Level level, BlockPos start, Direction facing) {
        Direction left   = facing.getClockWise();   // вправо для наблюдателя = clockwise для блока
        Direction right  = facing.getCounterClockWise();
        Direction down   = Direction.DOWN;

        BlockPos pos = start;

        // Идём вниз
        while (true) {
            BlockPos next = pos.relative(down);
            if (!isSameMonitor(level, next, facing)) break;
            pos = next;
        }
        // Идём влево (для наблюдателя)
        while (true) {
            BlockPos next = pos.relative(right); // right от facing = left для зрителя
            if (!isSameMonitor(level, next, facing)) break;
            pos = next;
        }
        return pos;
    }

    /** Измеряем ширину и высоту прямоугольника от нижнего левого */
    private static int[] measureRect(Level level, BlockPos bottomLeft, Direction facing) {
        Direction leftDir = facing.getCounterClockWise(); // для расширения вправо (зрителю)
        Direction up      = Direction.UP;

        int w = 0, h = 0;

        // Высота — вверх
        BlockPos col = bottomLeft;
        while (isSameMonitor(level, col, facing) && h < MAX_HEIGHT) {
            h++;
            col = col.relative(up);
        }

        // Ширина — вправо (зрителю), проверяем всю высоту колонки
        BlockPos row = bottomLeft;
        while (true) {
            if (!isSameMonitor(level, row, facing)) break;
            // Проверяем всю вертикаль этой колонки
            boolean colFull = true;
            BlockPos check = row;
            for (int y = 0; y < h; y++) {
                if (!isSameMonitor(level, check, facing)) { colFull = false; break; }
                check = check.relative(Direction.UP);
            }
            if (!colFull) break;
            if (w >= MAX_WIDTH) break;
            w++;
            row = row.relative(leftDir);
        }

        return new int[]{ Math.max(1, w), Math.max(1, h) };
    }

    /** Собирает все позиции в прямоугольнике */
    private static List<BlockPos> collectRect(BlockPos bottomLeft, Direction facing, int w, int h) {
        List<BlockPos> list = new ArrayList<>();
        Direction rightDir = facing.getCounterClockWise();
        for (int col = 0; col < w; col++) {
            BlockPos colBase = bottomLeft.relative(rightDir, col);
            for (int row = 0; row < h; row++) {
                list.add(colBase.relative(Direction.UP, row));
            }
        }
        return list;
    }

    /** Локальные координаты блока внутри мультиблока (0-based, x=колонка, y=строка снизу) */
    private static int[] getLocalCoords(BlockPos pos, BlockPos master, Direction facing) {
        Direction rightDir = facing.getCounterClockWise();
        // Разница по горизонтали
        BlockPos diff = pos.subtract(master);
        // Проекция на rightDir
        int x = (int) Math.round(
                diff.getX() * rightDir.getStepX() +
                diff.getZ() * rightDir.getStepZ());
        int y = diff.getY();
        return new int[]{ x, y };
    }

    /** Разбиваем мультиблоки вокруг позиции */
    private static void dissolveAround(Level level, BlockPos center, BlockPos knownMaster) {
        if (knownMaster == null) return;
        // Сбрасываем весь бывший мультиблок по мастеру
        MonitorBlockEntity master = getMonitor(level, knownMaster);
        if (master == null) return;
        int oldW = master.getWidth(), oldH = master.getHeight();
        Direction facing = level.getBlockState(knownMaster).getValue(
                com.example.ohmypc.block.MonitorBlock.FACING);
        collectRect(knownMaster, facing, oldW, oldH).forEach(p -> {
            MonitorBlockEntity mbe = getMonitor(level, p);
            if (mbe != null) {
                mbe.resetMultiblock();
                mbe.setChanged();
            }
        });
    }

    private static boolean isSameMonitor(Level level, BlockPos pos, Direction facing) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(ModBlocks.MONITOR.get())) return false;
        return state.getValue(com.example.ohmypc.block.MonitorBlock.FACING) == facing;
    }

    private static MonitorBlockEntity getMonitor(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof MonitorBlockEntity mbe) return mbe;
        return null;
    }
}
