package divinerpg.objects.entities.assets.render.galaxy;

import divinerpg.objects.entities.entity.galaxy.EntityAngryEnderman;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelEnderman;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import java.util.Random;

public class AngryEndermanRender extends RenderLiving<EntityAngryEnderman> {
    private static final ResourceLocation ENDERMAN_TEXTURES = new ResourceLocation("textures/entity/enderman/enderman.png");
    private final Random rnd = new Random();

    public AngryEndermanRender(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelEnderman(0.0F), 0.5F);
    }

    public ModelEnderman getMainModel() {
        return (ModelEnderman) super.getMainModel();
    }

    /**
     * Renders the desired {@code T} type Entity.
     */
    public void doRender(EntityAngryEnderman entity, double x, double y, double z, float entityYaw, float partialTicks) {
        IBlockState iblockstate = entity.getHeldBlockState();
        ModelEnderman modelenderman = this.getMainModel();
        modelenderman.isCarrying = iblockstate != null;
        modelenderman.isAttacking = entity.isScreaming();

        if (entity.isScreaming()) {
            double d0 = 0.02D;
            x += this.rnd.nextGaussian() * 0.02D;
            z += this.rnd.nextGaussian() * 0.02D;
        }

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityAngryEnderman entity) {
        return ENDERMAN_TEXTURES;
    }
}
