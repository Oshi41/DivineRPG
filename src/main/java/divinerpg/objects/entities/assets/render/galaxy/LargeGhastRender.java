package divinerpg.objects.entities.assets.render.galaxy;

import divinerpg.objects.entities.entity.galaxy.EntityLargeGhast;
import net.minecraft.client.model.ModelGhast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class LargeGhastRender extends RenderLiving<EntityLargeGhast> {
    private static final ResourceLocation GHAST_TEXTURES = new ResourceLocation("textures/entity/ghast/ghast.png");
    private static final ResourceLocation GHAST_SHOOTING_TEXTURES = new ResourceLocation("textures/entity/ghast/ghast_shooting.png");

    public LargeGhastRender(RenderManager rendermanagerIn) {
        super(rendermanagerIn, new ModelGhast(), 0.5F);
    }

    /**
     * Allows the render to do state modifications necessary before the model is rendered.
     */
    @Override
    protected void preRenderCallback(EntityLargeGhast entitylivingbaseIn, float partialTickTime) {
        float color = 1.0F;
        float scale = 7.5F;

        GlStateManager.scale(scale, scale, scale);
        GlStateManager.color(color, color, color, color);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityLargeGhast entity) {
        return entity.getAttackingEntity() != null
                ? GHAST_SHOOTING_TEXTURES
                : GHAST_TEXTURES;
    }
}
