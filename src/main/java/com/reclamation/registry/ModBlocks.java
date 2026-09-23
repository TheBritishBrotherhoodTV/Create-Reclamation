package com.reclamation.registry;

import com.reclamation.CreateReclamation;
import com.reclamation.content.reclaimer.MechanicalReclaimerBlock;
import com.simibubi.create.AllDisplaySources;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.material.MapColor;

public class ModBlocks {
    public static final BlockEntry<MechanicalReclaimerBlock> MECHANICAL_RECLAIMER = CreateReclamation.REGISTRATE
            .block("mechanical_reclaimer", MechanicalReclaimerBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F, 6.0F)
                    .noOcclusion())
            .transform(DisplaySource.displaySource(ModDisplaySources.RECLAIMER_PROGRESS))
            .transform(DisplaySource.displaySource(AllDisplaySources.KINETIC_SPEED))
            .transform(DisplaySource.displaySource(AllDisplaySources.KINETIC_STRESS))
            .simpleItem()
            .register();

    public static void register() {}
}
