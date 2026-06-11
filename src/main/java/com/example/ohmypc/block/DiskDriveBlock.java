package com.example.ohmypc.block;

import com.example.ohmypc.block.entity.DiskDriveBlockEntity;
import com.example.ohmypc.block.entity.ModBlockEntities;
import com.example.ohmypc.item.FloppyDiskItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class DiskDriveBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty   HAS_DISK = BlockStateProperties.POWERED;

    public DiskDriveBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.0F, 8.0F)
                .sound(SoundType.METAL));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HAS_DISK, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING, HAS_DISK);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(HAS_DISK, false);
    }

    @Override
    public RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }

    /**
     * ПКМ с диском в руке — вставить.
     * ПКМ пустой рукой — вытащить диск (дроп в мир).
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                  Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof DiskDriveBlockEntity drive)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);

        if (!held.isEmpty() && held.getItem() instanceof FloppyDiskItem) {
            // Вставить диск
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            if (drive.hasDisk()) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§eDisk drive already has a disk!"));
                return InteractionResult.CONSUME;
            }
            ItemStack toInsert = held.split(1);
            drive.insertDisk(toInsert);
            level.setBlock(pos, state.setValue(HAS_DISK, true), 3);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§aInserted §f" + ((FloppyDiskItem)toInsert.getItem()).getFloppyColor().getName() + " §adisk"));
            return InteractionResult.CONSUME;
        }

        if (held.isEmpty() && drive.hasDisk()) {
            // Вытащить диск
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            ItemStack ejected = drive.ejectDisk();
            level.setBlock(pos, state.setValue(HAS_DISK, false), 3);
            if (!player.addItem(ejected)) {
                // Если инвентарь полон — дроп
                Block.popResource(level, pos.above(), ejected);
            }
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7Disk ejected."));
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DiskDriveBlockEntity(pos, state);
    }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.DISK_DRIVE.get(), DiskDriveBlockEntity::tick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof DiskDriveBlockEntity drive && drive.hasDisk()) {
                Block.popResource(level, pos, drive.ejectDisk());
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
