package divinerpg.items;

import divinerpg.api.DivineAPI;
import divinerpg.api.arcana.IArcana;
import divinerpg.utils.properties.ExtendedItemProperties;
import divinerpg.utils.properties.ISpawnEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class RangedWeaponBased extends ShootableItem {
    private final int duration;
    private final ISpawnEntity spawnBullet;
    private final int consumeCount;
    private final Item ammo;
    private final int arcana;
    private final int delay;
    private final String delayKey = "nextShootTime";
    private final UseAction useAction;

    public RangedWeaponBased(ExtendedItemProperties properties) {
        super(properties.maxStackSize(1));

        duration = Math.max(0, properties.useDuration);
        spawnBullet = properties.spawnBullet;
        consumeCount = properties.count;
        ammo = properties.ammo;
        arcana = properties.arcana;
        delay = properties.delay;
        useAction = duration > 0 ? UseAction.BOW : UseAction.NONE;
    }

    /**
     * Only perform shot, no checks
     *
     * @param world       - world
     * @param player      - actor
     * @param ammoStack   - stack with ammo
     * @param weaponStack - stack with weapon
     * @param hand        - main hand
     * @param speed       - usually 1.5F
     * @param percentage  - loaded power in percentage
     */
    private void performShoot(World world, PlayerEntity player, ItemStack ammoStack, ItemStack weaponStack, Hand hand, float speed, int percentage) {
        // spawn entity only on server side
        if (spawnBullet != null && !world.isRemote) {
            ThrowableEntity bullet = spawnBullet.createEntity(world, player, percentage);
            bullet.shoot(player, player.rotationPitch, player.rotationYaw, 0, speed, 1);
            world.addEntity(bullet);
        }

        if (player.isCreative())
            return;

        // shrink ammo
        if (consumeCount > 0 && ammoStack != null) {
            ammoStack.shrink(consumeCount);
            if (ammoStack.isEmpty()) {
                player.inventory.deleteStack(ammoStack);
            }
        }

        // damaging weapon
        if (weaponStack != null && weaponStack.isDamageable()) {
            weaponStack.damageItem(1, player, playerEntity -> playerEntity.sendBreakAnimation(hand));
        }

        // consuming arcana
        if (arcana > 0) {
            IArcana arcana = DivineAPI.getPlayerArcana(player);
            arcana.consume(this.arcana);
        }

        // set delay
        if (delay > 0 && !weaponStack.isEmpty()) {
            CompoundNBT tag = weaponStack.getTag();
            tag.putLong(delayKey, player.getEntityWorld().getGameTime() + delay);
        }
    }

    /**
     * Perform all checks if we can shoot
     *
     * @param player - player
     * @param weapon - itemstack with weapon
     */
    private boolean canShoot(PlayerEntity player, ItemStack weapon) {
        if (player.isCreative())
            return true;

        // arcana check
        if (arcana > 0) {
            IArcana arcana = DivineAPI.getPlayerArcana(player);
            if (arcana.getArcana() < this.arcana)
                return false;
        }

        // check ammo
        if (ammo != null) {
            ItemStack ammo = player.findAmmo(weapon);
            if (ammo.isEmpty() || ammo.getCount() < this.consumeCount)
                return false;
        }

        // manage with delay
        if (delay > 0) {

            if (!weapon.hasTag()) {
                weapon.setTag(new CompoundNBT());
            }

            CompoundNBT tag = weapon.getTag();

            return tag == null || tag.getLong(delayKey) <= player.getEntityWorld().getGameTime();
        }

        return true;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return duration;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return useAction;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack weaponStack = playerIn.getHeldItem(handIn);

        if (duration > 0) {

            if (canShoot(playerIn, weaponStack)) {
                ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(weaponStack, worldIn, playerIn, handIn, true);
                if (ret != null) return ret;

                playerIn.setActiveHand(handIn);
                return new ActionResult<>(ActionResultType.SUCCESS, weaponStack);
            }

            return new ActionResult<>(ActionResultType.FAIL, weaponStack);
        } else {
            ActionResult<ItemStack> result = super.onItemRightClick(worldIn, playerIn, handIn);

            if (result.getType() != ActionResultType.FAIL) {
                if (canShoot(playerIn, result.getResult())) {
                    performShoot(worldIn, playerIn, playerIn.findAmmo(weaponStack), weaponStack, handIn, 1.5F, 100);
                    result = ActionResult.newResult(ActionResultType.SUCCESS, weaponStack);
                }
            }

            return result;
        }
    }

    @Override
    public Predicate<ItemStack> getInventoryAmmoPredicate() {
        if (ammo == null)
            return o -> true;

        return stack -> ammo.equals(stack.getItem());
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
        PlayerEntity player = (PlayerEntity) entityLiving;
        if (player == null)
            return;

        float speed = BowItem.getArrowVelocity(this.getUseDuration(stack) - timeLeft);
        if (speed > 0.1 && canShoot(player, stack)) {
            float percantage = (float) timeLeft / getUseDuration(stack) * 100;
            performShoot(worldIn, player, player.findAmmo(stack), stack, Hand.MAIN_HAND, speed, (int) percantage);
        }
    }
}