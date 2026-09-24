package com.reclamation.infrastructure.ponder;

import com.reclamation.CreateReclamation;
import com.reclamation.registry.ModBlocks;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class ReclamationPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return CreateReclamation.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ResourceLocation> sceneHelper = helper.withKeyFunction(r -> r);
        sceneHelper.forComponents(
                ModBlocks.MECHANICAL_RECLAIMER.getId(),
                ModBlocks.PRECISION_RECLAIMER.getId(),
                ModBlocks.INDUSTRIAL_RECLAIMER.getId(),
                ModBlocks.ADVANCED_RECLAIMER.getId()
        )
                .addStoryBoard("mechanical_reclaimer", ReclamationPonders::reclaimerTutorial, ReclamationPonderTags.RECLAMATION)
                .addStoryBoard("mechanical_reclaimer", ReclamationPonders::reclaimerAutomation, ReclamationPonderTags.RECLAMATION);

    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        helper.registerTag(ReclamationPonderTags.RECLAMATION)
                .item(ModBlocks.MECHANICAL_RECLAIMER.get())
                .title("Mechanical Reclamation")
                .description("Dismantle manufactured items into constituent ingredients using Create kinetics.")
                .addToIndex()
                .register();

        helper.addTagToComponent(ModBlocks.MECHANICAL_RECLAIMER.getId(), ReclamationPonderTags.RECLAMATION);
        helper.addTagToComponent(ModBlocks.PRECISION_RECLAIMER.getId(), ReclamationPonderTags.RECLAMATION);
        helper.addTagToComponent(ModBlocks.INDUSTRIAL_RECLAIMER.getId(), ReclamationPonderTags.RECLAMATION);
        helper.addTagToComponent(ModBlocks.ADVANCED_RECLAIMER.getId(), ReclamationPonderTags.RECLAMATION);
    }
}

