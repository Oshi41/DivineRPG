package divinerpg.objects.entities.entity.vanilla.dragon.phase;

import divinerpg.DivineRPG;
import divinerpg.objects.entities.entity.vanilla.dragon.DivineDragonBase;
import divinerpg.objects.entities.entity.vanilla.dragon.PhaseRegistry;
import divinerpg.objects.entities.entity.vanilla.dragon.phase.base.PhaseBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityDragonFireball;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class PhaseStrafePlayer extends PhaseBase {
    private final Logger LOGGER;
    private int fireballCharge;
    private Path currentPath;
    private Vec3d targetLocation;
    private EntityLivingBase attackTarget;
    private boolean holdingPatternClockwise;

    public PhaseStrafePlayer(DivineDragonBase dragonIn) {
        super(dragonIn);
        LOGGER = DivineRPG.logger;
    }

    /**
     * Gives the phase a chance to update its status.
     * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
     */
    public void doLocalUpdate() {
        if (this.attackTarget == null) {
            LOGGER.warn("Skipping player strafe phase because no player was found");
            this.dragon.getPhaseManager().setPhase(PhaseRegistry.HOVER);
        } else {
            if (this.currentPath != null && this.currentPath.isFinished()) {
                double d0 = this.attackTarget.posX;
                double d1 = this.attackTarget.posZ;
                double d2 = d0 - this.dragon.posX;
                double d3 = d1 - this.dragon.posZ;
                double d4 = MathHelper.sqrt(d2 * d2 + d3 * d3);
                double d5 = Math.min(0.4000000059604645D + d4 / 80.0D - 1.0D, 10.0D);
                this.targetLocation = new Vec3d(d0, this.attackTarget.posY + d5, d1);
            }

            double d12 = this.targetLocation == null ? 0.0D : this.targetLocation.squareDistanceTo(this.dragon.posX, this.dragon.posY, this.dragon.posZ);

            if (d12 < 100.0D || d12 > maxDistanceSq()) {
                this.findNewTarget();
            }

            double d13 = 64.0D;

            if (this.attackTarget.getDistanceSq(this.dragon) < 4096.0D) {
                if (this.dragon.canEntityBeSeen(this.attackTarget)) {
                    ++this.fireballCharge;
                    Vec3d vec3d1 = (new Vec3d(this.attackTarget.posX - this.dragon.posX, 0.0D, this.attackTarget.posZ - this.dragon.posZ)).normalize();
                    Vec3d vec3d = (new Vec3d(MathHelper.sin(this.dragon.rotationYaw * 0.017453292F), 0.0D, -MathHelper.cos(this.dragon.rotationYaw * 0.017453292F))).normalize();
                    float f1 = (float) vec3d.dotProduct(vec3d1);
                    float f = (float) (Math.acos(f1) * (180D / Math.PI));
                    f = f + 0.5F;

                    if (this.fireballCharge >= 5 && f >= 0.0F && f < 10.0F) {
                        dragon.attackEntityWithRangedAttack(attackTarget, f);
                        this.fireballCharge = 0;

                        if (this.currentPath != null) {
                            while (!this.currentPath.isFinished()) {
                                this.currentPath.incrementPathIndex();
                            }
                        }

                        this.dragon.getPhaseManager().setPhase(PhaseRegistry.HOVER);
                    }
                } else if (this.fireballCharge > 0) {
                    --this.fireballCharge;
                }
            } else if (this.fireballCharge > 0) {
                --this.fireballCharge;
            }
        }
    }

    private void findNewTarget() {
        if (this.currentPath == null || this.currentPath.isFinished()) {
            int i = this.dragon.initPathPoints();
            int j = i;

            if (this.dragon.getRNG().nextInt(8) == 0) {
                this.holdingPatternClockwise = !this.holdingPatternClockwise;
                j = i + 6;
            }

            if (this.holdingPatternClockwise) {
                ++j;
            } else {
                --j;
            }

            j = j - 12;
            j = j & 7;
            j = j + 12;

            this.currentPath = this.dragon.findPath(i, j, null);

            if (this.currentPath != null) {
                this.currentPath.incrementPathIndex();
            }
        }

        this.navigateToNextPathNode();
    }

    private void navigateToNextPathNode() {
        if (this.currentPath != null && !this.currentPath.isFinished()) {
            Vec3d vec3d = this.currentPath.getCurrentPos();
            this.currentPath.incrementPathIndex();
            double x = vec3d.x;
            double z = vec3d.z;
            double y;

            while (true) {
                y = vec3d.y + (double) (this.dragon.getRNG().nextFloat() * 20.0F);

                if (y >= vec3d.y) {
                    break;
                }
            }

            this.targetLocation = new Vec3d(x, y, z);
        }
    }

    /**
     * Called when this phase is set to active
     */
    public void initPhase() {
        this.fireballCharge = 0;
        this.targetLocation = null;
        this.currentPath = null;
        this.attackTarget = null;
    }

    @Override
    public ResourceLocation getId() {
        return PhaseRegistry.STRIFE_PLAYER;
    }

    public void setTarget(EntityLivingBase target) {
        this.attackTarget = target;
        int i = this.dragon.initPathPoints();
        int j = this.dragon.getNearestPpIdx(this.attackTarget.posX, this.attackTarget.posY, this.attackTarget.posZ);
        int k = MathHelper.floor(this.attackTarget.posX);
        int l = MathHelper.floor(this.attackTarget.posZ);
        double d0 = (double) k - this.dragon.posX;
        double d1 = (double) l - this.dragon.posZ;
        double d2 = MathHelper.sqrt(d0 * d0 + d1 * d1);
        double d3 = Math.min(0.4000000059604645D + d2 / 80.0D - 1.0D, 10.0D);
        int i1 = MathHelper.floor(this.attackTarget.posY + d3);
        PathPoint pathpoint = new PathPoint(k, i1, l);
        this.currentPath = this.dragon.findPath(i, j, pathpoint);

        if (this.currentPath != null) {
            this.currentPath.incrementPathIndex();
            this.navigateToNextPathNode();
        }
    }

    /**
     * Returns the location the dragon is flying toward
     */
    @Nullable
    public Vec3d getTargetLocation() {
        return this.targetLocation;
    }
}
