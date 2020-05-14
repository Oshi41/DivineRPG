package divinerpg.objects.entities.ai;

import divinerpg.objects.entities.entity.vanilla.dragon.IDragon;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;

public class AIDivineCircleFlying<T extends EntityLivingBase & IDragon> extends EntityAIBase {
    private T parent;
    private BlockPos center = null;
    private AxisAlignedBB area;

    public AIDivineCircleFlying(T e) {
        parent = e;
        setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        return true;
    }

    @Override
    public void updateTask() {
        // check if center changed
        setCenter(parent.getCenter());

        if (center == null)
            return;

        // can attack
        if (parent.getAttackingEntity() != null && area.contains(parent.getAttackingEntity().getPositionVector())) {
            moveEntity(parent.getAttackingEntity().getPositionVector());
            return;
        }

        Vec3d futurePos = parent.getPositionVector().add(parent.getLookVec().scale(10));

        // corrected pos

        int z = (int) MathHelper.clamp(futurePos.z, area.minZ, area.maxZ);
        int x = (int) MathHelper.clamp(futurePos.x, area.minX, area.maxX);
        int y = parent.world.getHeight(((int) futurePos.x), (int) futurePos.z) + 8;

        moveEntity(new Vec3d(x, y, z));
    }

    private void moveEntity(@Nonnull Vec3d vec3d) {
        // less 50% of health
        boolean fast = parent.getHealth() * 100 / parent.getMaxHealth() < 50;

        double d6 = vec3d.x - parent.posX;
        double d8 = vec3d.z - parent.posZ;
        double d7 = vec3d.y - parent.posY;
        double d3 = d6 * d6 + d7 * d7 + d8 * d8;
        float f5 = 1.6F;
        d7 = MathHelper.clamp(d7 / (double) MathHelper.sqrt(d6 * d6 + d8 * d8), -f5, f5);
        parent.motionY += d7 * 0.10000000149011612D;
        parent.rotationYaw = MathHelper.wrapDegrees(parent.rotationYaw);
        double d4 = MathHelper.clamp(MathHelper.wrapDegrees(180.0D - MathHelper.atan2(d6, d8) * (180D / Math.PI) - (double) parent.rotationYaw), -50.0D, 50.0D);
        Vec3d vec3d1 = (new Vec3d(vec3d.x - parent.posX, vec3d.y - parent.posY, vec3d.z - parent.posZ)).normalize();
        Vec3d vec3d2 = (new Vec3d(MathHelper.sin(parent.rotationYaw * 0.017453292F), parent.motionY, -MathHelper.cos(parent.rotationYaw * 0.017453292F))).normalize();
        float f7 = Math.max(((float) vec3d2.dotProduct(vec3d1) + 0.5F) / 1.5F, 0.0F);
        parent.randomYawVelocity *= 0.8F;
        parent.randomYawVelocity = (float) ((double) parent.randomYawVelocity + d4 * (double) getYawFactor());
        parent.rotationYaw += parent.randomYawVelocity * 0.1F;
        float f8 = (float) (2.0D / (d3 + 1.0D));
        float f9 = 0.06F;
        parent.moveRelative(0.0F, 0.0F, -1.0F, 0.06F * (f7 * f8 + (1.0F - f8)));

        if (!fast) {
            parent.move(MoverType.SELF, parent.motionX * 0.800000011920929D, parent.motionY * 0.800000011920929D, parent.motionZ * 0.800000011920929D);
        } else {
            parent.move(MoverType.SELF, parent.motionX, parent.motionY, parent.motionZ);
        }

        Vec3d vec3d3 = (new Vec3d(parent.motionX, parent.motionY, parent.motionZ)).normalize();
        float f10 = ((float) vec3d3.dotProduct(vec3d2) + 1.0F) / 2.0F;
        f10 = 0.8F + 0.15F * f10;
        parent.motionX *= f10;
        parent.motionZ *= f10;
        parent.motionY *= 0.9100000262260437D;
    }

    private float getYawFactor() {
        float f = MathHelper.sqrt(parent.motionX * parent.motionX + parent.motionZ * parent.motionZ) + 1.0F;
        float f1 = Math.min(f, 40.0F);
        return 0.7F / f1 / f;
    }

    private void setCenter(BlockPos pos) {
        if (pos == center || pos == null)
            return;

        center = pos;
        double distance = getDistance();
        area = new AxisAlignedBB(center.add(-distance, -distance, -distance), center.add(distance, distance, distance));
    }

    private double getDistance() {
        return parent.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue();
    }
}
