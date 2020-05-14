package divinerpg.objects.entities.ai;

import divinerpg.objects.entities.entity.projectiles.EntityFractiteShot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;
import java.util.Random;

public class AIDivineKingAttack extends EntityAIBase {
    private final EntityLiving parentEntity;
    private final ILaunchFireBall largeFireball;
    private final ILaunchFireBall witherFireball;
    private final ILaunchFireBall fracticeFireball;
    private int ticks;

    public AIDivineKingAttack(EntityLiving parentEntity) {
        this.parentEntity = parentEntity;
        setMutexBits(1);

        largeFireball = (world, parent, x, y, z, fireballStrength) -> new EntityLargeFireball(world);
        witherFireball = (world, parent, x, y, z, fireballStrength) -> new EntityWitherSkull(world);
        fracticeFireball = (world, parent, x, y, z, fireballStrength) -> new EntityFractiteShot(world);
    }

    @Override
    public boolean shouldExecute() {
        return parentEntity.getAttackTarget() != null;
    }

    @Override
    public void startExecuting() {
        ticks = 0;
    }

    @Override
    public void updateTask() {
        super.updateTask();

        ticks++;

        EntityLivingBase victim = parentEntity.getAttackTarget();
        if (victim != null)
            return;

        if (ticks % (20 * 60) == 0) {
            summonLightning(victim);
        }

        if (ticks % (20 * 40) == 0) {
            spawnFireball(victim, fracticeFireball);
        }

        if (ticks % (20 * 30) == 0) {
            spawnFireball(victim, witherFireball);
        }

        if (ticks % (20 * 10) == 0) {
            spawnFireball(victim, largeFireball);
        }
    }

    private void summonLightning(EntityLivingBase victim) {
        World world = victim.world;
        EntityLightningBolt bolt = new EntityLightningBolt(world, victim.posX, victim.posY, victim.posZ, false);
        world.spawnEntity(bolt);
    }

    private void spawnFireball(EntityLivingBase victim, ILaunchFireBall fireballFunc) {
        for (int i = 0; i < 4; i++) {
            Random rand = victim.world.rand;
            Entity fireball = fireballFunc.createFireball(parentEntity, victim);

            fireball.posX += rand.nextDouble() - rand.nextDouble() * 2;
            fireball.posY += rand.nextDouble() - rand.nextDouble() * 2;
            fireball.posZ += rand.nextDouble() - rand.nextDouble() * 2;

            victim.world.spawnEntity(fireball);
        }
    }
}
