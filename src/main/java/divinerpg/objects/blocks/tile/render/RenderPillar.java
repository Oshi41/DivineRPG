package divinerpg.objects.blocks.tile.render;

import divinerpg.objects.blocks.tile.entity.pillar.TileEntityPillar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPillar extends TileEntitySpecialRenderer<TileEntityPillar> {

    public void render(TileEntityPillar te, double x, double y, double z, float partialTicks,
                       int destroyStage, float alpha) {

        ItemStack stack = te.getInventory().getStackInSlot(0);

        if (!stack.isEmpty()) {
            float time = (float) te.getWorld().getTotalWorldTime() + partialTicks;

            BlockPos pos = te.getPos();

            AxisAlignedBB box = getWorld().getBlockState(pos).getBoundingBox(getWorld(), pos);


            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5D, y + box.maxY, z + 0.5D);
            GlStateManager.translate(0.0F, MathHelper.sin(time / 10.0F) * 0.1F + 0.1F, 0.0F);
            GlStateManager.scale(0.75D, 0.75D, 0.75D);
            float angle = time / 20.0F * 57.295776F;
            GlStateManager.rotate(angle, 0.0F, 1.0F, 0.0F);
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        }
    }
}