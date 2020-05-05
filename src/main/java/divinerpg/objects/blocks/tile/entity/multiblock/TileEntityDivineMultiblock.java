package divinerpg.objects.blocks.tile.entity.multiblock;

import divinerpg.objects.blocks.tile.entity.base.ModUpdatableTileEntity;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class TileEntityDivineMultiblock extends ModUpdatableTileEntity implements IMultiblockTile {
    private IMultiStructure structure;
    private BlockPos topLeft;
    private EnumFacing finger;
    private EnumFacing thumb;

    public TileEntityDivineMultiblock(IMultiStructure structure) {
        this.structure = structure;

        finger = EnumFacing.NORTH;
        thumb = EnumFacing.NORTH;
        topLeft = BlockPos.ORIGIN.down();
    }

    @Override
    public void onDestroy() {
        finger = EnumFacing.NORTH;
        thumb = EnumFacing.NORTH;
        topLeft = BlockPos.ORIGIN.down();

        structure.constructStructure(world, topLeft, finger, thumb, true);
    }

    @Override
    public boolean checkAndBuild() {
        BlockPattern.PatternHelper match = structure.isMatch(world, getPos());
        if (match != null) {
            topLeft = match.getFrontTopLeft();
            finger = match.getForwards();
            thumb = match.getUp();

            if (world.isRemote) {
                structure.constructStructure(world, match);
            }
        }

        return false;
    }

    @Override
    public boolean isConstructed() {
        return !world.isOutsideBuildHeight(topLeft);
    }

    @Override
    public void recheckStructure() {
        if (isConstructed()) {
            if (structure.isMatch(world, getPos()) == null)
                onDestroy();
        } else {
            checkAndBuild();
        }
    }

    // region NBT

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        topLeft = BlockPos.fromLong(compound.getLong("topLeft"));
        finger = EnumFacing.byName(compound.getString("finger"));
        thumb = EnumFacing.byName(compound.getString("thumb"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound result = super.writeToNBT(compound);

        result.setLong("topLeft", topLeft.toLong());
        result.setString("finger", finger.getName());
        result.setString("thumb", thumb.getName());

        return result;
    }

    //endregion
}
