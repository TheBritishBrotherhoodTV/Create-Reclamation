package com.reclamation.content.recipe;

import com.reclamation.infrastructure.config.ReclamationConfig;
import com.reclamation.registry.ModItems;
import com.reclamation.registry.ModRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReclamationRecipeHelper {

    public record SalvageOutput(ItemStack stack, float chance) {}

    public record ReclaimedResult(int consumedInputCount, List<SalvageOutput> possibleOutputs, int processingTime) {
        
        /**
         * Resolves the actual item drops for a completed dismantling operation.
         * Components have an 85% recovery rate; fractured parts yield Salvaged Scrap.
         */
        public List<ItemStack> rollOutputs(RandomSource random) {
            List<ItemStack> results = new ArrayList<>();
            for (SalvageOutput entry : possibleOutputs) {
                if (random.nextFloat() < entry.chance()) {
                    results.add(entry.stack().copy());
                } else {
                    results.add(ModItems.SALVAGED_SCRAP.asStack());
                }
            }
            return consolidate(results);
        }

        public List<ItemStack> getPreviewOutputs() {
            List<ItemStack> preview = new ArrayList<>();
            for (SalvageOutput entry : possibleOutputs) {
                preview.add(entry.stack().copy());
            }
            return consolidate(preview);
        }
    }

    /**
     * Finds a matching reclamation recipe.
     * 1. Checks explicit 'create_reclamation:reclaiming' datapack recipes first.
     * 2. Falls back to dynamic crafting recipe deconstruction.
     */
    public static Optional<ReclaimedResult> findReclamation(Level level, ItemStack input) {
        if (input.isEmpty() || level == null) {
            return Optional.empty();
        }

        // Anti-exploit: Reject damaged tools/armor to prevent free repair exploits
        if (input.isDamageableItem() && input.isDamaged()) {
            return Optional.empty();
        }

        // 1. Check explicit custom Reclaiming recipes
        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        List<RecipeHolder<ReclaimingRecipe>> customRecipes =
                level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.RECLAIMING.get());

        for (RecipeHolder<ReclaimingRecipe> holder : customRecipes) {
            ReclaimingRecipe custom = holder.value();
            if (custom.getIngredient().test(input)) {
                List<SalvageOutput> outputs = new ArrayList<>();
                for (ReclaimingRecipe.ChanceOutput out : custom.getResults()) {
                    outputs.add(new SalvageOutput(out.stack(), out.chance()));
                }
                return Optional.of(new ReclaimedResult(custom.getInputCount(), outputs, custom.getProcessingTime()));
            }
        }

        // 2. Dynamic Crafting Recipe Fallback
        boolean fallbackEnabled = true;
        float salvageRate = 0.85f;
        try {
            if (ReclamationConfig.SERVER_SPEC.isLoaded()) {
                fallbackEnabled = ReclamationConfig.SERVER.enableCraftingFallback.get();
                salvageRate = ReclamationConfig.SERVER.defaultSalvageRate.get().floatValue();
            }
        } catch (Exception ignored) {}

        if (!fallbackEnabled) {
            return Optional.empty();
        }

        List<RecipeHolder<CraftingRecipe>> recipes = level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING);

        for (RecipeHolder<CraftingRecipe> holder : recipes) {
            CraftingRecipe recipe = holder.value();
            ItemStack result = recipe.getResultItem(level.registryAccess());

            if (!result.isEmpty() && ItemStack.isSameItemSameComponents(result, input)) {
                int resultCount = Math.max(1, result.getCount());

                NonNullList<Ingredient> ingredients = recipe.getIngredients();
                List<SalvageOutput> outputs = new ArrayList<>();
                int nonEmptyIngredientCount = 0;

                for (Ingredient ingredient : ingredients) {
                    if (ingredient.isEmpty()) {
                        continue;
                    }
                    nonEmptyIngredientCount++;
                    ItemStack[] matchingStacks = ingredient.getItems();
                    if (matchingStacks.length > 0 && !matchingStacks[0].isEmpty()) {
                        ItemStack outStack = matchingStacks[0].copy();
                        outStack.setCount(1);
                        outputs.add(new SalvageOutput(outStack, salvageRate));
                    }
                }

                // Anti-exploit: Reject single-ingredient uncrafting loops (e.g. 1 Ingot -> 9 Nuggets or 1 Log -> 4 Planks)
                if (nonEmptyIngredientCount <= 1) {
                    continue;
                }

                if (!outputs.isEmpty()) {
                    int baseProcessingTime = Math.max(40, outputs.size() * 20);
                    return Optional.of(new ReclaimedResult(resultCount, outputs, baseProcessingTime));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Consolidates output items of the same type into single stacks up to maxStackSize.
     */
    public static List<ItemStack> consolidate(List<ItemStack> items) {
        List<ItemStack> consolidated = new ArrayList<>();
        for (ItemStack item : items) {
            boolean merged = false;
            for (ItemStack existing : consolidated) {
                if (ItemStack.isSameItemSameComponents(existing, item)) {
                    int space = existing.getMaxStackSize() - existing.getCount();
                    if (space >= item.getCount()) {
                        existing.grow(item.getCount());
                        merged = true;
                        break;
                    }
                }
            }
            if (!merged) {
                consolidated.add(item.copy());
            }
        }
        return consolidated;
    }
}
