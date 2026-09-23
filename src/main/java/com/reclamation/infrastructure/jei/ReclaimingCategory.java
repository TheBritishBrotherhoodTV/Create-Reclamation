package com.reclamation.infrastructure.jei;

import com.reclamation.CreateReclamation;
import com.reclamation.registry.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ReclaimingCategory implements IRecipeCategory<JEIReclaimRecipe> {

    public static final RecipeType<JEIReclaimRecipe> TYPE =
            RecipeType.create(CreateReclamation.MOD_ID, "reclaiming", JEIReclaimRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated animatedArrow;

    public ReclaimingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(170, 75);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, ModBlocks.MECHANICAL_RECLAIMER.asStack());
        this.animatedArrow = guiHelper.createAnimatedRecipeArrow(40);
    }

    @Override
    public RecipeType<JEIReclaimRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.create_reclamation.mechanical_reclaimer");
    }

    @Override
    public int getWidth() {
        return 170;
    }

    @Override
    public int getHeight() {
        return 75;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, JEIReclaimRecipe recipe, IFocusGroup focuses) {
        // Input slot on the left
        builder.addSlot(RecipeIngredientRole.INPUT, 16, 28)
                .addItemStack(recipe.input());

        // 3x3 Output slots on the right
        int startX = 86;
        int startY = 10;
        int slotSize = 19;

        for (int i = 0; i < Math.min(9, recipe.outputs().size()); i++) {
            var output = recipe.outputs().get(i);
            int row = i / 3;
            int col = i % 3;
            int x = startX + col * slotSize;
            int y = startY + row * slotSize;

            final float chance = output.chance();
            var slot = builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .addItemStack(output.stack());

            if (chance < 1.0f) {
                int percent = Math.round(chance * 100);
                slot.addTooltipCallback((slotsView, tooltip) -> {
                    tooltip.add(Component.literal(percent + "% Salvage Chance")
                            .withStyle(ChatFormatting.GOLD));
                    tooltip.add(Component.literal((100 - percent) + "% Scrap Fallback")
                            .withStyle(ChatFormatting.DARK_GRAY));
                });
            }
        }
    }

    @Override
    public void draw(JEIReclaimRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Draw animated progress arrow between input and outputs
        animatedArrow.draw(guiGraphics, 50, 28);

        // Display processing time in seconds
        float seconds = recipe.processingTime() / 20.0f;
        String timeStr = String.format("%.1fs", seconds);
        guiGraphics.drawString(
                net.minecraft.client.Minecraft.getInstance().font,
                timeStr,
                48,
                48,
                0x777777,
                false
        );
    }
}
