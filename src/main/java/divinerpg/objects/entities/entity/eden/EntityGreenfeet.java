package divinerpg.objects.entities.entity.eden;

import divinerpg.objects.entities.entity.EntityDivineMob;
import divinerpg.registry.LootTableRegistry;
import divinerpg.registry.SoundRegistry;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityGreenfeet extends EntityDivineMob {

    public EntityGreenfeet(World worldIn) {
        super(worldIn);
        this.setSize(1, 2);
    }

    @Override
    public float getEyeHeight() {
        return 1.75F;
    }


    @Override
    protected void initEntityAI() {
        super.initEntityAI();
        addAttackingAI();
    }

    @Override
    public int getTotalArmorValue() {
        return 10;
    }

    @Override
    public void onLivingUpdate() {
        if (this.world.isDaytime() && !this.world.isRemote) {
            float lightLevel = this.getBrightness();
            if (lightLevel > 0.5F
                    && this.world.canBlockSeeSky(new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.posY),
                            MathHelper.floor(this.posZ)))
                    && this.rand.nextFloat() * 30.0F < (lightLevel - 0.4F) * 2.0F) {
                this.setFire(8);
            }
        }
        super.onLivingUpdate();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.NESRO;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundRegistry.NESRO_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.NESRO_HURT;
    }

    @Override
    protected ResourceLocation getLootTable() {
        return LootTableRegistry.ENTITIES_GREENFEET;
    }
}
