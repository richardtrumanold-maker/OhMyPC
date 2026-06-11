package com.example.ohmypc.block;

import com.example.ohmypc.block.entity.ComputerBlockEntity;
import com.example.ohmypc.menu.ComputerAssemblyMenu;
import com.example.ohmypc.menu.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class ComputerBlock extends BaseEntityBlock {

    public static final DirectionProperty                FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<ComputerState> STATE  =
            EnumProperty.create("state", ComputerState.class);

    public ComputerBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.5F, 10.0F)
                .sound(SoundType.METAL)
                .lightLevel(s -> s.getValue(STATE) == ComputerState.ON ? 4 : 0)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(STATE, ComputerState.OFF));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STATE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(STATE, ComputerState.OFF);
    }

    /** Обновить blockstate индикатора */
    public static void setState(Level level, BlockPos pos, ComputerState state) {
        BlockState current = level.getBlockState(pos);
        if (current.is(level.getBlockState(pos).getBlock())) {
            level.setBlock(pos, current.setValue(STATE, state), 3);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                  Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ComputerBlockEntity cbe)) return InteractionResult.PASS;

        // Shift+ПКМ = всегда меню сборки
        // ПКМ пустой рукой + ON = терминал
        boolean openTerminal = player.getItemInHand(hand).isEmpty()
                && state.getValue(STATE) == ComputerState.ON
                && !player.isShiftKeyDown();

        if (openTerminal) {
            NetworkHooks.openScreen((ServerPlayer) player, new MenuProvider() {
                @Override public Component getDisplayName() {
                    return Component.literal("⌨ " + cbe.getComputerName());
                }
                @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                    return new com.example.ohmypc.menu.LuaTerminalMenu(id, inv, pos);
                }
            }, pos);
        } else {
            NetworkHooks.openScreen((ServerPlayer) player, new MenuProvider() {
                @Override public Component getDisplayName() {
                    return Component.literal("⚙ " + cbe.getComputerName() + " — Assembly");
                }
                @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                    return new ComputerAssemblyMenu(id, inv, pos, level);
                }
            }, pos);
        }
        return InteractionResult.CONSUME;
    }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ComputerBlockEntity(pos, state);
    }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                    BlockEntityType<T> type) {
        return createTickerHelper(type,
                com.example.ohmypc.block.entity.ModBlockEntities.COMPUTER.get(),
                ComputerBlockEntity::tick);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                             @Nullable LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ComputerBlockEntity cbe) cbe.setCustomName(stack.getHoverName());
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                          BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ComputerBlockEntity cbe) {
                cbe.shutdown();
                net.minecraft.world.Containers.dropContents(level, pos, cbe);
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
