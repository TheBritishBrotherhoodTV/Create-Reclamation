package com.reclamation.content.reclaimer;

import com.reclamation.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class MechanicalReclaimerBlock extends HorizontalKineticBlock implements IBE<MechanicalReclaimerBlockEntity> {

    public MechanicalReclaimerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        return defaultBlockState().setValue(HORIZONTAL_FACING, facing);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(HORIZONTAL_FACING).getOpposite();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        return onBlockEntityUseItemOn(level, pos, be -> {
            // First check if there are finished outputs to extract
            boolean extractedAny = false;
            for (int i = 0; i < be.outputInv.getSlots(); i++) {
                ItemStack out = be.outputInv.getStackInSlot(i);
                if (!out.isEmpty()) {
                    if (!player.getInventory().add(out)) {
                        player.drop(out, false);
                    }
                    be.outputInv.setStackInSlot(i, ItemStack.EMPTY);
                    extractedAny = true;
                }
            }
            if (extractedAny) {
                be.setChanged();
                be.sendData();
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);
                return ItemInteractionResult.SUCCESS;
            }

            // Otherwise, try to insert held item
            if (!stack.isEmpty()) {
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(be.inputInv, stack.copy(), false);
                int insertedCount = stack.getCount() - remainder.getCount();
                if (insertedCount > 0) {
                    stack.shrink(insertedCount);
                    be.setChanged();
                    be.sendData();
                    level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.BLOCKS, 0.8f, 1.0f);
                    return ItemInteractionResult.SUCCESS;
                }
            }

            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        });
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        return onBlockEntityUse(level, pos, be -> {
            boolean extractedAny = false;
            for (int i = 0; i < be.outputInv.getSlots(); i++) {
                ItemStack out = be.outputInv.getStackInSlot(i);
                if (!out.isEmpty()) {
                    if (!player.getInventory().add(out)) {
                        player.drop(out, false);
                    }
                    be.outputInv.setStackInSlot(i, ItemStack.EMPTY);
                    extractedAny = true;
                }
            }
            if (extractedAny) {
                be.setChanged();
                be.sendData();
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);
                return InteractionResult.SUCCESS;
            }

            ItemStack input = be.getInputStack();
            if (!input.isEmpty()) {
                if (player.isShiftKeyDown()) {
                    if (!player.getInventory().add(input)) {
                        player.drop(input, false);
                    }
                    be.setInputStack(ItemStack.EMPTY);
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);
                    return InteractionResult.SUCCESS;
                } else {
                    float progress = be.getProcessingProgress() * 100;
                    player.displayClientMessage(
                            Component.literal("Reclaiming: " + input.getHoverName().getString()
                                    + String.format(" (%.0f%%)", progress)),
                            true
                    );
                    return InteractionResult.SUCCESS;
                }
            }

            player.displayClientMessage(Component.literal("Reclaimer is empty. Insert a crafted item to dismantle."), true);
            return InteractionResult.SUCCESS;
        });
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            withBlockEntityDo(level, pos, MechanicalReclaimerBlockEntity::dropAllContents);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public Class<MechanicalReclaimerBlockEntity> getBlockEntityClass() {
        return MechanicalReclaimerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MechanicalReclaimerBlockEntity> getBlockEntityType() {
        return ModBlockEntities.MECHANICAL_RECLAIMER.get();
    }
}
