package com.reclamation.content.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public enum MaterialCategory {
    EASY("Easy", 1.00f),
    STANDARD("Standard", 0.90f),
    DIFFICULT("Difficult", 0.75f);

    private final String displayName;
    private final float baseRecoverability;

    private static final Map<ResourceLocation, MaterialCategory> EXPLICIT_OVERRIDES = new HashMap<>();

    MaterialCategory(String displayName, float baseRecoverability) {
        this.displayName = displayName;
        this.baseRecoverability = baseRecoverability;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getBaseRecoverability() {
        return baseRecoverability;
    }

    public static void registerOverride(ResourceLocation itemId, MaterialCategory category) {
        EXPLICIT_OVERRIDES.put(itemId, category);
    }

    /**
     * Determines material classification using tag heuristics, item registry names, and explicit overrides.
     */
    public static MaterialCategory categorize(ItemStack stack) {
        if (stack.isEmpty()) {
            return STANDARD;
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (EXPLICIT_OVERRIDES.containsKey(id)) {
            return EXPLICIT_OVERRIDES.get(id);
        }

        String path = id.getPath().toLowerCase();

        // 1. Difficult components (delicate, electronic, precision, multi-stage)
        if (path.contains("precision_mechanism")
                || path.contains("electron_tube")
                || path.contains("clockwork")
                || path.contains("netherite")
                || path.contains("diamond")
                || path.contains("arm")
                || path.contains("controller")
                || path.contains("speed_controller")
                || path.contains("sequenced_gearshift")
                || path.contains("display_link")
                || path.contains("content_observer")) {
            return DIFFICULT;
        }

        // 2. Easy components (basic raw materials, metals, simple structural parts)
        if (path.contains("nugget")
                || path.contains("ingot")
                || path.contains("sheet")
                || path.contains("plate")
                || path.contains("shaft")
                || path.contains("plank")
                || path.contains("slab")
                || path.contains("casing")
                || path.contains("stone")
                || path.contains("cobblestone")
                || path.contains("iron")
                || path.contains("copper")
                || path.contains("gold")
                || path.contains("andesite_casing")
                || path.contains("brass_casing")
                || path.contains("copper_casing")) {
            return EASY;
        }

        // 3. Standard components (Andesite alloy, gearboxes, funnels, chutes, normal manufactured parts)
        return STANDARD;
    }
}
