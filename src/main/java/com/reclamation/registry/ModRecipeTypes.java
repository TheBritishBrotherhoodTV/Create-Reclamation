package com.reclamation.registry;

import com.reclamation.CreateReclamation;
import com.reclamation.content.recipe.ReclaimingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, CreateReclamation.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, CreateReclamation.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<ReclaimingRecipe>> RECLAIMING =
            RECIPE_TYPES.register("reclaiming", () -> RecipeType.simple(CreateReclamation.asResource("reclaiming")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ReclaimingRecipe>> RECLAIMING_SERIALIZER =
            RECIPE_SERIALIZERS.register("reclaiming", ReclaimingRecipe.Serializer::new);

    public static void register(IEventBus modEventBus) {
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}
