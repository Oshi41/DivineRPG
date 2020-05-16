package divinerpg.objects.entities.entity.projectiles.king;

import divinerpg.objects.entities.entity.projectiles.EntityHeatSeekingProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityKingRage extends EntityHeatSeekingProjectile {
    public EntityKingRage(World w) {
        super(w);
        init();
    }

    public EntityKingRage(World w, EntityLivingBase e) {
        super(w, e);
        init();
    }

    private void init() {
        setPlayersOnly();
        this.setSize(0.3125F, 0.3125F);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        super.onImpact(result);

        if (result.typeOfHit == RayTraceResult.Type.MISS)
            return;

        setDead();

        if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
            if (result.entityHit instanceof EntityLivingBase) {
                onHit(((EntityLivingBase) result.entityHit));
            }
        }

        if (result.typeOfHit == RayTraceResult.Type.BLOCK
                && !world.isRemote) {
            this.world.newExplosion(this,
                    this.posX,
                    this.posY,
                    this.posZ,
                    5,
                    false,
                    net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this.thrower));

            EntityAreaEffectCloud entityareaeffectcloud = new EntityAreaEffectCloud(this.world, this.posX, this.posY, this.posZ);
            entityareaeffectcloud.setOwner(this.thrower);
            entityareaeffectcloud.setParticle(EnumParticleTypes.CLOUD);
            entityareaeffectcloud.setRadius(3.0F);
            entityareaeffectcloud.setDuration(600);
            entityareaeffectcloud.setRadiusPerTick((7.0F - entityareaeffectcloud.getRadius()) / (float)entityareaeffectcloud.getDuration());
            entityareaeffectcloud.addEffect(new PotionEffect(MobEffects.LEVITATION, 20 * 3, 2));
            this.world.spawnEntity(entityareaeffectcloud);
        }
    }

    protected void onHit(EntityLivingBase target) {
        if (world.isRemote) {
            for (int i = 0; i < 5; i++) {
                world.spawnParticle(EnumParticleTypes.CRIT_MAGIC,
                        true,
                        target.posX + (rand.nextFloat() - 0.5) * 2,
                        target.posY + (rand.nextFloat() - 0.5) * 2,
                        target.posZ + (rand.nextFloat() - 0.5) * 2,
                        (rand.nextFloat() - 0.5) * 2,
                        (rand.nextFloat() - 0.5) * 2,
                        (rand.nextFloat() - 0.5) * 2
                );
            }
        } else {
            target.setHealth(target.getHealth() / 4);
        }

    }
}
