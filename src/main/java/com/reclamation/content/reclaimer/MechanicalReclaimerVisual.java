package com.reclamation.content.reclaimer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.function.Consumer;

public class MechanicalReclaimerVisual extends KineticBlockEntityVisual<MechanicalReclaimerBlockEntity> {

    protected final RotatingInstance shaft;
    protected final Direction opposite;

    public MechanicalReclaimerVisual(VisualizationContext context, MechanicalReclaimerBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        Direction facing = blockEntity.getBlockState().getValue(MechanicalReclaimerBlock.HORIZONTAL_FACING);
        this.opposite = facing.getOpposite();

        this.shaft = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF))
                .createInstance();

        this.shaft.setup(blockEntity)
                .setPosition(getVisualPosition())
                .rotateToFace(Direction.SOUTH, opposite)
                .setChanged();

        updateLight(partialTick);
    }

    public static SimpleBlockEntityVisualizer.Factory<MechanicalReclaimerBlockEntity> create() {
        return MechanicalReclaimerVisual::new;
    }

    @Override
    public void update(float pt) {
        shaft.setup(blockEntity).setChanged();
    }

    @Override
    public void updateLight(float pt) {
        BlockPos lightPos = pos.relative(opposite);
        relight(lightPos, shaft);
    }

    @Override
    protected void _delete() {
        shaft.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(shaft);
    }
}
