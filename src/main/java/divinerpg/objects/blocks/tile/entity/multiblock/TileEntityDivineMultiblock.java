package divinerpg.objects.blocks.tile.entity.multiblock;

import divinerpg.DivineRPG;
import divinerpg.objects.blocks.tile.entity.base.ModUpdatableTileEntity;
import divinerpg.utils.multiblock.StructureMatch;
import divinerpg.utils.multiblock.StructurePattern;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IInteractionObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TileEntityDivineMultiblock extends ModUpdatableTileEntity implements IMultiblockTile, IInteractionObject {

    private final StructurePattern pattern;
    private final String name;
    private final Integer guiId;

    private StructureMatch multiblockMatch;
    private boolean constructed;

    private boolean working;

    public TileEntityDivineMultiblock(StructurePattern pattern, String name, Integer guiId) {
        this.pattern = pattern;
        this.name = name;
        this.guiId = guiId;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        recheckStructure();
    }

    @Override
    public StructurePattern getPattern() {
        return pattern;
    }

    @Nullable
    @Override
    public StructureMatch getMultiblockMatch() {
        return multiblockMatch;
    }

    @Override
    public void onDestroy(@Nonnull StructureMatch match) {
        match.destroy(world);
    }

    @Override
    public void onBuilt(@Nonnull StructureMatch match) {
        match.buildStructure(world);
    }

    @Override
    public void recheckStructure() {
        if (world.isRemote || working)
            return;

        working = true;

        StructureMatch multiMatch = getPattern().checkMultiblock(world, getPos());
        if (multiMatch != null) {
            onBuilt(multiMatch);
        } else {
            if (getMultiblockMatch() != null) {
                onDestroy(getMultiblockMatch());
            }
        }

        multiblockMatch = multiMatch;
        constructed = getMultiblockMatch() != null;
        working = false;
    }

    @Override
    public void click(EntityPlayer player) {
        if (guiId == null || player.getEntityWorld().isRemote)
            return;

        BlockPos pos = getPos();
        player.openGui(DivineRPG.instance, guiId, player.world, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean isConstructed() {
        return constructed;
    }

    @Override
    public String getGuiID() {
        return TileEntity.getKey(getClass()).toString();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        constructed = compound.getBoolean("constructed");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound nbt = super.writeToNBT(compound);
        nbt.setBoolean("constructed", constructed);
        return nbt;
    }
}
