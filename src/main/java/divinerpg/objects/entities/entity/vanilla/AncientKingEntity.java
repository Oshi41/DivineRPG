package divinerpg.objects.entities.entity.vanilla;

import divinerpg.config.Config;
import divinerpg.config.MobStatInfo;
import divinerpg.objects.entities.entity.projectiles.EntityFractiteShot;
import divinerpg.objects.entities.entity.projectiles.king.EntityKingAnvil;
import divinerpg.objects.entities.entity.projectiles.king.EntityKingRage;
import divinerpg.objects.entities.entity.projectiles.king.EnumKingThrowable;
import divinerpg.objects.entities.entity.vanilla.dragon.DivineDragonBase;
import divinerpg.objects.entities.entity.vanilla.dragon.PhaseRegistry;
import divinerpg.objects.entities.entity.vanilla.dragon.phase.base.IPhase;
import divinerpg.registry.ModArmor;
import divinerpg.utils.LocalizeUtils;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class AncientKingEntity extends DivineDragonBase {
    private final MobStatInfo info;

    public AncientKingEntity(World worldIn) {
        super(worldIn);

        for (int i = 0; i < 2; i++) {
            this.heads.add(new MultiPartEntityPart(this, "head_" + i, 6.0F, 6.0F));
        }

        knockback = 5;
        info = Config.mobStats.get(EntityList.getKey(this));

        info.values.put(SharedMonsterAttributes.MOVEMENT_SPEED, 0.3F);
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

        if (ticksExisted % 20 == 0)
            heal(perSecondAmount(v));

        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE)
                .setBaseValue(getAttack(info.values.get(SharedMonsterAttributes.ATTACK_DAMAGE), v));
    }

    // region tick updates

    /**
     * Gets amount of half heart healed by second.
     *
     * @param healthPercentage
     * @return Amount of half hearts
     */
    private float perSecondAmount(int healthPercentage) {
        if (healthPercentage < 20) {
            return 2;
        }

        if (healthPercentage < 50) {
            return 1.5F;
        }

        if (healthPercentage < 70) {
            return 1;
        }

        return 0;
    }

    /**
     * Gets dragon attack by it's health percentage
     *
     * @param baseValue        - basic value from config
     * @param healthPercentage - current health percentage
     * @return
     */
    private double getAttack(double baseValue, int healthPercentage) {
        if (healthPercentage < 20) {
            return baseValue * 2;
        }

        if (healthPercentage < 50) {
            return baseValue * 1.5;
        }

        if (healthPercentage < 70) {
            return baseValue * 1.2;
        }

        return baseValue;
    }

    // endregion

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);

        if (world.isRemote)
            return;

        BlockPos center = getDragonGuardCenter();
        world.setBlockState(center, Blocks.CHEST.getDefaultState());
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(center);

        chest.setInventorySlotContents(0, ModArmor.king_helmet.getDefaultInstance());
        chest.setInventorySlotContents(1, ModArmor.king_chestplate.getDefaultInstance());
        chest.setInventorySlotContents(2, ModArmor.king_leggings.getDefaultInstance());
        chest.setInventorySlotContents(3, ModArmor.king_boots.getDefaultInstance());
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
    protected void createAndSpawnFireball(EntityLivingBase target, double accelX, double accelY, double accelZ, double x, double y, double z) {

        switch (getFireballType(getHealthPercentage())) {
            case RAGE:
                EntityKingRage rage = new EntityKingRage(world, this);
                rage.shoot(target.posX, target.posY, target.posZ, 3, 0);
                world.spawnEntity(rage);
                return;

            case ANVIL:
                EntityKingAnvil anvil = new EntityKingAnvil(world, this, accelX, accelY, accelZ);
                anvil.setLocationAndAngles(x, y, z, 0, 0);
                world.spawnEntity(anvil);
                return;

            case WITHER:
                EntityWitherSkull skull = new EntityWitherSkull(world, this, accelX, accelY, accelZ);
                skull.setLocationAndAngles(x, y, z, 0, 0);
                world.spawnEntity(skull);
                return;

            case FRACTILE:
                EntityFractiteShot fractileShot = new EntityFractiteShot(world, this, accelX, accelY, accelZ);
                fractileShot.setLocationAndAngles(x, y, z, 0, 0);
                world.spawnEntity(fractileShot);
                return;

            default:
                super.createAndSpawnFireball(target, accelX, accelY, accelZ, x, y, z);
        }
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        return true;
    }

    @Override
    public ResourceLocation fixPhase(DivineDragonBase dragon, IPhase current, ResourceLocation toChange) {

        if (toChange == PhaseRegistry.CHARGING_PLAYER || toChange == PhaseRegistry.SITTING_ATTACKING)
            return PhaseRegistry.HOVER;

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

    /**
     * Randomly select type of fireball
     *
     * @param percentage
     * @return
     */
    private EnumKingThrowable getFireballType(int percentage) {
        // searching for possible fireball types
        List<EnumKingThrowable> possibleTypes = Arrays.stream(EnumKingThrowable.values())
                .filter(x -> x.maxHealthPercantage >= percentage).collect(Collectors.toList());

        // gets all weights for all possible fireball types
        Integer weights = possibleTypes.stream().map(x -> x.weight).reduce(Integer::sum).orElse(0) + 1;

        // select randomly
        int selected = getRNG().nextInt(weights);

        // lowest iclusive edge
        int current = 0;

        // iterating through all types
        for (int i = 0; i < possibleTypes.size(); i++) {
            EnumKingThrowable type = possibleTypes.get(i);

            // if in range
            if (current <= selected && selected < current + type.weight) {
                return type;
            }

            // increasing lowest edge
            current += type.weight;
        }

        // default if fireball
        return EnumKingThrowable.FIREBALL;
    }
}
