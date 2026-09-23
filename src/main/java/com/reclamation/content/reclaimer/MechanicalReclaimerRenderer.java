package com.reclamation.content.reclaimer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class MechanicalReclaimerRenderer extends KineticBlockEntityRenderer<MechanicalReclaimerBlockEntity> {

    public MechanicalReclaimerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(MechanicalReclaimerBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        // Render fallback kinetic shaft if Flywheel instancing is not active
        if (!VisualizationManager.supportsVisualization(be.getLevel())) {
            BlockState state = be.getBlockState();
            Direction facing = state.getValue(MechanicalReclaimerBlock.HORIZONTAL_FACING);
            Direction opposite = facing.getOpposite();
            BlockPos lightPos = be.getBlockPos().relative(opposite);
            int shaftLight = LevelRenderer.getLightColor(be.getLevel(), lightPos);

            SuperByteBuffer shaft = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, opposite);
            standardKineticRotationTransform(shaft, be, shaftLight).renderInto(ms, buffer.getBuffer(RenderType.solid()));
        }

        // Render in-world item on top plate (render active input, or resting output results)
        ItemStack displayStack = be.getInputStack();
        if (displayStack.isEmpty()) {
            for (int i = 0; i < be.outputInv.getSlots(); i++) {
                ItemStack out = be.outputInv.getStackInSlot(i);
                if (!out.isEmpty()) {
                    displayStack = out;
                    break;
                }
            }
        }

        if (!displayStack.isEmpty()) {
            ms.pushPose();

            float renderTime = AnimationTickHolder.getRenderTime(be.getLevel());
            float speed = be.getSpeed();
            float vibration = 0;
            float rotationAngle = 0;

            if (speed != 0 && !be.getInputStack().isEmpty()) {
                // Subtle mechanical dismantling agitation while actively processing
                vibration = Mth.sin(renderTime * 0.8f) * 0.02f;
                rotationAngle = Mth.sin(renderTime * 0.3f) * 8.0f;
            }

            ms.translate(0.5, 1.02 + vibration, 0.5);
            ms.mulPose(Axis.YP.rotationDegrees(rotationAngle));
            ms.scale(0.55f, 0.55f, 0.55f);

            int itemLight = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().above());
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            itemRenderer.renderStatic(displayStack, ItemDisplayContext.FIXED, itemLight, overlay, ms, buffer, be.getLevel(), 0);

            ms.popPose();
        }
    }

    @Override
    protected SuperByteBuffer getRotatedModel(MechanicalReclaimerBlockEntity be, BlockState state) {
        Direction facing = state.getValue(MechanicalReclaimerBlock.HORIZONTAL_FACING);
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, facing.getOpposite());
    }
}
