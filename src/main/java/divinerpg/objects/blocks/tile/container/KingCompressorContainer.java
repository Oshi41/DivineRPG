package divinerpg.objects.blocks.tile.container;

import divinerpg.objects.blocks.tile.entity.TileEntityKingCompressor;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;

public class KingCompressorContainer extends Container {
    private final TileEntityKingCompressor tile;
    private final int lastInvIndex;

    public KingCompressorContainer(InventoryPlayer inventoryPlayer, TileEntityKingCompressor tile) {
        this.tile = tile;

        Iterator<EntityEquipmentSlot> iterator = Arrays.asList(
                EntityEquipmentSlot.HEAD,
                EntityEquipmentSlot.CHEST,
                EntityEquipmentSlot.LEGS,
                EntityEquipmentSlot.FEET,
                EntityEquipmentSlot.MAINHAND,
                EntityEquipmentSlot.OFFHAND
        ).iterator();

        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 2; i++) {
                int x = 8 + i * 60;
                int y = 8 + j * 22;

                EntityEquipmentSlot slot = iterator.next();

                addSlotToContainer(new Slot(tile, slot.getSlotIndex(), x, y) {
                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        if (!tile.isItemValidForSlot(this.getSlotIndex(), stack))
                            return false;

                        EntityEquipmentSlot forSlot = EntityLiving.getSlotForItemStack(stack);

                        if (forSlot == EntityEquipmentSlot.MAINHAND) {
                            return stack.getItem().isDamageable();
                        }

                        return forSlot == slot;
                    }

                    @Nullable
                    @Override
                    public String getSlotTexture() {

                        switch (slot) {
                            case OFFHAND:
                                return "minecraft:items/empty_armor_slot_shield";

                            case MAINHAND:
                                return super.getSlotTexture();

                            default:
                                return ItemArmor.EMPTY_SLOT_NAMES[slot.getIndex()];
                        }
                    }
                });
            }
        }

        addSlotToContainer(new Slot(tile, 6, 37, 51) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return tile.isItemValidForSlot(this.getSlotIndex(), stack);
            }
        });

        this.addSlotToContainer(new SlotFurnaceOutput(inventoryPlayer.player, tile, 7, 143 + 5, 43 + 5));

        lastInvIndex = tile.getSizeInventory() - 1;

        //
        // Player inv
        //

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(inventoryPlayer, k, 8 + k * 18, 142));
        }
    }

    /**
     * Gets tile reference
     *
     * @return
     */
    public TileEntityKingCompressor getTile() {
        return tile;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return tile.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemStack1 = slot.getStack();
            itemStack = itemStack1.copy();

            if (index < this.lastInvIndex) {
                if (!this.mergeItemStack(itemStack1, lastInvIndex + 1, this.inventorySlots.size() - 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemStack1, 0, lastInvIndex, false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }
        return itemStack;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendAllWindowProperties(this, tile);
    }
}
