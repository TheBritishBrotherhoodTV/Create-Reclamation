package com.reclamation.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.reclamation.registry.ModItems;
import com.reclamation.registry.ModRecipeTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ReclaimingRecipe implements Recipe<SingleRecipeInput> {

    public record ChanceOutput(ItemStack stack, float chance) {
        public static final Codec<ChanceOutput> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ItemStack.CODEC.fieldOf("item").forGetter(ChanceOutput::stack),
                        Codec.FLOAT.optionalFieldOf("chance", 1.0f).forGetter(ChanceOutput::chance)
                ).apply(instance, ChanceOutput::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, ChanceOutput> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, ChanceOutput::stack,
                ByteBufCodecs.FLOAT, ChanceOutput::chance,
                ChanceOutput::new
        );
    }

    private final Ingredient ingredient;
    private final int inputCount;
    private final List<ChanceOutput> results;
    private final int processingTime;
    private final int minSpeed;

    public ReclaimingRecipe(Ingredient ingredient, int inputCount, List<ChanceOutput> results, int processingTime, int minSpeed) {
        this.ingredient = ingredient;
        this.inputCount = Math.max(1, inputCount);
        this.results = results;
        this.processingTime = Math.max(20, processingTime);
        this.minSpeed = minSpeed;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public int getInputCount() {
        return inputCount;
    }

    public List<ChanceOutput> getResults() {
        return results;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public int getMinSpeed() {
        return minSpeed;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        ItemStack stack = input.item();
        return this.ingredient.test(stack) && stack.getCount() >= inputCount;
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return getResultItem(registries);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return results.isEmpty() ? ItemStack.EMPTY : results.get(0).stack().copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(ingredient);
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.RECLAIMING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.RECLAIMING.get();
    }

    public List<ItemStack> rollOutputs(RandomSource random) {
        List<ItemStack> rolled = new ArrayList<>();
        for (ChanceOutput out : results) {
            if (random.nextFloat() < out.chance()) {
                rolled.add(out.stack().copy());
            } else {
                rolled.add(ModItems.SALVAGED_SCRAP.asStack());
            }
        }
        return ReclamationRecipeHelper.consolidate(rolled);
    }

    public static class Serializer implements RecipeSerializer<ReclaimingRecipe> {
        public static final MapCodec<ReclaimingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(ReclaimingRecipe::getIngredient),
                Codec.INT.optionalFieldOf("input_count", 1).forGetter(ReclaimingRecipe::getInputCount),
                ChanceOutput.CODEC.listOf().fieldOf("results").forGetter(ReclaimingRecipe::getResults),
                Codec.INT.optionalFieldOf("processing_time", 100).forGetter(ReclaimingRecipe::getProcessingTime),
                Codec.INT.optionalFieldOf("min_speed", 16).forGetter(ReclaimingRecipe::getMinSpeed)
        ).apply(inst, ReclaimingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ReclaimingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, ReclaimingRecipe::getIngredient,
                        ByteBufCodecs.INT, ReclaimingRecipe::getInputCount,
                        ChanceOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), ReclaimingRecipe::getResults,
                        ByteBufCodecs.INT, ReclaimingRecipe::getProcessingTime,
                        ByteBufCodecs.INT, ReclaimingRecipe::getMinSpeed,
                        ReclaimingRecipe::new
                );

        @Override
        public MapCodec<ReclaimingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ReclaimingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
