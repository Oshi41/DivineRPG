package divinerpg.objects.entities.assets.model.vanilla;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AncientKingEntityModel extends ModelBase {
    /**
     * The head Model renderer of the dragon
     */
    private final List<ModelRenderer> heads = new ArrayList<>();

    /**
     * List of side spines model
     */
    private final Map<Integer, ModelRenderer> spines = new HashMap<>();

    /**
     * The jaw Model renderer of the dragon
     */
    private final ModelRenderer jaw;
    /**
     * The body Model renderer of the dragon
     */
    private final ModelRenderer body;
    /**
     * The rear leg Model renderer of the dragon
     */
    private final ModelRenderer rearLeg;
    /**
     * The front leg Model renderer of the dragon
     */
    private final ModelRenderer frontLeg;
    /**
     * The rear leg tip Model renderer of the dragon
     */
    private final ModelRenderer rearLegTip;
    /**
     * The front leg tip Model renderer of the dragon
     */
    private final ModelRenderer frontLegTip;
    /**
     * The rear foot Model renderer of the dragon
     */
    private final ModelRenderer rearFoot;
    /**
     * The front foot Model renderer of the dragon
     */
    private final ModelRenderer frontFoot;
    /**
     * The wing Model renderer of the dragon
     */
    private final ModelRenderer wing;
    /**
     * The wing tip Model renderer of the dragon
     */
    private final ModelRenderer wingTip;
    private float partialTicks;

    public AncientKingEntityModel() {
        this.textureWidth = 256;
        this.textureHeight = 256;
        this.setTextureOffset("body.body", 0, 0);
        this.setTextureOffset("wing.skin", -56, 88);
        this.setTextureOffset("wingtip.skin", -56, 144);
        this.setTextureOffset("rearleg.main", 0, 0);
        this.setTextureOffset("rearfoot.main", 112, 0);
        this.setTextureOffset("rearlegtip.main", 196, 0);
        this.setTextureOffset("head.upperhead", 112, 30);
        this.setTextureOffset("wing.bone", 112, 88);
        this.setTextureOffset("head.upperlip", 176, 44);
        this.setTextureOffset("jaw.jaw", 176, 65);
        this.setTextureOffset("frontleg.main", 112, 104);
        this.setTextureOffset("wingtip.bone", 112, 136);
        this.setTextureOffset("frontfoot.main", 144, 104);
        this.setTextureOffset("neck.box", 192, 104);
        this.setTextureOffset("frontlegtip.main", 226, 138);
        this.setTextureOffset("body.scale", 220, 53);
        this.setTextureOffset("head.scale", 0, 0);
        this.setTextureOffset("neck.scale", 48, 0);
        this.setTextureOffset("head.nostril", 112, 0);
        float f = -16.0F;
        this.jaw = new ModelRenderer(this, "jaw");
        this.jaw.setRotationPoint(0.0F, 4.0F, -8.0F);
        this.jaw.addBox("jaw", -6.0F, 0.0F, -16.0F, 12, 4, 16);

        heads.add(initHead(this.jaw, 0));
        heads.add(initHead(this.jaw, -2));
        heads.add(initHead(this.jaw, 3));

        for (int i = -1; i <= 1; i++) {
            ModelRenderer spine = new ModelRenderer(this, "neck");
            spine.addBox("box", -5.0F, -5.0F, -5.0F, 10, 10, 10);
            spine.addBox("scale", -1.0F, -9.0F, -3.0F, 2, 4, 6);

            spines.put(i * 45, spine);
        }

        this.body = new ModelRenderer(this, "body");
        this.body.setRotationPoint(0.0F, 4.0F, 8.0F);
        this.body.addBox("body", -12.0F, 0.0F, -16.0F, 24, 24, 64);
        this.body.addBox("scale", -1.0F, -6.0F, -10.0F, 2, 6, 12);
        this.body.addBox("scale", -1.0F, -6.0F, 10.0F, 2, 6, 12);
        this.body.addBox("scale", -1.0F, -6.0F, 30.0F, 2, 6, 12);
        this.wing = new ModelRenderer(this, "wing");
        this.wing.setRotationPoint(-12.0F, 5.0F, 2.0F);
        this.wing.addBox("bone", -56.0F, -4.0F, -4.0F, 56, 8, 8);
        this.wing.addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56);
        this.wingTip = new ModelRenderer(this, "wingtip");
        this.wingTip.setRotationPoint(-56.0F, 0.0F, 0.0F);
        this.wingTip.addBox("bone", -56.0F, -2.0F, -2.0F, 56, 4, 4);
        this.wingTip.addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56);
        this.wing.addChild(this.wingTip);
        this.frontLeg = new ModelRenderer(this, "frontleg");
        this.frontLeg.setRotationPoint(-12.0F, 20.0F, 2.0F);
        this.frontLeg.addBox("main", -4.0F, -4.0F, -4.0F, 8, 24, 8);
        this.frontLegTip = new ModelRenderer(this, "frontlegtip");
        this.frontLegTip.setRotationPoint(0.0F, 20.0F, -1.0F);
        this.frontLegTip.addBox("main", -3.0F, -1.0F, -3.0F, 6, 24, 6);
        this.frontLeg.addChild(this.frontLegTip);
        this.frontFoot = new ModelRenderer(this, "frontfoot");
        this.frontFoot.setRotationPoint(0.0F, 23.0F, 0.0F);
        this.frontFoot.addBox("main", -4.0F, 0.0F, -12.0F, 8, 4, 16);
        this.frontLegTip.addChild(this.frontFoot);
        this.rearLeg = new ModelRenderer(this, "rearleg");
        this.rearLeg.setRotationPoint(-16.0F, 16.0F, 42.0F);
        this.rearLeg.addBox("main", -8.0F, -4.0F, -8.0F, 16, 32, 16);
        this.rearLegTip = new ModelRenderer(this, "rearlegtip");
        this.rearLegTip.setRotationPoint(0.0F, 32.0F, -4.0F);
        this.rearLegTip.addBox("main", -6.0F, -2.0F, 0.0F, 12, 32, 12);
        this.rearLeg.addChild(this.rearLegTip);
        this.rearFoot = new ModelRenderer(this, "rearfoot");
        this.rearFoot.setRotationPoint(0.0F, 31.0F, 4.0F);
        this.rearFoot.addBox("main", -9.0F, 0.0F, -20.0F, 18, 6, 24);
        this.rearLegTip.addChild(this.rearFoot);
    }

    private ModelRenderer initHead(ModelRenderer jaw, int offset) {
        ModelRenderer head = new ModelRenderer(this, "head");
        head.addBox("upperlip", -6.0F, -1.0F, -24.0F, 12, 5, 16);
        head.addBox("upperhead", -8.0F, -8.0F, -10.0F, 16, 16, 16);
        head.mirror = true;
        head.addBox("scale", -5.0F, -12.0F, -4.0F, 2, 4, 6);
        head.addBox("nostril", -5.0F, -3.0F, -22.0F, 2, 2, 4);
        head.mirror = false;
        head.addBox("scale", 3.0F, -12.0F, -4.0F, 2, 4, 6);
        head.addBox("nostril", 3.0F, -3.0F, -22.0F, 2, 2, 4);
        head.addChild(jaw);

        head.offsetZ = offset;

        return head;
    }

    /**
     * Used for easily adding entity-dependent animations. The second and third float params here are the same second
     * and third as in the setRotationAngles method.
     */
    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTickTime) {
        this.partialTicks = partialTickTime;
    }

    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        GlStateManager.pushMatrix();
        float f = (float) (Math.cos(ageInTicks / 80)) * 2;
        this.jaw.rotateAngleX = (float) (Math.sin(f * ((float) Math.PI * 2F)) + 1.0D) * 0.2F;
        float f1 = (float) (Math.sin(f * ((float) Math.PI * 2F) - 1.0F) + 1.0D);
        f1 = (f1 * f1 + f1 * 2.0F) * 0.05F;
        GlStateManager.translate(0.0F, f1 - 2.0F, -3.0F);
        GlStateManager.rotate(f1 * 2.0F, 1.0F, 0.0F, 0.0F);
        float f2;
        float f4 = 0.0F;
        float f8;
        f2 = 20.0F;
        float f3 = -12.0F;

        int index = 0;

        for (Map.Entry<Integer, ModelRenderer> entry : spines.entrySet()) {
            ModelRenderer neck = entry.getValue();
            neck.offsetX = 0;
            neck.rotateAngleX = 0;
            neck.rotateAngleZ = 0;
            neck.rotateAngleY = (float) Math.sin(entry.getKey());
            f2 = 20;
            f3 = -12;
            f4 = 0;

            for (int i = 0; i < 5; ++i) {
                neck.rotationPointY = f2;
                neck.rotationPointZ = f3;
                neck.rotationPointX = f4;
                f2 = (float) ((double) f2 + Math.sin(neck.rotateAngleX) * 10.0D);
                f3 = (float) ((double) f3 - Math.cos(neck.rotateAngleY) * Math.cos(neck.rotateAngleX) * 10.0D);
                f4 = (float) ((double) f4 - Math.sin(neck.rotateAngleY) * Math.cos(neck.rotateAngleX) * 10.0D);
                neck.render(scale);
            }

            ModelRenderer head = heads.get(index);
            head.offsetX = 0;
            head.offsetZ = 0;
            head.offsetY = 0;
            head.rotationPointY = f2;
            head.rotationPointZ = f3;
            head.rotationPointX = f4;
            head.render(scale);

            index++;
        }

