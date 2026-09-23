package com.reclamation.registry;

import com.reclamation.CreateReclamation;
import com.reclamation.content.reclaimer.MechanicalReclaimerBlockEntity;
import com.reclamation.content.reclaimer.MechanicalReclaimerRenderer;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

public class ModBlockEntities {
    public static final BlockEntityEntry<MechanicalReclaimerBlockEntity> MECHANICAL_RECLAIMER = CreateReclamation.REGISTRATE
            .blockEntity("mechanical_reclaimer", MechanicalReclaimerBlockEntity::new)
            .validBlocks(ModBlocks.MECHANICAL_RECLAIMER)
            .renderer(() -> MechanicalReclaimerRenderer::new)
            .register();

    public static void register() {
    }
}
