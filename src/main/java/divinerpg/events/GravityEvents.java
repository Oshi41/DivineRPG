package divinerpg.events;

import divinerpg.DivineRPG;
import divinerpg.potions.GravityPotion;
import divinerpg.registry.PotionRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = DivineRPG.MODID)
public class GravityEvents {

    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingUpdateEvent e) {
        if (e.getEntityLiving() == null
                || e.getEntityLiving().getEntityWorld() == null)
            return;

        EntityLivingBase entity = e.getEntityLiving();

        // detecting special gravity potion
        PotionEffect effect = entity.getActivePotionEffect(PotionRegistry.Gravity);

        // recheck it only on last tick
        if (effect != null && effect.getDuration() > 1)
            return;

        GravityPotion.trySetEffect(entity);
    }

    @SubscribeEvent
    public void onFallEvent(LivingFallEvent e) {
        if (e.isCanceled())
            return;

        PotionEffect effect = e.getEntityLiving().getActivePotionEffect(PotionRegistry.Gravity);
        if (effect == null)
            return;

        int amplifier = effect.getAmplifier();
        if (amplifier == 100)
            return;

        if (amplifier == 0) {
            e.setCanceled(true);
            return;
        }

        double gravityFactor = amplifier / 100.0;

        e.setDistance((float) (e.getDistance() * gravityFactor));
    }
}