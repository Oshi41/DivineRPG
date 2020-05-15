package divinerpg.objects.entities.entity.vanilla.dragon.phase.base;

import divinerpg.objects.entities.entity.vanilla.dragon.DivineDragonBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public abstract class PhaseBase implements IPhase {
    protected final DivineDragonBase dragon;

    public PhaseBase(DivineDragonBase dragonIn) {
        this.dragon = dragonIn;
    }

    public boolean getIsStationary() {
        return false;
    }

    /**
     * Generates particle effects appropriate to the phase (or sometimes sounds).
     * Called by dragon's onLivingUpdate. Only used when worldObj.isRemote.
     */
    public void doClientRenderEffects() {
    }

    /**
     * Gives the phase a chance to update its status.
     * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
     */
    public void doLocalUpdate() {
    }

    public void onHealingStructureDestroyed(BlockPos pos, DamageSource dmgSrc, @Nullable EntityPlayer plyr) {
    }

    /**
     * Called when this phase is set to active
     */
    public void initPhase() {
    }

    public void removeAreaEffect() {
    }

    /**
     * Returns the maximum amount dragon may rise or fall during this phase
     */
    public float getMaxRiseOrFall() {
        return 0.6F;
    }

    /**
     * Returns the location the dragon is flying toward
     */
    @Nullable
    public Vec3d getTargetLocation() {
        return null;
    }

    /**
     * Normally, just returns damage. If dragon is sitting and src is an arrow, arrow is enflamed and zero damage
     * returned.
     */
    public float getAdjustedDamage(MultiPartEntityPart pt, DamageSource src, float damage) {
        return damage;
    }

    public float getYawFactor() {
        float f = MathHelper.sqrt(this.dragon.motionX * this.dragon.motionX + this.dragon.motionZ * this.dragon.motionZ) + 1.0F;
        float f1 = Math.min(f, 40.0F);
        return 0.7F / f1 / f;
    }

    protected double maxDistanceSq() {
        return Math.pow(dragon.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue(), 2);
    }
}
