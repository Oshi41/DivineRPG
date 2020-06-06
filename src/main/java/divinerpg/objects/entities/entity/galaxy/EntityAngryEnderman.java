package divinerpg.objects.entities.entity.galaxy;

import divinerpg.api.DivineAPI;
import divinerpg.api.armor.cap.IArmorPowers;
import divinerpg.config.Config;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EntityAngryEnderman extends EntityEnderman {
    private static final Set<Item> helmets = new HashSet<Item>() {{
        // todo
        add(ItemBlock.getItemFromBlock(Blocks.PUMPKIN));
    }};

    public EntityAngryEnderman(World worldIn) {
        super(worldIn);

        Arrays.fill(inventoryHandsDropChances, 0);
        setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Blocks.GRASS));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();

        Config.initEntityAttributes(this);
    }

    @Override
    public void setHeldBlockState(@Nullable IBlockState state) {
        super.setHeldBlockState(state);

        ItemStack stack = ItemStack.EMPTY;

        if (state != null) {
            Item item = Item.getItemFromBlock(state.getBlock());
            int i = item.getHasSubtypes()
                    ? state.getBlock().getMetaFromState(state)
                    : 0;
            stack = new ItemStack(item, 1, i);
        }

        setItemStackToSlot(EntityEquipmentSlot.MAINHAND, stack);
    }

    @Override
    protected void initEntityAI() {
        super.initEntityAI();

        this.targetTasks.addTask(1, new EntityAngryEnderman.AIFindPlayer(this));
    }

    /**
     * Checks to see if this enderman should be attacking this player
     */
    private boolean shouldAttackPlayer(EntityPlayer e) {
        IArmorPowers powers = DivineAPI.getArmorPowers(e);
        if (powers != null) {
            Set<Item> items = powers.currentItems(EntityEquipmentSlot.HEAD);
            return items.isEmpty()
                    || helmets.isEmpty()
                    || helmets.stream().noneMatch(items::contains);
        }

        return true;
    }

    static class AIFindPlayer extends EntityAINearestAttackableTarget<EntityPlayer> {
        private final EntityAngryEnderman enderman;
        /**
         * The player
         */
        private EntityPlayer player;
        private int aggroTime;
        private int teleportTime;

        public AIFindPlayer(EntityAngryEnderman enderman) {
            super(enderman, EntityPlayer.class, false);
            this.enderman = enderman;
        }

        /**
         * Returns whether the EntityAIBase should begin execution.
         */
        public boolean shouldExecute() {
            double d0 = this.getTargetDistance();
            this.player = this.enderman.world.getNearestAttackablePlayer(this.enderman.posX, this.enderman.posY, this.enderman.posZ, d0, d0, null,
                    player -> player != null && enderman.shouldAttackPlayer(player));
            return this.player != null;
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void startExecuting() {
            this.aggroTime = 5;
            this.teleportTime = 0;
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void resetTask() {
            this.player = null;
            super.resetTask();
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean shouldContinueExecuting() {
            if (this.player != null) {
                if (!this.enderman.shouldAttackPlayer(this.player)) {
                    return false;
                } else {
                    this.enderman.faceEntity(this.player, 10.0F, 10.0F);
                    return true;
                }
            } else {
                return this.targetEntity != null && this.targetEntity.isEntityAlive() || super.shouldContinueExecuting();
            }
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void updateTask() {
            if (this.player != null) {
                if (--this.aggroTime <= 0) {
                    this.targetEntity = this.player;
                    this.player = null;
                    super.startExecuting();
                }
            } else {
                if (this.targetEntity != null) {
                    if (this.enderman.shouldAttackPlayer(this.targetEntity)) {
                        if (this.targetEntity.getDistanceSq(this.enderman) < 16.0D) {
                            this.enderman.teleportRandomly();
                        }

                        this.teleportTime = 0;
                    } else if (this.targetEntity.getDistanceSq(this.enderman) > 256.0D && this.teleportTime++ >= 30) {
                        if (this.enderman.teleportToEntity(this.targetEntity)) {
                            this.teleportTime = 0;
                        }
                    }
                }

                super.updateTask();
            }
        }
    }
}
