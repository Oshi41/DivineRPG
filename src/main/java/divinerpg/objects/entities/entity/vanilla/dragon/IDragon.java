package divinerpg.objects.entities.entity.vanilla.dragon;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public interface IDragon {
    /**
     * Gets center of guarding zone
     *
     * @return
     */
    BlockPos getCenter();

    /**
     * Index into the ring buffer. Incremented once per tick and restarts at 0 once it reaches the end of the buffer.
     */
    AtomicInteger getRingBufferIndex();

    /**
     * Ring buffer array for the last 64 Y-positions and yaw rotations. Used to calculate offsets for the animations.
     */
    AtomicReference<Double[][]> getRingBuffer();

    /**
     * Animation time, used to control the speed of the animation cycles (wings flapping, jaw opening, etc.)
     */
    AtomicReference<Float> getAnimTime();

    /**
     * Animation time at previous tick.
     */
    AtomicReference<Float> getPrevAnimTime();

    //
    // Should automatically inherit
    //

    /**
     * Should inherit from EntityLivingBase
     *
     * @param attribute
     * @return
     */
    IAttributeInstance getEntityAttribute(IAttribute attribute);

    /**
     * Should inherit from EntityLivingBase
     *
     * @return
     */
    float getHealth();

    /**
     * Should inherit from EntityLivingBase
     *
     * @param pos
     * @return
     */
    double getDistanceSqToCenter(BlockPos pos);

    //
    // Defaults
    //

    /**
     * Returns a double[3] array with movement offsets, used to calculate trailing tail/neck positions. [0] = yaw
     * offset, [1] = y offset, [2] = unused, always 0. Parameters: buffer index offset, partial ticks.
     */
    default double[] getMovementOffsets(int offset, float partialTicks) {
        if (this.getHealth() <= 0.0F) {
            partialTicks = 0.0F;
        }

        int ringBufferIndex = getRingBufferIndex().get();
        Double[][] ringBuffer = getRingBuffer().get();

        partialTicks = 1.0F - partialTicks;
        int i = ringBufferIndex - offset & 63;
        int j = ringBufferIndex - offset - 1 & 63;
        double[] adouble = new double[3];
        double d0 = ringBuffer[i][0];
        double d1 = MathHelper.wrapDegrees(ringBuffer[j][0] - d0);
        adouble[0] = d0 + d1 * (double) partialTicks;
        d0 = ringBuffer[i][1];
        d1 = ringBuffer[j][1] - d0;
        adouble[1] = d0 + d1 * (double) partialTicks;
        adouble[2] = ringBuffer[i][2] + (ringBuffer[j][2] - ringBuffer[i][2]) * (double) partialTicks;
        return adouble;
    }

    @SideOnly(Side.CLIENT)
    default float getHeadPartYOffset(int p_184667_1_, double[] p_184667_2_, double[] p_184667_3_) {
        BlockPos blockpos = getCenter();
        float f = Math.max(MathHelper.sqrt(this.getDistanceSqToCenter(blockpos)) / 4.0F, 1.0F);
        return ((float) p_184667_1_ / f);
    }

    default boolean isFast(EntityLivingBase parent) {
        return parent.getHealth() * 100 / parent.getMaxHealth() < 50;
    }

    /**
     * Gets max entity reach distance
     *
     * @return
     */
    default double distance() {
        return getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue();
    }

    /**
     * Calculates guard zone
     *
     * @return
     */
    default AxisAlignedBB getGuardZone() {
        double distance = distance();
        BlockPos center = getCenter();
        return new AxisAlignedBB(center.add(-distance, -distance, -distance), center.add(distance, distance, distance));
    }

    /**
     * Should call every tick
     *
     * @param entity
     */
    default void tick(EntityLiving entity) {
        rememberPosition(entity);

        AxisAlignedBB zone = getGuardZone();

        EntityLivingBase target = entity.getAttackTarget();
        if (target != null && zone.contains(target.getPositionVector())) {
            moveEntity(entity, target.getPositionVector());
            return;
        }

        Vec3d futurePos = entity.getPositionVector().add(entity.getLookVec().scale(10));

        // corrected pos

        int z = (int) MathHelper.clamp(futurePos.z, zone.minZ, zone.maxZ);
        int x = (int) MathHelper.clamp(futurePos.x, zone.minX, zone.maxX);
        int y = entity.world.getHeight(((int) futurePos.x), (int) futurePos.z) + 8;

        moveEntity(entity, new Vec3d(x, y, z));
    }

    /**
     * Store rotation info
     *
     * @param entity
     */
    default void rememberPosition(EntityLiving entity) {
        AtomicReference<Float> animTime = getAnimTime();
        AtomicReference<Float> prevAnimTime = getPrevAnimTime();
        prevAnimTime.set(animTime.get());

        if (!entity.isAIDisabled()) {
            AtomicInteger index = getRingBufferIndex();
            Double[][] ringBuffer = getRingBuffer().get();

            if (index.get() < 0) {
                for (int i = 0; i < ringBuffer.length; ++i) {
                    ringBuffer[i][0] = (double) entity.rotationYaw;
                    ringBuffer[i][1] = entity.posY;
                }
            }

            if (index.incrementAndGet() == ringBuffer.length) {
                index.set(0);
            }

            ringBuffer[index.get()][0] = (double) entity.rotationYaw;
            ringBuffer[index.get()][1] = entity.posY;

            float f11 = 0.2F / (MathHelper.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ) * 10.0F + 1.0F);
            f11 = f11 * (float) Math.pow(2.0D, entity.motionY);

            if (!isFast(entity))
                f11 /= 2;

            animTime.set(animTime.get() + f11);
        } else {
            animTime.set(0.5F);
        }
    }

    /**
     * @param vec3d
     */
    default void moveEntity(@Nonnull EntityLivingBase parent, @Nonnull Vec3d vec3d) {
        // less 50% of health
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
        parent.randomYawVelocity = (float) ((double) parent.randomYawVelocity + d4 * getYawFactor(parent));
        parent.rotationYaw += parent.randomYawVelocity * 0.1F;
        float f8 = (float) (2.0D / (d3 + 1.0D));
        float f9 = 0.06F;
        parent.moveRelative(0.0F, 0.0F, -1.0F, 0.06F * (f7 * f8 + (1.0F - f8)));

        if (!isFast(parent)) {
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

    default double getYawFactor(EntityLivingBase parent) {
        float f = MathHelper.sqrt(parent.motionX * parent.motionX + parent.motionZ * parent.motionZ) + 1.0F;
        float f1 = Math.min(f, 40.0F);
        return 0.7F / f1 / f;
    }
}
