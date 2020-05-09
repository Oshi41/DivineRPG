package divinerpg.objects.blocks.tile.render;

import divinerpg.objects.blocks.tile.entity.TileEntityKingCompressor;
import divinerpg.objects.blocks.tile.entity.multiblock.IMultiblockTile;
import divinerpg.objects.blocks.tile.model.KingCompressorModel;
import divinerpg.utils.multiblock.StructureMatch;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class RenderKingCompressor extends TileEntitySpecialRenderer<TileEntityKingCompressor> {
    private final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/enderdragon/dragon.png");

    @Override
    public void render(TileEntityKingCompressor te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        this.bindTexture(TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();

        EnumFacing facing = getFacing(te).rotateYCCW().getOpposite();
        float rotationIn = 0;

        switch (facing) {
            case NORTH:
                GlStateManager.translate(x + 0.5F, y + 0.25F, z + 0.74F);
                break;
            case SOUTH:
                GlStateManager.translate(x + 0.5F, y + 0.25F, z + 0.26F);
                rotationIn = 180.0F;
                break;
            case WEST:
                GlStateManager.translate(x + 0.74F, y + 0.25F, z + 0.5F);
                rotationIn = 270.0F;
                break;
            case EAST:
            default:
                GlStateManager.translate(x + 0.26F, y + 0.25F, z + 0.5F);
                rotationIn = 90.0F;
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        GlStateManager.enableAlpha();


        float scale = 5.8F;
        GlStateManager.scale(scale, scale, scale);

        getModel().render(null, 0, 0.0F, 0.0F, rotationIn, 0.0F, 0.0625F);
        GlStateManager.popMatrix();

        if (destroyStage >= 0)
        {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
    }

    private ModelBase getModel() {
        return new KingCompressorModel();
    }

    private EnumFacing getFacing(IMultiblockTile tile) {
        if (tile != null) {
            StructureMatch match = tile.getMatch();
            if (match != null) {
                if (match.forwards.getAxis() != EnumFacing.Axis.Y)
                    return match.forwards;

                if (match.up.getAxis() != EnumFacing.Axis.Y)
                    return match.up;
            }
        }


        return EnumFacing.NORTH;
    }

}
