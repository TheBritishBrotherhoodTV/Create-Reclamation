package com.reclamation.infrastructure.jei;

import com.reclamation.CreateReclamation;
import com.reclamation.content.recipe.ReclamationRecipeHelper;
import com.reclamation.content.recipe.ReclaimingRecipe;
import com.reclamation.registry.ModBlocks;
import com.reclamation.registry.ModRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        List<JEIReclaimRecipe> recipes = new ArrayList<>();
        Set<Item> processedInputs = new HashSet<>();

        // 1. Load explicit Reclaiming recipes
        List<RecipeHolder<ReclaimingRecipe>> customRecipes =
                level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.RECLAIMING.get());

        for (RecipeHolder<ReclaimingRecipe> holder : customRecipes) {
            ReclaimingRecipe custom = holder.value();
            for (ItemStack matchingInput : custom.getIngredient().getItems()) {
                if (!matchingInput.isEmpty()) {
                    ItemStack in = matchingInput.copy();
                    in.setCount(custom.getInputCount());
                    List<ReclamationRecipeHelper.SalvageOutput> outputs = new ArrayList<>();
                    for (ReclaimingRecipe.ChanceOutput out : custom.getResults()) {
                        outputs.add(new ReclamationRecipeHelper.SalvageOutput(out.stack(), out.chance()));
                    }
                    recipes.add(new JEIReclaimRecipe(in, outputs, custom.getProcessingTime()));
                    processedInputs.add(in.getItem());
                }
            }
        }

        // 2. Load dynamic Crafting recipes
        List<RecipeHolder<CraftingRecipe>> craftingRecipes =
                level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING);

        for (RecipeHolder<CraftingRecipe> holder : craftingRecipes) {
            CraftingRecipe crafting = holder.value();
            ItemStack result = crafting.getResultItem(level.registryAccess());

            if (!result.isEmpty() && !processedInputs.contains(result.getItem())) {
                NonNullList<Ingredient> ingredients = crafting.getIngredients();
                List<ReclamationRecipeHelper.SalvageOutput> outputs = new ArrayList<>();
                int nonEmptyIngredientCount = 0;

                for (Ingredient ing : ingredients) {
                    if (ing.isEmpty()) continue;
                    nonEmptyIngredientCount++;
                    ItemStack[] matching = ing.getItems();
                    if (matching.length > 0 && !matching[0].isEmpty()) {
                        ItemStack out = matching[0].copy();
                        out.setCount(1);
                        outputs.add(new ReclamationRecipeHelper.SalvageOutput(out, 0.85f));
                    }
                }

                if (nonEmptyIngredientCount > 1 && !outputs.isEmpty()) {
                    int processingTime = Math.max(40, outputs.size() * 20);
                    recipes.add(new JEIReclaimRecipe(result.copy(), outputs, processingTime));
                    processedInputs.add(result.getItem());
                }
            }
        }

        registration.addRecipes(ReclaimingCategory.TYPE, recipes);
    }
}
