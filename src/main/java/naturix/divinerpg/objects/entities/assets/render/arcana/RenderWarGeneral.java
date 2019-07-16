package naturix.divinerpg.objects.entities.assets.render.arcana;

import javax.annotation.Nullable;

import naturix.divinerpg.objects.entities.assets.model.twilight.ModelSamek;
import naturix.divinerpg.objects.entities.entity.arcana.WarGeneral;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderWarGeneral extends RenderLiving<WarGeneral> {
	
	public static final IRenderFactory FACTORY = new Factory();
	ResourceLocation texture = new ResourceLocation("divinerpg:textures/entity/war_general.png");
	private final ModelSamek ModelSamek;
    
	public RenderWarGeneral(RenderManager rendermanagerIn, ModelBase modelbaseIn, float shadowsizeIn) {
        super(rendermanagerIn, new ModelSamek(), 1F);
        ModelSamek = (ModelSamek) super.mainModel;

    }


	@Nullable
    @Override
    protected ResourceLocation getEntityTexture(WarGeneral entity) {
        return texture;
    }

	 public static class Factory implements IRenderFactory<WarGeneral> {

	        @Override
	        public Render<? super WarGeneral> createRenderFor(RenderManager manager) {
	            return new RenderWarGeneral(manager, new ModelSamek(), 0.5F);
	        }
	    }

	}