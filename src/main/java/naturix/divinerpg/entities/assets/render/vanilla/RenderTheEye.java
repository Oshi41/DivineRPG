package naturix.divinerpg.entities.assets.render.vanilla;

import javax.annotation.Nullable;

import naturix.divinerpg.entities.assets.model.vanilla.model.ModelEye;
import naturix.divinerpg.entities.entity.vanilla.TheEye;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderTheEye extends RenderLiving<TheEye> {
	
	public static final IRenderFactory FACTORY = new Factory();
	ResourceLocation texture = new ResourceLocation("divinerpg:textures/entity/the_eye.png");
    
	public RenderTheEye(RenderManager rendermanagerIn, ModelBase modelbaseIn, float shadowsizeIn) {
        super(rendermanagerIn, new ModelEye(), 1F);
    }

	@Nullable
    @Override
    protected ResourceLocation getEntityTexture(TheEye entity) {
        return texture;
    }

	 public static class Factory implements IRenderFactory<TheEye> {

	        @Override
	        public Render<? super TheEye> createRenderFor(RenderManager manager) {
	            return new RenderTheEye(manager, new ModelEye(), 0.5F);
	        }
	    }

	}