//        for (ModelRenderer x : heads) {
//            x.rotationPointY = f2;
//            x.rotationPointZ = f3;
//            x.rotationPointX = f4;
//            x.render(scale);
//        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(1.5F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(0.0F, -1.0F, 0.0F);
        this.body.rotateAngleZ = 0.0F;
        this.body.render(scale);

        for (int j = 0; j < 2; ++j) {
            GlStateManager.enableCull();
            float f11 = f * ((float) Math.PI * 2F);
            this.wing.rotateAngleX = 0.125F - (float) Math.cos(f11) * 0.2F;
            this.wing.rotateAngleY = 0.25F;
            this.wing.rotateAngleZ = (float) (Math.sin(f11) + 0.125D) * 0.8F;
            this.wingTip.rotateAngleZ = -((float) (Math.sin(f11 + 2.0F) + 0.5D)) * 0.75F;
            this.rearLeg.rotateAngleX = 1.0F + f1 * 0.1F;
            this.rearLegTip.rotateAngleX = 0.5F + f1 * 0.1F;
            this.rearFoot.rotateAngleX = 0.75F + f1 * 0.1F;
            this.frontLeg.rotateAngleX = 1.3F + f1 * 0.1F;
            this.frontLegTip.rotateAngleX = -0.5F - f1 * 0.1F;
            this.frontFoot.rotateAngleX = 0.75F + f1 * 0.1F;
            this.wing.render(scale);
            this.frontLeg.render(scale);
            this.rearLeg.render(scale);
            GlStateManager.scale(-1.0F, 1.0F, 1.0F);

            if (j == 0) {
                GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
            }
        }

        GlStateManager.popMatrix();
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.disableCull();
        float f10 = -((float) Math.sin(f * ((float) Math.PI * 2F))) * 0.0F;
        f8 = f * ((float) Math.PI * 2F);
        f2 = 10.0F;
        f3 = 60.0F;
        f4 = 0.0F;

        // Main part
        ModelRenderer spine = spines.get(0);

        for (int k = 0; k < 12; ++k) {
            f10 = (float) ((double) f10 + Math.sin((float) k * 0.45F + f8) * 0.05000000074505806D);
            spine.rotationPointY = f2;
            spine.rotationPointZ = f3;
            spine.rotationPointX = f4;
            f2 = (float) ((double) f2 + Math.sin(spine.rotateAngleX) * 10.0D);
            f3 = (float) ((double) f3 - Math.cos(spine.rotateAngleY) * Math.cos(spine.rotateAngleX) * 10.0D);
            f4 = (float) ((double) f4 - Math.sin(spine.rotateAngleY) * Math.cos(spine.rotateAngleX) * 10.0D);
            spine.render(scale);
        }

        GlStateManager.popMatrix();
    }

    /**
     * Updates the rotations in the parameters for rotations greater than 180 degrees or less than -180 degrees. It adds
     * or subtracts 360 degrees, so that the appearance is the same, although the numbers are then simplified to range -
     * 180 to 180
     */
    private float updateRotations(double p_78214_1_) {
        while (p_78214_1_ >= 180.0D) {
            p_78214_1_ -= 360.0D;
        }

        while (p_78214_1_ < -180.0D) {
            p_78214_1_ += 360.0D;
        }

        return (float) p_78214_1_;
    }
}
