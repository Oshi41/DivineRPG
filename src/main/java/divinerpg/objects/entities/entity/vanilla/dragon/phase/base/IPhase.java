package divinerpg.objects.entities.entity.vanilla.dragon.phase.base;

import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public interface IPhase {
    boolean getIsStationary();

    /**
     * Generates particle effects appropriate to the phase (or sometimes sounds).
     * Called by dragon's onLivingUpdate. Only used when worldObj.isRemote.
     */
    void doClientRenderEffects();

    /**
     * Gives the phase a chance to update its status.
     * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
     */
    void doLocalUpdate();

    /**
     * Called when some healing structure was destroyed
     *
     * @param pos    - position of structure
     * @param dmgSrc - damage
     * @param plyr   - destroyer
     */
    void onHealingStructureDestroyed(BlockPos pos, DamageSource dmgSrc, @Nullable EntityPlayer plyr);

    /**
     * Called when this phase is set to active
     */
    void initPhase();

    void removeAreaEffect();

    /**
     * Returns the maximum amount dragon may rise or fall during this phase
     */
    float getMaxRiseOrFall();

    float getYawFactor();

    /**
     * Unique ID of phase
     *
     * @return
     */
    ResourceLocation getId();

    /**
     * Returns the location the dragon is flying toward
     */
    @Nullable
    Vec3d getTargetLocation();

    /**
     * Normally, just returns damage. If dragon is sitting and src is an arrow, arrow is enflamed and zero damage
     * returned.
     */
    float getAdjustedDamage(MultiPartEntityPart pt, DamageSource src, float damage);
}
