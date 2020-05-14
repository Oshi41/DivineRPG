package divinerpg.objects.entities.entity.vanilla.dragon.phase;

import divinerpg.objects.entities.entity.vanilla.dragon.DivineDragonBase;
import divinerpg.objects.entities.entity.vanilla.dragon.PhaseRegistry;
import divinerpg.objects.entities.entity.vanilla.dragon.phase.base.PhaseSittingBase;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PhaseSittingFlaming extends PhaseSittingBase {
    private int flameTicks;
    private int flameCount;
    private EntityAreaEffectCloud areaEffectCloud;

    public PhaseSittingFlaming(DivineDragonBase dragonIn) {
        super(dragonIn);
    }

    /**
     * Generates particle effects appropriate to the phase (or sometimes sounds).
     * Called by dragon's onLivingUpdate. Only used when worldObj.isRemote.
     */
    public void doClientRenderEffects() {
        ++this.flameTicks;

        if (this.flameTicks % 2 == 0 && this.flameTicks < 10) {
            dragon.heads.forEach(x -> {
                Vec3d vec3d = this.dragon.getHeadLookVec(1.0F).normalize();
                vec3d.rotateYaw(-((float) Math.PI / 4F));
                double d0 = x.posX;
                double d1 = x.posY + (double) (x.height / 2.0F);
                double d2 = x.posZ;

                for (int i = 0; i < 8; ++i) {
                    double d3 = d0 + this.dragon.getRNG().nextGaussian() / 2.0D;
                    double d4 = d1 + this.dragon.getRNG().nextGaussian() / 2.0D;
                    double d5 = d2 + this.dragon.getRNG().nextGaussian() / 2.0D;

                    for (int j = 0; j < 6; ++j) {
                        this.dragon.world.spawnParticle(EnumParticleTypes.DRAGON_BREATH, d3, d4, d5, -vec3d.x * 0.07999999821186066D * (double) j, -vec3d.y * 0.6000000238418579D, -vec3d.z * 0.07999999821186066D * (double) j);
                    }

                    vec3d.rotateYaw(0.19634955F);
                }
            });
        }
    }

    /**
     * Gives the phase a chance to update its status.
     * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
     */
    public void doLocalUpdate() {
        ++this.flameTicks;

        if (this.flameTicks >= dragon.flameTicks) {
            if (this.flameCount >= 4) {
                this.dragon.getPhaseManager().setPhase(PhaseRegistry.TAKEOFF);
            } else {
                this.dragon.getPhaseManager().setPhase(PhaseRegistry.SITTING_SCANNING);
            }
        } else if (this.flameTicks == 10) {
            dragon.heads.forEach(x -> {
                Vec3d vec3d = (new Vec3d(x.posX - this.dragon.posX, 0.0D, x.posZ - this.dragon.posZ)).normalize();
                float f = 5.0F;
                double d0 = x.posX + vec3d.x * 5.0D / 2.0D;
                double d1 = x.posZ + vec3d.z * 5.0D / 2.0D;
                double d2 = x.posY + (double) (x.height / 2.0F);
                BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(MathHelper.floor(d0), MathHelper.floor(d2), MathHelper.floor(d1));

                while (this.dragon.world.isAirBlock(blockpos$mutableblockpos) && d2 >= 0) //Forge: Fix infinite loop if ground is missing.
                {
                    --d2;
                    blockpos$mutableblockpos.setPos(MathHelper.floor(d0), MathHelper.floor(d2), MathHelper.floor(d1));
                }

                d2 = MathHelper.floor(d2) + 1;
                this.areaEffectCloud = new EntityAreaEffectCloud(this.dragon.world, d0, d2, d1);
                this.areaEffectCloud.setOwner(this.dragon);
                this.areaEffectCloud.setRadius(5.0F);
                this.areaEffectCloud.setDuration(200);
                this.areaEffectCloud.setParticle(EnumParticleTypes.DRAGON_BREATH);
                this.areaEffectCloud.addEffect(new PotionEffect(MobEffects.INSTANT_DAMAGE));
                this.dragon.world.spawnEntity(this.areaEffectCloud);
            });

        }
    }

    /**
     * Called when this phase is set to active
     */
    public void initPhase() {
        this.flameTicks = 0;
        ++this.flameCount;
    }

    public void removeAreaEffect() {
        if (this.areaEffectCloud != null) {
            this.areaEffectCloud.setDead();
            this.areaEffectCloud = null;
        }
    }

    @Override
    public ResourceLocation getId() {
        return PhaseRegistry.SITTING_FLAMING;
    }

    public void resetFlameCount() {
        this.flameCount = 0;
    }
}
