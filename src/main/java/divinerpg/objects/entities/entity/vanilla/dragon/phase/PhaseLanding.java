package divinerpg.objects.entities.entity.vanilla.dragon.phase;

import divinerpg.objects.entities.entity.vanilla.dragon.DivineDragonBase;
import divinerpg.objects.entities.entity.vanilla.dragon.PhaseRegistry;
import divinerpg.objects.entities.entity.vanilla.dragon.phase.base.PhaseBase;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class PhaseLanding extends PhaseBase {
    private Vec3d targetLocation;

    public PhaseLanding(DivineDragonBase dragonIn) {
        super(dragonIn);
    }

    /**
     * Generates particle effects appropriate to the phase (or sometimes sounds).
     * Called by dragon's onLivingUpdate. Only used when worldObj.isRemote.
     */
    public void doClientRenderEffects() {
        Vec3d vec3d = this.dragon.getHeadLookVec(1.0F).normalize();
        vec3d.rotateYaw(-((float) Math.PI / 4F));

        dragon.heads.forEach(x -> {
            double d0 = x.posX;
            double d1 = x.posY + (double) (x.height / 2.0F);
            double d2 = x.posZ;

            for (int i = 0; i < 8; ++i) {
                double d3 = d0 + this.dragon.getRNG().nextGaussian() / 2.0D;
                double d4 = d1 + this.dragon.getRNG().nextGaussian() / 2.0D;
                double d5 = d2 + this.dragon.getRNG().nextGaussian() / 2.0D;
                this.dragon.world.spawnParticle(EnumParticleTypes.DRAGON_BREATH, d3, d4, d5, -vec3d.x * 0.07999999821186066D + this.dragon.motionX, -vec3d.y * 0.30000001192092896D + this.dragon.motionY, -vec3d.z * 0.07999999821186066D + this.dragon.motionZ);
                vec3d.rotateYaw(0.19634955F);
            }
        });
    }

    /**
     * Gives the phase a chance to update its status.
     * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
     */
    public void doLocalUpdate() {
        if (this.targetLocation == null) {
            this.targetLocation = new Vec3d(this.dragon.getDragonGuardCenter());
        }

        if (this.targetLocation.squareDistanceTo(this.dragon.posX, this.dragon.posY, this.dragon.posZ) < 1.0D) {
            this.dragon.getPhaseManager().<PhaseSittingFlaming>getPhase(PhaseRegistry.SITTING_FLAMING).resetFlameCount();
            this.dragon.getPhaseManager().setPhase(PhaseRegistry.SITTING_SCANNING);
        }
    }

    /**
     * Returns the maximum amount dragon may rise or fall during this phase
     */
    public float getMaxRiseOrFall() {
        return 1.5F;
    }

    public float getYawFactor() {
        float f = MathHelper.sqrt(this.dragon.motionX * this.dragon.motionX + this.dragon.motionZ * this.dragon.motionZ) + 1.0F;
        float f1 = Math.min(f, 40.0F);
        return f1 / f;
    }

    @Override
    public ResourceLocation getId() {
        return PhaseRegistry.LANDING;
    }

    /**
     * Called when this phase is set to active
     */
    public void initPhase() {
        this.targetLocation = null;
    }

    /**
     * Returns the location the dragon is flying toward
     */
    @Nullable
    public Vec3d getTargetLocation() {
        return this.targetLocation;
    }
}
