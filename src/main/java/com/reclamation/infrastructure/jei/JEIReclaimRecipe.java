package com.reclamation.infrastructure.jei;

import com.reclamation.content.recipe.ReclamationRecipeHelper.SalvageOutput;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record JEIReclaimRecipe(ItemStack input, List<SalvageOutput> outputs, int processingTime) {}
