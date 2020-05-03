package divinerpg.objects.blocks.tile.entity.pillar;

import divinerpg.objects.blocks.tile.entity.base.ModUpdatableTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class TileEntityPillar extends ModUpdatableTileEntity {
    private ItemStackHandler inventory;

    public TileEntityPillar() {
        inventory = new DivineStackHandler(1, integer -> markDirty(), this::isSlotValid);
    }

    protected boolean isSlotValid(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        inventory.deserializeNBT(compound.getCompoundTag("inventory"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound result = super.writeToNBT(compound);
        result.setTag("inventory", inventory.serializeNBT());
        return result;
    }

    public boolean hasCapability(@Nonnull Capability<?> cap, EnumFacing side) {
        return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(cap, side);
    }

    public <T> T getCapability(@Nonnull Capability<T> cap, EnumFacing side) {
        return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.inventory)
                : super.getCapability(cap, side);
    }

    public IItemHandlerModifiable getInventory() {
        return inventory;
    }
}
