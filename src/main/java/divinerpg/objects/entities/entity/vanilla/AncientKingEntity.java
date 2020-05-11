package divinerpg.objects.entities.entity.vanilla;

import divinerpg.objects.entities.entity.EntityDivineRPGBoss;
import divinerpg.objects.entities.entity.EntityDivineRPGMob;
import divinerpg.registry.ModArmor;
import divinerpg.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class AncientKingEntity extends EntityDivineRPGBoss {
    private BlockPos chestLootPos;

    public AncientKingEntity(World par1World) {
        super(par1World);
    }

    public AncientKingEntity(World par1World, BlockPos pos) {
        this(par1World);
        chestLootPos = pos;
        setPosition(pos.getX(), pos.getY(), pos.getZ());
        this.setSize(16.0F, 8.0F);
    }

    @Override
    protected void initEntityAI() {
        addBasicAI();
        this.targetTasks.addTask(1, new EntityAIFindEntityNearestPlayer(this));
    }

    @Override
    public BossInfo.Color getBarColor() {
        return BossInfo.Color.PURPLE;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        Vec3d targetPos = null;

        EntityLivingBase target = getAttackTarget();
        if (target != null) {
            targetPos = new Vec3d(target.getPosition());
        }

        if (targetPos != null) {
            double d6 = targetPos.x - this.posX;
            double d7 = targetPos.y - this.posY;
            double d8 = targetPos.z - this.posZ;
            double d3 = d6 * d6 + d7 * d7 + d8 * d8;
            float f5 = getMaxRiseOrFall();
            d7 = MathHelper.clamp(d7 / (double) MathHelper.sqrt(d6 * d6 + d8 * d8), -f5, f5);
            this.motionY += d7 * 0.10000000149011612D;
            this.rotationYaw = MathHelper.wrapDegrees(this.rotationYaw);
            double d4 = MathHelper.clamp(MathHelper.wrapDegrees(180.0D - MathHelper.atan2(d6, d8) * (180D / Math.PI) - (double) this.rotationYaw), -50.0D, 50.0D);
            Vec3d vec3d1 = (new Vec3d(targetPos.x - this.posX, targetPos.y - this.posY, targetPos.z - this.posZ)).normalize();
            Vec3d vec3d2 = (new Vec3d(MathHelper.sin(this.rotationYaw * 0.017453292F), this.motionY, -MathHelper.cos(this.rotationYaw * 0.017453292F))).normalize();
            float f7 = Math.max(((float) vec3d2.dotProduct(vec3d1) + 0.5F) / 1.5F, 0.0F);
            this.randomYawVelocity *= 0.8F;
            this.randomYawVelocity = (float) ((double) this.randomYawVelocity + d4 * getYawFactor());
            this.rotationYaw += this.randomYawVelocity * 0.1F;
            float f8 = (float) (2.0D / (d3 + 1.0D));
            float f9 = 0.06F;
            this.moveRelative(0.0F, 0.0F, -1.0F, 0.06F * (f7 * f8 + (1.0F - f8)));

            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

            Vec3d vec3d3 = (new Vec3d(this.motionX, this.motionY, this.motionZ)).normalize();
            float f10 = ((float) vec3d3.dotProduct(vec3d2) + 1.0F) / 2.0F;
            f10 = 0.8F + 0.15F * f10;
            this.motionX *= f10;
            this.motionZ *= f10;
            this.motionY *= 0.9100000262260437D;
        }
    }

    private double getYawFactor() {
        float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ) + 1.0F;
        float f1 = Math.min(f, 40.0F);
        return 0.7F / f1 / f;
    }

    /**
     * Returns the maximum amount dragon may rise or fall during this phase
     */
    private float getMaxRiseOrFall() {
        return 0.6F;
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);

        if (!world.isRemote)
            return;


        world.setBlockState(chestLootPos, Blocks.CHEST.getDefaultState());
        IInventory entity = (IInventory) world.getTileEntity(chestLootPos);
        List<Item> toDrop = Arrays.asList(ModArmor.king_helmet, ModArmor.king_chestplate, ModArmor.king_leggings, ModArmor.king_boots);

        for (int i = 0; i < toDrop.size(); i++) {
            entity.setInventorySlotContents(i, new ItemStack(toDrop.get(i)));
        }
    }

    // region NBT

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        chestLootPos = BlockPos.fromLong(compound.getLong("chestLootPos"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound res = super.writeToNBT(compound);

        if (chestLootPos != null)
            res.setLong("chestLootPos", chestLootPos.toLong());

        return res;
    }

    // endregion
}
