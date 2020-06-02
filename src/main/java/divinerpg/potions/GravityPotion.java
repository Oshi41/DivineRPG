package divinerpg.potions;

import divinerpg.dimensions.galaxy.IGravityProvider;
import divinerpg.registry.PotionRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class GravityPotion extends Potion {
    public GravityPotion() {
        super(false, 13565951);
    }

    private static int getGravityAmplifier(EntityLivingBase entity) {
        if (entity != null && entity.getEntityWorld() != null) {
            Biome biome = entity.getEntityWorld().getBiome(entity.getPosition());
            if (biome instanceof IGravityProvider) {
                return ((IGravityProvider) biome).gravity();
            }
        }

        // base value
        return 100;
    }

    public static void trySetEffect(EntityLivingBase e) {
        if (e == null || e.getEntityWorld() == null)
            return;

        // can't apply on such entity
        if (e.hasNoGravity())
            return;

        PotionEffect effect = e.getActivePotionEffect(PotionRegistry.Gravity);

        if (effect != null && effect.getDuration() > 0)
            return;

        int amplifier = getGravityAmplifier(e);
        if (amplifier == 100)
            return;

        e.addPotionEffect(new PotionEffect(PotionRegistry.Gravity, 20, amplifier, false, false));
    }

    /**
     * Amplifier can apply values from 0 .. infinity
     * Where 100 means normal gravity
     * 0 means no gravity at all
     * After 100 means more gravity like on Jupiter
     *
     * @param entity
     * @param amplifier
     */
    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        super.performEffect(entity, amplifier);

        double appliedGravity = getGravity(entity);
        double gravityTick = amplifier / 100.0 * appliedGravity;

        // need to fix gravity difference
        entity.motionY += appliedGravity - gravityTick;
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration > 0;
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap map, int amplifier) {
        super.removeAttributesModifiersFromEntity(entityLivingBaseIn, map, amplifier);

        // repeat effect
        trySetEffect(entityLivingBaseIn);
    }

    /**
     * Copy of entity gravity logic
     *
     * @param entity
     * @return
     */
    private double getGravity(EntityLivingBase entity) {
        if (!entity.hasNoGravity()
                && (entity.isServerWorld() || entity.canPassengerSteer())
                && (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).capabilities.isFlying)) {

            if (entity.isInWater() || entity.isInLava()) {
                return 0.02D;
            }

            if (!entity.isElytraFlying()) {
                if (!entity.isPotionActive(MobEffects.LEVITATION)) {
                    BlockPos pos = new BlockPos(entity.posX, 0.0D, entity.posZ);
                    if (!entity.world.isRemote
                            || entity.world.isBlockLoaded(pos)
                            && entity.world.getChunkFromBlockCoords(pos).isLoaded()) {
                        return 0.08D;
                    }
                }
            }
        }

        return 0;
    }
}
