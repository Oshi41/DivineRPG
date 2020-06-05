package divinerpg.objects.entities.entity.boss;

import divinerpg.objects.entities.ai.AIDivineFireballAttack;
import divinerpg.objects.entities.ai.ILaunchThrowable;
import divinerpg.objects.entities.entity.EntityDivineFlyingMob;
import divinerpg.objects.entities.entity.projectiles.EntityCoriShot;
import divinerpg.objects.entities.entity.skythern.EntityAdvancedCori;
import divinerpg.objects.entities.entity.eden.EntityWeakCori;
import divinerpg.registry.LootTableRegistry;
import divinerpg.registry.SoundRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraft.world.BossInfo.Color;

public class EntityExperiencedCori extends EntityDivineFlyingMob {
	private BossInfoServer bossInfo = (BossInfoServer) (new BossInfoServer(this.getDisplayName(), BossInfo.Color.BLUE,
            BossInfo.Overlay.PROGRESS));
    private int deathTicks;
    public EntityExperiencedCori(World worldIn) {
        super(worldIn);
        this.setSize(4F, 6.8F);
        this.experienceValue = 2000;
    }
    
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
    }    
    
    @Override
    protected AIDivineFireballAttack createShootAI() {
        return new AIDivineFireballAttack(this,
                new ILaunchThrowable() {

                    @Override
                    public float getInaccuracy(World world) {
                        return 0;
                    }

                    @Override
                    public EntityThrowable createThowable(World world, EntityLivingBase parent, double x, double y, double z) {
                        return new EntityCoriShot(world, parent, (float) parent.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
                    }
                },
                SoundRegistry.CORI_SHOOT);
    }

    @Override
    public float getEyeHeight() {
        return 3.8F;
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 1;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.CORI_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundRegistry.CORI_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.CORI_HURT;
    }

    @Override
    protected ResourceLocation getLootTable() {
        return LootTableRegistry.ENTITIES_EXPERIENCED_CORI;
    }
    

    @Override
    public boolean canDespawn() {
        return false;
    }

    @Override
    public boolean isNonBoss() {
        return false;
    }

    public Color getBarColor() {
        return Color.WHITE;
    }

    @Override
    public void addTrackingPlayer(EntityPlayerMP player) {
        super.addTrackingPlayer(player);
        bossInfo.setColor(getBarColor());
        this.bossInfo.addPlayer(player);
    }

    @Override
    public void removeTrackingPlayer(EntityPlayerMP player) {
        super.removeTrackingPlayer(player);
        this.bossInfo.removePlayer(player);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
    }
    
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.isEntityAlive() && this.rand.nextInt(1000) < this.livingSoundTime++)
        {
            this.playLivingSound();
            if(!this.world.isRemote) {
                EntityWeakCori weak = new EntityWeakCori(world);
                EntityAdvancedCori advanced = new EntityAdvancedCori(world);
                weak.setLocationAndAngles(this.posX + rand.nextInt(8), this.posY, this.posZ + rand.nextInt(8), this.rotationYaw, this.rotationPitch);
                advanced.setLocationAndAngles(this.posX + rand.nextInt(4), this.posY, this.posZ + rand.nextInt(4), this.rotationYaw, this.rotationPitch);
                if (rand.nextInt(10) == 1) {
                    world.spawnEntity(weak);
                }
                if (rand.nextInt(20) == 1) {
                    world.spawnEntity(advanced);
                }
            }
        }
    }
}