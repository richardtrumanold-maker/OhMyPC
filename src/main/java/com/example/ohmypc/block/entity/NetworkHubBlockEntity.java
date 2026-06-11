package com.example.ohmypc.block.entity;

import com.example.ohmypc.filesystem.FileSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Сетевой хаб — разделяемое хранилище файлов для нескольких компьютеров.
 * Монтируется как /net/ в FileSystem компьютера при подключении кабелем.
 */
public class NetworkHubBlockEntity extends BlockEntity {

    private final FileSystem sharedFs;
    private final List<BlockPos> connectedComputers = new ArrayList<>();

    public NetworkHubBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NETWORK_HUB.get(), pos, state);
        // Shared FS хранится по позиции хаба — уникальна для каждого хаба
        this.sharedFs = new FileSystem("hub_" + pos.asLong());
    }

    public FileSystem getSharedFs() { return sharedFs; }

    public void addComputer(BlockPos computer) {
        if (!connectedComputers.contains(computer)) connectedComputers.add(computer);
    }
    public void removeComputer(BlockPos computer) { connectedComputers.remove(computer); }
    public List<BlockPos> getConnectedComputers() { return connectedComputers; }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        var list = new net.minecraft.nbt.LongArrayTag(
                connectedComputers.stream().mapToLong(BlockPos::asLong).toArray());
        tag.put("computers", list);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        connectedComputers.clear();
        if (tag.contains("computers")) {
            for (long l : tag.getLongArray("computers"))
                connectedComputers.add(BlockPos.of(l));
        }
    }
}
