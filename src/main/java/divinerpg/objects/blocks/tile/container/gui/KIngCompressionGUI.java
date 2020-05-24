package divinerpg.objects.blocks.tile.container.gui;

import divinerpg.DivineRPG;
import divinerpg.objects.blocks.tile.container.KingCompressorContainer;
import divinerpg.objects.blocks.tile.entity.TileEntityKingCompressor;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Set;

public class KIngCompressionGUI extends GuiContainer {
    public static final ResourceLocation Texture = new ResourceLocation(Reference.MODID, "textures/gui/king_compression_gui.png");
    private final TileEntityKingCompressor tile;

    public KIngCompressionGUI(KingCompressorContainer inventorySlotsIn) {
        super(inventorySlotsIn);

        tile = inventorySlotsIn.getTile();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1, 1, 1, 1);
        this.mc.getTextureManager().bindTexture(Texture);
        drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        int k = (int) Math.floor(tile.getBurningTicks() * 13.0 / tile.getCookTimeLength());
        this.drawTexturedModalRect(this.guiLeft + 39, this.guiTop + 36 + 14 - k, 176, 14 - k, 14, k + 1);

        k = (int) Math.floor(tile.getCurrentCookTime() * 24.0 / tile.getCookTimeLength());
        this.drawTexturedModalRect(this.guiLeft + 112, this.guiTop + 48, 176, 14, k + 1, 16);

        Set<String> sets = tile.getAbsorbedSets();
        String text = "King compressor";

        int stringWidth = this.fontRenderer.getStringWidth(text);

        int topX = 90 + this.guiLeft;
        int topY = 7 + this.guiTop;
        this.fontRenderer.drawString(text, topX, topY, 16777215);

        String numbers = String.format("%s / %s", sets.size(), tile.getLimit());
        this.fontRenderer.drawString(numbers, topX, topY + fontRenderer.FONT_HEIGHT, 16777215);

        drawToolTip(sets, mouseX, mouseY, topX, topY, stringWidth, fontRenderer.FONT_HEIGHT);
    }

    private void drawToolTip(Set<String> sets, int mouseX, int mouseY, int topX, int topY, int strWidth, int fontHeight) {
        if (topX <= mouseX && mouseX <= topX + strWidth) {
            if (topY <= mouseY && mouseY <= topY + fontHeight) {
                drawHoveringText(new ArrayList<>(sets), mouseX, mouseY);
            }
        }
    }
}
