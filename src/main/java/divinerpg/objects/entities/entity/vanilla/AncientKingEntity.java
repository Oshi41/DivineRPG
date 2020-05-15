package divinerpg.objects.entities.entity.vanilla;

import divinerpg.config.Config;
import divinerpg.config.MobStatInfo;
import divinerpg.objects.entities.entity.vanilla.dragon.DivineDragonBase;
import divinerpg.objects.entities.entity.vanilla.dragon.PhaseRegistry;
import divinerpg.objects.entities.entity.vanilla.dragon.phase.base.IPhase;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class AncientKingEntity extends DivineDragonBase {
    private final MobStatInfo info;

    public AncientKingEntity(World worldIn) {
        super(worldIn);

        for (int i = 0; i < 2; i++) {
            this.heads.add(new MultiPartEntityPart(this, "head_" + i, 6.0F, 6.0F));
        }

        knockback = 5;
        info = Config.mobStats.get(EntityList.getKey(this));
    }

    public AncientKingEntity(World worldIn, BlockPos pos) {
        this(worldIn);
        setPosition(pos.getX(), pos.getY(), pos.getZ());
        getDataManager().set(GUARD_POSITION, pos);
    }

    @Override
    public BlockPos getHealingPosition() {
        return null;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        int v = getHealthPercentage();

        double moveSpeed = 0.5;
        double attack = info.values.get(SharedMonsterAttributes.ATTACK_DAMAGE);

        if (v < 70) {
            moveSpeed *= 0.3;
            attack *= 1.5;

            heal(0.1F);
        }
        if (v < 50) {
            moveSpeed += 0.3;
            attack *= 2;

            heal(0.3F);
        }
        if (v < 30) {
            moveSpeed += 0.5;
            attack *= 2;

            heal(0.5F);
        }

        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(moveSpeed);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(attack);
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        int percentage = getHealthPercentage();

        if (percentage < 30) {
            Random rand = target.getRNG();

            for (int i = 0; i < 4; i++) {
                BlockPos pos = target.getPosition()
                        .add(rand.nextFloat() - 0.5, rand.nextFloat() - 0.5, rand.nextInt() - 0.5);

                EntityLightningBolt bolt = new EntityLightningBolt(world, pos.getX(), pos.getY(), pos.getZ(), false);

                world.spawnEntity(bolt);
            }
        }

        super.attackEntityWithRangedAttack(target, distanceFactor);
    }

    @Override
    protected Entity createFireball(double accelX, double accelY, double accelZ, double x, double y, double z) {
        int v = getHealthPercentage();

        if (v < 30) {
            // todo
        }

        if (v < 50) {
            // todo
        }

        if (v < 70) {
            EntityWitherSkull skull = new EntityWitherSkull(world, this, accelX, accelY, accelZ);
            skull.setLocationAndAngles(x, y, z, 0, 0);
            return skull;
        }


        return super.createFireball(accelX, accelY, accelZ, x, y, z);
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        return true;
    }

    @Override
    public ResourceLocation fixPhase(DivineDragonBase dragon, IPhase current, ResourceLocation toChange) {

        if (toChange == PhaseRegistry.LANDING)
            return PhaseRegistry.TAKEOFF;

        return super.fixPhase(dragon, current, toChange);
    }

    protected int getHealthPercentage() {
        return (int) (getHealth() / getMaxHealth() * 100);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        getDataManager().set(GUARD_POSITION, BlockPos.fromLong(compound.getLong("center")));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound nbt = super.writeToNBT(compound);
        nbt.setLong("center", getDragonGuardCenter().toLong());
        return nbt;
    }
}
