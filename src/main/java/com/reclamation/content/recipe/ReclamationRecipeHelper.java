package com.reclamation.content.recipe;

import com.reclamation.content.reclaimer.ReclaimerTier;
import com.reclamation.infrastructure.config.ReclamationConfig;
import com.reclamation.registry.ModItems;
import com.reclamation.registry.ModRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ReclamationRecipeHelper {

    public record SalvageOutput(ItemStack stack, float baseRecoverability, MaterialCategory category) {
        public SalvageOutput(ItemStack stack, float baseRecoverability) {
            this(stack, baseRecoverability, MaterialCategory.categorize(stack));
        }

        public float calculateEffectiveRate(ReclaimerTier tier) {
            float eff = tier.getEfficiency() * baseRecoverability * category.getBaseRecoverability();
            return Math.min(0.98f, Math.max(0.01f, eff));
        }
    }

    public record ReclaimedResult(int consumedInputCount, List<SalvageOutput> possibleOutputs, int processingTime, boolean isExplicit) {
        
        /**
         * Resolves actual items recovered using deterministic fractional accumulation.
         * Guarantees long-term statistical convergence to the tier efficiency without punishing streaks.
         */
        public List<ItemStack> rollOutputs(ReclaimerTier tier, Map<String, Float> accumulators, RandomSource random) {
            List<ItemStack> results = new ArrayList<>();
            float scrapMultiplier = 1.0f;
            try {
                if (ReclamationConfig.SERVER_SPEC.isLoaded()) {
                    scrapMultiplier = ReclamationConfig.SERVER.scrapConversionRate.get().floatValue();
                }
            } catch (Exception ignored) {}

            for (SalvageOutput entry : possibleOutputs) {
                float effectiveRate = entry.calculateEffectiveRate(tier);
                ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(entry.stack().getItem());
                String keyStr = itemKey.toString();

                float acc = accumulators.getOrDefault(keyStr, 0.0f) + effectiveRate;
                boolean recoveredItem = false;

                // 1. Guaranteed discrete integer unit earned (with float epsilon allowance)
                if (acc >= 0.999f) {
                    ItemStack recovered = entry.stack().copy();
                    recovered.setCount(1);
                    results.add(recovered);
                    acc = Math.max(0.0f, acc - 1.0f);
                    recoveredItem = true;
                }


                // 2. If no item recovered on this operation, chance to yield Salvaged Scrap from lost fraction
                if (!recoveredItem) {
                    if (random.nextFloat() < (1.0f - effectiveRate) * scrapMultiplier) {
                        results.add(ModItems.SALVAGED_SCRAP.asStack());
                    }
                }

                accumulators.put(keyStr, Math.max(0.0f, acc));
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

    private static final Map<Item, Optional<ReclaimedResult>> RECIPE_CACHE = new ConcurrentHashMap<>();

    public static void clearCache() {
        RECIPE_CACHE.clear();
    }

    /**
     * Finds a matching reclamation recipe.
     * 1. Checks explicit 'create_reclamation:reclaiming' datapack recipes first.
     * 2. Falls back to conservative crafting recipe deconstruction with ambiguity and exploit checks.
     */
    public static Optional<ReclaimedResult> findReclamation(Level level, ItemStack input) {
        if (input.isEmpty() || level == null) {
            return Optional.empty();
        }

        // Anti-exploit: Reject damaged tools/armor to prevent free repair exploits
        if (input.isDamageableItem() && input.isDamaged()) {
            return Optional.empty();
        }

        Item item = input.getItem();
        if (RECIPE_CACHE.containsKey(item)) {
            return RECIPE_CACHE.get(item);
        }

        Optional<ReclaimedResult> resolved = resolveReclamation(level, input);
        RECIPE_CACHE.put(item, resolved);
        return resolved;
    }

    private static Optional<ReclaimedResult> resolveReclamation(Level level, ItemStack input) {
        // 1. Explicit custom Reclaiming recipes (Authoritative)
        List<RecipeHolder<ReclaimingRecipe>> customRecipes =
                level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.RECLAIMING.get());

        for (RecipeHolder<ReclaimingRecipe> holder : customRecipes) {
            ReclaimingRecipe custom = holder.value();
            if (custom.getIngredient().test(input)) {
                List<SalvageOutput> outputs = new ArrayList<>();
                for (ReclaimingRecipe.ChanceOutput out : custom.getResults()) {
                    outputs.add(new SalvageOutput(out.stack(), out.chance()));
                }
                return Optional.of(new ReclaimedResult(custom.getInputCount(), outputs, custom.getProcessingTime(), true));
            }
        }

        // 2. Dynamic Crafting Recipe Fallback (Conservative & Safe)
        boolean fallbackEnabled = true;
        try {
            if (ReclamationConfig.SERVER_SPEC.isLoaded()) {
                fallbackEnabled = ReclamationConfig.SERVER.enableCraftingFallback.get();
            }
        } catch (Exception ignored) {}

        if (!fallbackEnabled) {
            return Optional.empty();
        }

        List<RecipeHolder<CraftingRecipe>> allCrafting = level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING);
        List<RecipeHolder<CraftingRecipe>> matchingRecipes = new ArrayList<>();

        for (RecipeHolder<CraftingRecipe> holder : allCrafting) {
            CraftingRecipe recipe = holder.value();
            ItemStack result = recipe.getResultItem(level.registryAccess());
            if (!result.isEmpty() && ItemStack.isSameItemSameComponents(result, input)) {
                matchingRecipes.add(holder);
            }
        }

        if (matchingRecipes.isEmpty()) {
            return Optional.empty();
        }

        // Multiple Recipe Ambiguity Protection:
        // If multiple crafting recipes produce this item, verify if they use the same ingredient types.
        // If distinct recipe recipes exist (e.g. Chest from Oak vs Birch vs Acacia), refuse automatic deconstruction!
        Set<Set<Item>> distinctIngredientSets = new HashSet<>();
        for (RecipeHolder<CraftingRecipe> match : matchingRecipes) {
            Set<Item> ingredients = new HashSet<>();
            for (Ingredient ing : match.value().getIngredients()) {
                if (!ing.isEmpty()) {
                    for (ItemStack s : ing.getItems()) {
                        if (!s.isEmpty()) {
                            ingredients.add(s.getItem());
                        }
                    }
                }
            }
            if (!ingredients.isEmpty()) {
                distinctIngredientSets.add(ingredients);
            }
        }

        if (distinctIngredientSets.size() > 1) {
            // Ambiguous crafting origins: require explicit JSON recipe
            return Optional.empty();
        }

        // Safe unambiguous single recipe
        CraftingRecipe selected = matchingRecipes.get(0).value();
        ItemStack resultStack = selected.getResultItem(level.registryAccess());
        int resultCount = Math.max(1, resultStack.getCount());

        NonNullList<Ingredient> ingredients = selected.getIngredients();
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

                // Anti-duplication / Circular Recursion Protection:
                // An output ingredient cannot be identical to the input item
                if (outStack.getItem() == input.getItem()) {
                    return Optional.empty();
                }

                outputs.add(new SalvageOutput(outStack, 1.0f));
            }
        }

        // Anti-exploit: Reject single-ingredient decomposition (e.g. 1 Ingot -> 9 Nuggets or 1 Log -> 4 Planks)
        if (nonEmptyIngredientCount <= 1) {
            return Optional.empty();
        }

        if (!outputs.isEmpty()) {
            int baseProcessingTime = Math.max(40, outputs.size() * 20);
            return Optional.of(new ReclaimedResult(resultCount, outputs, baseProcessingTime, false));
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
