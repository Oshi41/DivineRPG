package divinerpg.objects.entities.entity.vanilla.dragon.phase;

import divinerpg.objects.entities.entity.vanilla.dragon.DivineDragonBase;
import divinerpg.objects.entities.entity.vanilla.dragon.PhaseRegistry;
import divinerpg.objects.entities.entity.vanilla.dragon.phase.base.IPhase;
import divinerpg.objects.entities.entity.vanilla.dragon.phase.base.PhaseBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class PhaseHover extends PhaseBase {
    private Path currentPath;
    private Vec3d targetLocation;
    private boolean clockwise;

    public PhaseHover(DivineDragonBase dragonIn) {
        super(dragonIn);
    }

    /**
     * Gives the phase a chance to update its status.
     * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
     */
    public void doLocalUpdate() {
        double d0 = this.targetLocation == null ? 0.0D : this.targetLocation.squareDistanceTo(dragon.posX, dragon.posY, dragon.posZ);

        if (d0 < 100.0D || d0 > maxDistanceSq() || dragon.collidedHorizontally || dragon.collidedVertically) {
            this.findNewTarget();
        }
    }

    /**
     * Called when this phase is set to active
     */
    public void initPhase() {
        this.currentPath = null;
        this.targetLocation = null;
    }

    @Override
    public ResourceLocation getId() {
        return PhaseRegistry.HOVER;
    }

    /**
     * Returns the location the dragon is flying toward
     */
    @Nullable
    public Vec3d getTargetLocation() {
        return this.targetLocation;
    }

    private void findNewTarget() {
        if (this.currentPath != null && this.currentPath.isFinished()) {
            int i = 0;

            if (dragon.getRNG().nextInt(i + 3) == 0) {
                this.dragon.getPhaseManager().setPhase(PhaseRegistry.LANDING_APPROACH);
                return;
            }

            EntityLivingBase target = dragon.getAttackTarget();

            if (target != null) {
                this.strafePlayer(target);
                return;
            }
        }

        if (this.currentPath == null || this.currentPath.isFinished()) {
            int j = dragon.initPathPoints();
            int k = j;

            if (dragon.getRNG().nextInt(8) == 0) {
                this.clockwise = !this.clockwise;
                k = j + 6;
            }

            if (this.clockwise) {
                ++k;
            } else {
                --k;
            }

            k = k - 12;
            k = k & 7;
            k = k + 12;

            this.currentPath = dragon.findPath(j, k, null);

            if (this.currentPath != null) {
                this.currentPath.incrementPathIndex();
            }
        }

        this.navigateToNextPathNode();
    }

    protected void strafePlayer(EntityLivingBase player) {
        this.dragon.getPhaseManager().setPhase(PhaseRegistry.STRIFE_PLAYER);
        IPhase currentPhase = this.dragon.getPhaseManager().getCurrentPhase();

        if (currentPhase instanceof PhaseStrafePlayer) {
            ((PhaseStrafePlayer) currentPhase).setTarget(player);
        }
    }

    private void navigateToNextPathNode() {
        if (this.currentPath != null && !this.currentPath.isFinished()) {
            Vec3d vec3d = this.currentPath.getCurrentPos();
            this.currentPath.incrementPathIndex();
            double d0 = vec3d.x;
            double d1 = vec3d.z;
            double d2;

            while (true) {
                d2 = vec3d.y + (double) (dragon.getRNG().nextFloat() * 20.0F);

                if (d2 >= vec3d.y) {
                    break;
                }
            }

            this.targetLocation = new Vec3d(d0, d2, d1);
        }
    }

    public void onHealingStructureDestroyed(BlockPos pos, DamageSource dmgSrc, @Nullable EntityPlayer plyr) {
        if (plyr != null && !plyr.capabilities.disableDamage) {
            this.strafePlayer(plyr);
        }
    }
}
