package divinerpg.objects.entities.entity.mortum;

import divinerpg.objects.entities.entity.EntityDivineMob;
import divinerpg.registry.LootTableRegistry;
import divinerpg.registry.SoundRegistry;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class EntitySoulSpider extends EntityDivineMob {

    public EntitySoulSpider(World worldIn) {
        super(worldIn);
        this.setSize(0.3F, 0.5F);
    }

    @Override
    public float getEyeHeight() {
        return 0.35F;
    }


    @Override
    protected void initEntityAI() {
        super.initEntityAI();
        addAttackingAI();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.ticksExisted > 600 && !this.world.isRemote)
            this.setDead();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.GROWL;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundRegistry.GROWL_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.GROWL_HURT;
    }

    @Override
    protected ResourceLocation getLootTable() {
        return LootTableRegistry.ENTITIES_SOUL_SPIDER;
    }
}
