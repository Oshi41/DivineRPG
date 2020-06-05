package divinerpg.capabilities.gravity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class GravityHandler implements IGravity {
    private double gravity;

    /**
     * NBT ctor
     */
    public GravityHandler() {

    }

    public GravityHandler(double multiplier) {
        gravity = multiplier;
    }

    @Override
    public double getGravityMultiplier() {
        return gravity;
    }

    @Override
    public void setGravityMultiplier(double value) {
        gravity = value;
    }

    @Override
    public void applyForWorld(@Nonnull World world) {
        if (getGravityMultiplier() == 1)
            return;

        world.getLoadedEntityList().parallelStream()
                .forEach(this::applyGravity);
    }

    private void applyGravity(Entity entity) {
        if (entity == null || entity.hasNoGravity())
            return;

        double gravity = getGravity(entity);

        // no gravity
        if (gravity == 0)
            return;

        double gravityTick = gravity * getGravityMultiplier();
        entity.motionY += gravity - gravityTick;
    }

    /**
     * Copy of entity gravity logic
     *
     * @param entity
     * @return
     */
    private double getGravity(Entity entity) {
        if (entity == null || entity.hasNoGravity())
            return 0;

        if (entity instanceof EntityItem)
            return 0.04;

        // cant apply on flying player
        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isFlying)
            return 0;

        if (entity instanceof EntityLivingBase) {
            EntityLivingBase livingBase = (EntityLivingBase) entity;

            if (livingBase.isServerWorld() || livingBase.canPassengerSteer()) {
                if (entity.isInWater() || entity.isInLava()) {
                    return 0.02;
                }

                if (!livingBase.isElytraFlying()) {
                    if (!livingBase.isPotionActive(MobEffects.LEVITATION)) {
                        BlockPos pos = new BlockPos(entity.posX, 0.0D, entity.posZ);
                        if (!entity.world.isRemote
                                || entity.world.isBlockLoaded(pos)
                                && entity.world.getChunkFromBlockCoords(pos).isLoaded()) {
                            return 0.08;
                        }
                    }
                }
            }
        }

        return 0;
    }
}
