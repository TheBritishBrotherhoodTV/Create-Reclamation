package com.reclamation;

import com.reclamation.content.reclaimer.MechanicalReclaimerBlockEntity;
import com.reclamation.infrastructure.ponder.ReclamationPonderPlugin;
import com.reclamation.registry.ModBlockEntities;
import com.reclamation.registry.ModBlocks;
import com.reclamation.registry.ModCreativeTabs;
import com.reclamation.registry.ModItems;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CreateReclamation.MOD_ID)
public class CreateReclamation {
    public static final String MOD_ID = "create_reclamation";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
            .defaultCreativeTab(ModCreativeTabs.MAIN_TAB.getKey());

    public CreateReclamation(IEventBus modEventBus, net.neoforged.fml.ModContainer modContainer) {
        LOGGER.info("Initializing Create: Reclamation");

        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, com.reclamation.infrastructure.config.ReclamationConfig.SERVER_SPEC);

        ModCreativeTabs.register(modEventBus);
        com.reclamation.registry.ModDisplaySources.register();
        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        com.reclamation.registry.ModRecipeTypes.register(modEventBus);

        modEventBus.addListener(CreateReclamation::registerCapabilities);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(CreateReclamation::onClientSetup);
            modEventBus.addListener(CreateReclamation::onRegisterRenderers);
        }

        REGISTRATE.registerEventListeners(modEventBus);
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MECHANICAL_RECLAIMER.get(),
                (be, side) -> be.getItemHandler(side)
        );
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            PonderIndex.addPlugin(new ReclamationPonderPlugin());
        });
    }

    public static void onRegisterRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.MECHANICAL_RECLAIMER.get(),
                com.reclamation.content.reclaimer.MechanicalReclaimerRenderer::new
        );
    }
}
