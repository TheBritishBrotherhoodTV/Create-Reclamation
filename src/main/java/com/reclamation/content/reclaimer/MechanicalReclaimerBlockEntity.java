package com.reclamation.content.reclaimer;

import com.reclamation.content.recipe.ReclamationRecipeHelper;
import com.reclamation.infrastructure.config.ReclamationConfig;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class MechanicalReclaimerBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation {

    public final ItemStackHandler inputInv = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (level == null || stack.isEmpty()) return false;
            return ReclamationRecipeHelper.findReclamation(level, stack).isPresent();
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sendData();
        }
    };

    public final ItemStackHandler outputInv = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sendData();
        }
    };

    private final IItemHandler capability = new CombinedInvWrapper(inputInv, outputInv) {
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == 0) return ItemStack.EMPTY; // Do not extract raw inputs while processing
            return super.extractItem(slot, amount, simulate);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0) return stack; // Only insert into input slot
            return super.insertItem(slot, stack, simulate);
        }
    };

    private float processingTicks = 0;
    private float totalProcessingTicks = 0;
    private int requiredInputCount = 1;

    public MechanicalReclaimerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public IItemHandler getItemHandler(@Nullable Direction side) {
        return capability;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(new DirectBeltInputBehaviour(this)
                .allowingBeltFunnels()
                .setInsertionHandler((transportedStack, side, simulate) -> {
                    ItemStack stack = transportedStack.stack;
                    if (level == null || ReclamationRecipeHelper.findReclamation(level, stack).isEmpty()) {
                        return stack;
                    }
                    return ItemHandlerHelper.insertItemStacked(inputInv, stack, simulate);
                }));
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null) {
            return;
        }

        float speed = getSpeed();
        double minSpeed = 16.0;
        try {
            if (ReclamationConfig.SERVER_SPEC.isLoaded()) {
                minSpeed = ReclamationConfig.SERVER.minOperatingSpeed.get();
            }
        } catch (Exception ignored) {}

        if (Math.abs(speed) < minSpeed) {
            return;
        }

        ItemStack inputStack = inputInv.getStackInSlot(0);

        if (inputStack.isEmpty()) {
            if (processingTicks != 0 || totalProcessingTicks != 0) {
                processingTicks = 0;
                totalProcessingTicks = 0;
            }
            return;
        }

        // Client-side visual progress advancement
        if (level.isClientSide) {
            if (totalProcessingTicks > 0) {
                float speedFactor = Math.abs(speed) / 16.0f;
                processingTicks = Math.min(totalProcessingTicks, processingTicks + speedFactor);
            }
            return;
        }

        // Server-side processing
        if (totalProcessingTicks <= 0) {
            Optional<ReclamationRecipeHelper.ReclaimedResult> recipeOpt =
                    ReclamationRecipeHelper.findReclamation(level, inputStack);
            if (recipeOpt.isPresent()) {
                ReclamationRecipeHelper.ReclaimedResult recipe = recipeOpt.get();
                this.totalProcessingTicks = recipe.processingTime();
                this.processingTicks = 0;
                this.requiredInputCount = recipe.consumedInputCount();
                sendData();
            } else {
                return;
            }
        }

        // Speed factor: 16 RPM is 1x normal speed
        float speedFactor = Math.abs(speed) / 16.0f;
        processingTicks += speedFactor;

        // Sync to client periodically every 8 ticks
        if (level.getGameTime() % 8 == 0) {
            sendData();
        }

        // Periodic mechanical ratcheting / grinding sound effects while running
        int soundInterval = Math.max(5, (int) (18 / Math.max(0.5f, speedFactor)));
        if (level.getGameTime() % soundInterval == 0) {
            float pitch = 0.85f + Math.min(0.8f, Math.abs(speed) / 256.0f) * 0.5f;
            level.playSound(null, worldPosition, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.25f, pitch);
        }

        // Spawn dismantle particles and mechanical sparks occasionally while running
        if (level instanceof ServerLevel serverLevel && level.random.nextFloat() < 0.35f) {
            serverLevel.sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, inputStack),
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.05,
                    worldPosition.getZ() + 0.5,
                    4, 0.12, 0.08, 0.12, 0.05
            );
            if (level.random.nextFloat() < 0.3f) {
                serverLevel.sendParticles(
                        ParticleTypes.CRIT,
                        worldPosition.getX() + 0.5,
                        worldPosition.getY() + 1.08,
                        worldPosition.getZ() + 0.5,
                        2, 0.08, 0.05, 0.08, 0.02
                );
            }
        }

        if (processingTicks >= totalProcessingTicks) {
            Optional<ReclamationRecipeHelper.ReclaimedResult> recipeOpt =
                    ReclamationRecipeHelper.findReclamation(level, inputStack);
            if (recipeOpt.isPresent()) {
                ReclamationRecipeHelper.ReclaimedResult recipe = recipeOpt.get();
                inputInv.extractItem(0, recipe.consumedInputCount(), false);

                List<ItemStack> rolledOutputs = recipe.rollOutputs(level.random);
                for (ItemStack out : rolledOutputs) {
                    ItemHandlerHelper.insertItemStacked(outputInv, out.copy(), false);
                }

                level.playSound(null, worldPosition, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.6f, 1.2f);
                level.playSound(null, worldPosition, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.8f, 0.9f);
            }

            processingTicks = 0;
            totalProcessingTicks = 0;
            setChanged();
            sendData();
        }
    }

    public ItemStack getInputStack() {
        return inputInv.getStackInSlot(0);
    }

    public void setInputStack(ItemStack stack) {
        inputInv.setStackInSlot(0, stack);
        this.processingTicks = 0;
        this.totalProcessingTicks = 0;
        setChanged();
        sendData();
    }

    public void dropAllContents() {
        if (level == null || level.isClientSide) return;

        for (int i = 0; i < inputInv.getSlots(); i++) {
            ItemStack stack = inputInv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
                inputInv.setStackInSlot(i, ItemStack.EMPTY);
            }
        }

        for (int i = 0; i < outputInv.getSlots(); i++) {
            ItemStack stack = outputInv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
                outputInv.setStackInSlot(i, ItemStack.EMPTY);
            }
        }

        setChanged();
        sendData();
    }

    public float getProcessingProgress() {
        if (totalProcessingTicks <= 0) return 0;
        return Math.min(1.0f, processingTicks / totalProcessingTicks);
    }

    @Override
    public float calculateStressApplied() {
        try {
            if (ReclamationConfig.SERVER_SPEC.isLoaded()) {
                return ReclamationConfig.SERVER.stressImpact.get().floatValue();
            }
        } catch (Exception ignored) {}
        return 8.0f;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        ItemStack input = getInputStack();
        int outputCount = 0;
        for (int i = 0; i < outputInv.getSlots(); i++) {
            ItemStack out = outputInv.getStackInSlot(i);
            if (!out.isEmpty()) {
                outputCount += out.getCount();
            }
        }

        if (!input.isEmpty()) {
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("create_reclamation.goggles.reclaiming", input.getHoverName())
                    .withStyle(ChatFormatting.GOLD));

            float progress = getProcessingProgress();
            int percent = Math.round(progress * 100);
            int totalBars = 10;
            int filled = Math.min(totalBars, Math.round(progress * totalBars));
            String bar = "[" + "=".repeat(filled) + (filled < totalBars ? ">" : "") + " ".repeat(Math.max(0, totalBars - filled - (filled < totalBars ? 1 : 0))) + "]";

            tooltip.add(Component.literal("  " + percent + "% ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal(bar).withStyle(ChatFormatting.WHITE)));
        } else if (outputCount > 0) {
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("Reclamation Complete")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("create_reclamation.goggles.idle")
                    .withStyle(ChatFormatting.GRAY));
        }

        if (outputCount > 0) {
            tooltip.add(Component.translatable("create_reclamation.goggles.outputs", outputCount)
                    .withStyle(ChatFormatting.AQUA));
        }

        return true;
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if (compound.contains("InputInventory")) {
            inputInv.deserializeNBT(registries, compound.getCompound("InputInventory"));
        }
        if (compound.contains("OutputInventory")) {
            outputInv.deserializeNBT(registries, compound.getCompound("OutputInventory"));
        }

        processingTicks = compound.getFloat("ProcessingTicks");
        totalProcessingTicks = compound.getFloat("TotalProcessingTicks");
        requiredInputCount = compound.getInt("RequiredInputCount");
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.put("InputInventory", inputInv.serializeNBT(registries));
        compound.put("OutputInventory", outputInv.serializeNBT(registries));

        compound.putFloat("ProcessingTicks", processingTicks);
        compound.putFloat("TotalProcessingTicks", totalProcessingTicks);
        compound.putInt("RequiredInputCount", requiredInputCount);
    }
}
