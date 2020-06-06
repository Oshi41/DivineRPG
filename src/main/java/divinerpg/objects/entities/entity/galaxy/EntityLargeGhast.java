package divinerpg.objects.entities.entity.galaxy;

import divinerpg.objects.entities.ai.AIDivineFireballAttack;
import divinerpg.objects.entities.entity.EntityDivineFlyingMob;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityLargeGhast extends EntityDivineFlyingMob {

    public EntityLargeGhast(World world) {
        super(world);
        setSize(7, 7);
    }

    @Override
    protected void initEntityAI() {
        super.initEntityAI();
    }

    @Nullable
    @Override
    protected AIDivineFireballAttack createShootAI() {
        return new AIDivineFireballAttack(this,
                this::attackEntity,
                40,
                100,
                SoundEvents.ENTITY_GHAST_WARN,
                SoundEvents.ENTITY_GHAST_SHOOT);
    }

    private Entity attackEntity(EntityLivingBase parent, Entity target) {
        world.addWeatherEffect(
                new EntityLightningBolt(target.world,
                        target.posX,
                        target.posY,
                        target.posZ,
                        false)
        );

        return null;
    }
}
