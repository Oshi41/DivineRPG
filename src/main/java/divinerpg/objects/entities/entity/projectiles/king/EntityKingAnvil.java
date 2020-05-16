package divinerpg.objects.entities.entity.projectiles.king;

import divinerpg.objects.entities.assets.render.projectiles.base.IBlockRender;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class EntityKingAnvil extends EntityFireball implements IBlockRender {
    public EntityKingAnvil(World worldIn) {
        super(worldIn);
    }

    public EntityKingAnvil(World worldIn, EntityLivingBase shooter, double accelX, double accelY, double accelZ) {
        super(worldIn, shooter, accelX, accelY, accelZ);
    }

    @Override
    protected boolean isFireballFiery() {
        return false;
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (result.typeOfHit == RayTraceResult.Type.MISS)
            return;

        if (world.isRemote)
            return;

        setDead();

        if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
            if (result.entityHit instanceof EntityLivingBase) {
                removeItem((EntityLivingBase) result.entityHit);
            }
        }

        if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
            this.world.newExplosion(this,
                    this.posX,
                    this.posY,
                    this.posZ,
                    1,
                    true,
                    net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this));
        }
    }

    private void removeItem(EntityLivingBase entity) {
        Random random = entity.getRNG();

        List<Map.Entry<EntityEquipmentSlot, ItemStack>> items = Arrays.stream(EntityEquipmentSlot.values())
                .collect(Collectors.toMap(x -> x, entity::getItemStackFromSlot))
                .entrySet()
                .stream()
                .filter(x -> !x.getValue().isEmpty())
                .collect(Collectors.toList());

        if (items.isEmpty())
            return;

        Map.Entry<EntityEquipmentSlot, ItemStack> entry = items.get(random.nextInt(items.size()));

        entity.entityDropItem(entry.getValue(), 1);
        entity.setItemStackToSlot(entry.getKey(), ItemStack.EMPTY);

        world.playSound(null,
                entity.getPosition(),
                SoundEvents.BLOCK_METAL_BREAK,
                SoundCategory.PLAYERS,
                5,
                random.nextFloat());

    }

    @Override
    public Block getBlock() {
        return Blocks.ANVIL;
    }
}
