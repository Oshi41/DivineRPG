package divinerpg.objects.blocks.tile.entity.multiblock;

import divinerpg.DivineRPG;
import divinerpg.objects.blocks.tile.entity.base.ModUpdatableTileEntity;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IInteractionObject;

public abstract class TileEntityDivineMultiblock extends ModUpdatableTileEntity implements IMultiblockTile, IInteractionObject {
    private IMultiStructure structure;
    private String name;
    private Integer guiId;

    /**
     * Top left conrenr of structure
     */
    protected BlockPos topLeft;
    protected BlockPos bottomRight;
    protected EnumFacing finger;
    protected EnumFacing thumb;

    private boolean isWorking = false;

    /**
     * Multiblock tile entity
     *
     * @param structure - structure description
     * @param name      - name of tile
     * @param guiId     - possible GUI ID. Pass null o disable interaction
     */
    public TileEntityDivineMultiblock(IMultiStructure structure, String name, Integer guiId) {
        this.structure = structure;
        this.name = name;
        this.guiId = guiId;

        onDestroy();
    }

    @Override
    public void onDestroy() {
        isWorking = true;

        if (isConstructed()) {
            structure.constructStructure(world, topLeft, finger, thumb, true);
        }

        finger = EnumFacing.NORTH;
        thumb = EnumFacing.NORTH;
        topLeft = BlockPos.ORIGIN.down();

        isWorking = false;
    }

    @Override
    public boolean checkAndBuild() {
        isWorking = true;

        try {
            BlockPattern.PatternHelper match = structure.isMatch(world, getPos());
            if (match == null)
                return false;

            if (!world.isRemote) {
                structure.constructStructure(world, match);
            }

            topLeft = match.getFrontTopLeft();
            finger = match.getForwards();
            thumb = match.getUp();
            bottomRight = topLeft
                    .offset(match.getForwards(), match.getWidth())
                    .offset(match.getUp(), match.getHeight());
            return true;
        } finally {
            isWorking = false;
        }
    }

    @Override
    public boolean isConstructed() {
        if (topLeft == null || world.isOutsideBuildHeight(topLeft))
            return false;

        return bottomRight != null && !world.isOutsideBuildHeight(bottomRight);
    }

    @Override
    public void recheckStructure() {
        if (isWorking)
            return;

        if (isConstructed()) {
            if (structure.isMatch(world, getPos()) == null)
                onDestroy();
        } else {
            checkAndBuild();
        }
    }

    @Override
    public void click(EntityPlayer player) {
        if (!isConstructed())
            return;

        BlockPos pos = getPos();

        if (!world.isRemote && guiId != null) {
            player.openGui(DivineRPG.instance, guiId, world, pos.getX(), pos.getY(), pos.getZ());
        }
    }

    // region NBT

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        topLeft = BlockPos.fromLong(compound.getLong("topLeft"));
        bottomRight = BlockPos.fromLong(compound.getLong("bottomRight"));
        finger = EnumFacing.byName(compound.getString("finger"));
        thumb = EnumFacing.byName(compound.getString("thumb"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound result = super.writeToNBT(compound);

        result.setLong("topLeft", topLeft.toLong());
        result.setLong("bottomRight", bottomRight.toLong());
        result.setString("finger", finger.getName());
        result.setString("thumb", thumb.getName());

        return result;
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

    //endregion
}
