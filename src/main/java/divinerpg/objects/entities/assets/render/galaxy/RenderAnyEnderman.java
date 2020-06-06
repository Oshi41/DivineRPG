package divinerpg.objects.entities.assets.render.galaxy;

import net.minecraft.client.model.ModelEnderman;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class RenderAnyEnderman extends RenderLiving<EntityLiving> {
    private final ResourceLocation texture;
    private final Random rnd = new Random();

    public RenderAnyEnderman(RenderManager renderManagerIn) {
        this(renderManagerIn, new ResourceLocation("textures/entity/enderman/enderman.png"));
    }

    public RenderAnyEnderman(RenderManager renderManagerIn, ResourceLocation texture) {
        super(renderManagerIn, new ModelEnderman(0.0F), 0.5F);
        this.texture = texture;
        this.addLayer(new LayerEndermanEyes(this));
        this.addLayer(new LayerEndermanHeldBlock(this));
    }

    public ModelEnderman getMainModel() {
        return (ModelEnderman) super.getMainModel();
    }

    /**
     * Renders the desired {@code T} type Entity.
     */
    public void doRender(EntityLiving entity, double x, double y, double z, float entityYaw, float partialTicks) {
        ModelEnderman modelenderman = this.getMainModel();
        modelenderman.isCarrying = !entity.getHeldItemMainhand().isEmpty();
        modelenderman.isAttacking = entity.getAttackTarget() != null;

        if (modelenderman.isAttacking) {
            double d0 = 0.02D;
            x += this.rnd.nextGaussian() * 0.02D;
            z += this.rnd.nextGaussian() * 0.02D;
        }

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityLiving entity) {
        return texture;
    }
}
