package com.reclamation.registry;

import com.reclamation.CreateReclamation;
import com.reclamation.content.reclaimer.MechanicalReclaimerBlockEntity;
import com.reclamation.content.reclaimer.MechanicalReclaimerRenderer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

public class ModBlockEntities {
    public static final BlockEntityEntry<MechanicalReclaimerBlockEntity> MECHANICAL_RECLAIMER = CreateReclamation.REGISTRATE
            .blockEntity("mechanical_reclaimer", MechanicalReclaimerBlockEntity::new)
            .visual(() -> OrientedRotatingVisual.backHorizontal(AllPartialModels.SHAFT_HALF))
            .validBlocks(ModBlocks.MECHANICAL_RECLAIMER)
            .renderer(() -> MechanicalReclaimerRenderer::new)
            .register();

    public static void register() {
    }
}
