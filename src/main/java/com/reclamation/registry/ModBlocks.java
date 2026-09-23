package com.reclamation.registry;

import com.reclamation.CreateReclamation;
import com.reclamation.content.reclaimer.MechanicalReclaimerBlock;
import com.reclamation.content.reclaimer.ReclaimerTier;
import com.simibubi.create.AllDisplaySources;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.material.MapColor;

public class ModBlocks {

    public static final BlockEntry<MechanicalReclaimerBlock> MECHANICAL_RECLAIMER = CreateReclamation.REGISTRATE
            .block("mechanical_reclaimer", p -> new MechanicalReclaimerBlock(p, ReclaimerTier.MECHANICAL))
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

    public static final BlockEntry<MechanicalReclaimerBlock> PRECISION_RECLAIMER = CreateReclamation.REGISTRATE
            .block("precision_reclaimer", p -> new MechanicalReclaimerBlock(p, ReclaimerTier.PRECISION))
            .initialProperties(SharedProperties::copperMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .requiresCorrectToolForDrops()
                    .strength(3.5F, 6.0F)
                    .noOcclusion())
            .transform(DisplaySource.displaySource(ModDisplaySources.RECLAIMER_PROGRESS))
            .transform(DisplaySource.displaySource(AllDisplaySources.KINETIC_SPEED))
            .transform(DisplaySource.displaySource(AllDisplaySources.KINETIC_STRESS))
            .simpleItem()
            .register();

    public static final BlockEntry<MechanicalReclaimerBlock> INDUSTRIAL_RECLAIMER = CreateReclamation.REGISTRATE
            .block("industrial_reclaimer", p -> new MechanicalReclaimerBlock(p, ReclaimerTier.INDUSTRIAL))
            .initialProperties(SharedProperties::copperMetal)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_CYAN)
                    .requiresCorrectToolForDrops()
                    .strength(4.0F, 8.0F)
                    .noOcclusion())
            .transform(DisplaySource.displaySource(ModDisplaySources.RECLAIMER_PROGRESS))
            .transform(DisplaySource.displaySource(AllDisplaySources.KINETIC_SPEED))
            .transform(DisplaySource.displaySource(AllDisplaySources.KINETIC_STRESS))
            .simpleItem()
            .register();

    public static final BlockEntry<MechanicalReclaimerBlock> ADVANCED_RECLAIMER = CreateReclamation.REGISTRATE
            .block("advanced_reclaimer", p -> new MechanicalReclaimerBlock(p, ReclaimerTier.ADVANCED))
            .initialProperties(SharedProperties::copperMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_PURPLE)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 10.0F)
                    .noOcclusion())
            .transform(DisplaySource.displaySource(ModDisplaySources.RECLAIMER_PROGRESS))
            .transform(DisplaySource.displaySource(AllDisplaySources.KINETIC_SPEED))
            .transform(DisplaySource.displaySource(AllDisplaySources.KINETIC_STRESS))
            .simpleItem()
            .register();

    public static void register() {}
}
