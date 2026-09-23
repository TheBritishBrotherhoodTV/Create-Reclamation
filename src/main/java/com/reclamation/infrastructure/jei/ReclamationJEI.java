package com.reclamation.infrastructure.jei;

import com.reclamation.CreateReclamation;
import com.reclamation.content.recipe.ReclamationRecipeHelper;
import com.reclamation.registry.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class ReclamationJEI implements IModPlugin {

    public static final ResourceLocation PLUGIN_ID = CreateReclamation.asResource("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ReclaimingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModBlocks.MECHANICAL_RECLAIMER.asStack(), ReclaimingCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.PRECISION_RECLAIMER.asStack(), ReclaimingCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.INDUSTRIAL_RECLAIMER.asStack(), ReclaimingCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.ADVANCED_RECLAIMER.asStack(), ReclaimingCategory.TYPE);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        List<JEIReclaimRecipe> recipes = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack sampleStack = new ItemStack(item);
            ReclamationRecipeHelper.findReclamation(level, sampleStack).ifPresent(result -> {
                ItemStack inputStack = new ItemStack(item, result.consumedInputCount());
                recipes.add(new JEIReclaimRecipe(inputStack, result.possibleOutputs(), result.processingTime()));
            });
        }

        registration.addRecipes(ReclaimingCategory.TYPE, recipes);
    }
}

