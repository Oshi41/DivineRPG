package divinerpg.objects.entities.assets.render.projectiles;

import divinerpg.DivineRPG;
import divinerpg.objects.entities.assets.render.RenderDivineProjectile;
import divinerpg.objects.entities.entity.projectiles.king.EntityKingRage;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderKingRage extends RenderDivineProjectile<EntityKingRage> {
    private final ResourceLocation texture;

    public RenderKingRage(RenderManager manager) {
        super(manager, 1);
        texture = new ResourceLocation(DivineRPG.MODID, "textures/projectiles/fractite_cannon.png");
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityKingRage entity) {
        return texture;
    }
}
