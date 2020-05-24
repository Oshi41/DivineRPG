package divinerpg.objects.blocks.tile.entity.base.rituals;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IRitualDescription extends INBTSerializable<NBTTagCompound> {
    ResourceLocation getId();

    /**
     * Returns binded tile entity
     *
     * @return
     */
    TileEntity getBindedTilEntity();

    /**
     * Gets full description of needed ritual
     *
     * @return
     */
    @SideOnly(Side.CLIENT)
    ITextComponent getDescription();

    /**
     * Is ritual performed
     *
     * @return
     */
    boolean isPerformed();

    /**
     * Set if ritual was performed
     *
     * @return
     */
    void setIsPerformed(boolean isperformed);

    /**
     * Detects if we need to subscribe it wit hMinecraftForge.Event_Bus
     *
     * @return
     */
    default boolean isEventListener() {
        return false;
    }

    /**
     * Should implement this method with current variables:
     * <p>
     * Tag : value
     * <p>
     * "isPerformed" - was ritual performed (bool)
     * "Id" - Unique ID of ritual (String, should convert to ResourceLocation)
     *
     * @return
     */
    @Override
    NBTTagCompound serializeNBT();
}
