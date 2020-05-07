package divinerpg.objects.entities.assets.render.vanilla;

import divinerpg.objects.entities.assets.model.vanilla.AncientKingEntityModel;
import divinerpg.objects.entities.entity.vanilla.AncientKingEntity;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;

public class RenderAncientKing extends RenderLiving<AncientKingEntity> {
    private final ResourceLocation texture = new ResourceLocation("textures/entity/enderdragon/dragon.png");
    private final ResourceLocation explodeTexture = new ResourceLocation("textures/entity/enderdragon/dragon_exploding.png");

    public RenderAncientKing(RenderManager rendermanagerIn) {
        super(rendermanagerIn, new AncientKingEntityModel(), 0.5F);
    }

    @Override
    protected void applyRotations(AncientKingEntity entityLiving, float p_77043_2_, float rotationYaw, float partialTicks) {
//        float f = (float) entityLiving.getMovementOffsets(7, partialTicks)[0];
//        float f1 = (float) (entityLiving.getMovementOffsets(5, partialTicks)[1] - entityLiving.getMovementOffsets(10, partialTicks)[1]);
//        GlStateManager.rotate(-f, 0.0F, 1.0F, 0.0F);
//        GlStateManager.rotate(f1 * 10.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(0.0F, 0.0F, 1.0F);

        if (entityLiving.deathTime > 0) {
            float f2 = ((float) entityLiving.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            f2 = MathHelper.sqrt(f2);

            if (f2 > 1.0F) {
                f2 = 1.0F;
            }

            GlStateManager.rotate(f2 * this.getDeathMaxRotation(entityLiving), 0.0F, 0.0F, 1.0F);
        }
    }

    /**
     * Renders the model in RenderLiving
     */
    protected void renderModel(AncientKingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        if (entitylivingbaseIn.isDead) {
            float f = 200.0F;
            GlStateManager.depthFunc(515);
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(516, f);
            this.bindTexture(explodeTexture);
            this.mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.depthFunc(514);
        }

        this.bindEntityTexture(entitylivingbaseIn);

        this.mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);

        if (entitylivingbaseIn.hurtTime > 0) {
            GlStateManager.depthFunc(514);
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.color(1.0F, 0.0F, 0.0F, 0.5F);
            this.mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.depthFunc(515);
        }
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(AncientKingEntity entity) {
        return texture;
    }
}
