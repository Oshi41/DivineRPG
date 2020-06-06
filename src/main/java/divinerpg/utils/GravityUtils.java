package divinerpg.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.BlockPos;

public class GravityUtils {

    /**
     * Copy of entity gravity logic
     *
     * @param entity
     * @return
     */
    public static double getGravity(Entity entity) {
        if (entity == null || entity.hasNoGravity())
            return 0;

        if (entity instanceof EntityItem)
            return 0.04;

        // cant apply on flying player
        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isFlying)
            return 0;

        // already flying entity
        if (entity instanceof EntityFlying || entity instanceof net.minecraft.entity.passive.EntityFlying)
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
