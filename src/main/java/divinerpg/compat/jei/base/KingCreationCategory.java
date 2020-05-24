package divinerpg.compat.jei.base;

import divinerpg.DivineRPG;
import divinerpg.api.DivineAPI;
import divinerpg.compat.jei.JeiReferences;
import divinerpg.objects.blocks.tile.container.gui.KIngCompressionGUI;
import divinerpg.registry.ArmorRegistry;
import divinerpg.utils.LocalizeUtils;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class KingCreationCategory implements IRecipeCategory {
    protected final IDrawableAnimated animatedFlame;
    protected final IDrawableAnimated arrow;
    private final ITickTimer timer;
    private IDrawable background;
    private IDrawable icon;
    private String name;

    public KingCreationCategory(IGuiHelper helper) {
        ResourceLocation texture = KIngCompressionGUI.Texture;

        this.background = helper.createDrawable(texture, 0, 0, 176, 82);
        this.icon = helper.createDrawableIngredient(new ItemStack(ArmorRegistry.king_helmet));
        name = LocalizeUtils.i18n("tile.king_compressor.name");

        IDrawableStatic staticFlame = helper.createDrawable(texture, 176, 0, 14, 14);
        animatedFlame = helper.createAnimatedDrawable(staticFlame, 500, IDrawableAnimated.StartDirection.TOP, true);

        arrow = helper.drawableBuilder(texture, 176, 14, 24, 17)
                .buildAnimated(500, IDrawableAnimated.StartDirection.LEFT, false);

        timer = helper.createTickTimer(20 * 3, DivineAPI.getArmorDescriptionRegistry().getEntries().size(), false);
    }

    @Override
    public String getUid() {
        return JeiReferences.KING_ARMOR_CREATION;
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getModName() {
        return DivineRPG.NAME;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        animatedFlame.draw(minecraft, 39, 36);
        arrow.draw(minecraft, 112, 48);

        FontRenderer fontRenderer = minecraft.getRenderManager().getFontRenderer();

        fontRenderer.drawString(getTitle(), 90, 7, 16777215);
        fontRenderer.drawString(String.format("%s/%s", timer.getValue(), timer.getMaxValue()), 90, 7 + fontRenderer.FONT_HEIGHT, 16777215);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (90 <= mouseX && mouseX <= 90 + 30) {
            if (7 <= mouseY && mouseY <= 7 + 30) {
                return Collections.singletonList("Amount of sets need to absorb to create king armor");
            }
        }

        return Collections.emptyList();
    }

    @Override
    public void setRecipe(IRecipeLayout iRecipeLayout, IRecipeWrapper iRecipeWrapper, IIngredients iIngredients) {
        IGuiItemStackGroup stacks = iRecipeLayout.getItemStacks();

        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 2; i++) {
                int x = 8 + i * 60 - 1;
                int y = 8 + j * 22 - 1;

                stacks.init(j * 2 + i, true, x, y);
            }
        }

        stacks.init(6, true, 37, 51);
        stacks.init(7, false, 143 + 5, 43 + 5);

        stacks.set(iIngredients);

        iRecipeLayout.getItemStacks().addTooltipCallback(this::tooltipCallback);
    }

    private void tooltipCallback(int slotIndex, boolean input, ItemStack stack, List<String> result) {
        // armor info
        if (0 <= slotIndex && slotIndex < 6) {
            result.add("Any armor with super powers");
        }

        // fuel
        if (slotIndex == 6) {
            // result.add(String.format("Burn for %s ticks", TileEntityKingCompressor.fuelMap.get(stack.getItem())));
        }
    }
}
