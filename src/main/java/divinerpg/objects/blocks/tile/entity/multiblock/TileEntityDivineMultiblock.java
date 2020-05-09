package divinerpg.objects.blocks.tile.entity.multiblock;

import divinerpg.DivineRPG;
import divinerpg.objects.blocks.tile.entity.base.ModUpdatableTileEntity;
import divinerpg.utils.multiblock.StructureMatch;
import divinerpg.utils.multiblock.StructurePattern;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IInteractionObject;
import org.apache.commons.lang3.text.StrMatcher;

public abstract class TileEntityDivineMultiblock extends ModUpdatableTileEntity implements IMultiblockTile, IInteractionObject {
    private final StructurePattern structure;
    private String name;
    private Integer guiId;

    /**
     * Current Structure match
     */
    protected StructureMatch match;

    private boolean isWorking = false;

    /**
     * Multiblock tile entity
     *
     * @param structure - structure description
     * @param name      - name of tile
     * @param guiId     - possible GUI ID. Pass null o disable interaction
     */
    public TileEntityDivineMultiblock(StructurePattern structure, String name, Integer guiId) {
        this.structure = structure;
        this.name = name;
        this.guiId = guiId;

        onDestroy();
    }

    @Override
    public void onDestroy() {
        isWorking = true;

        if (isConstructed()) {
            match.destroy(world);
            match = null;
        }

        isWorking = false;
    }

    @Override
    public boolean checkAndBuild() {
        isWorking = true;

        try {
            match = structure.checkStructure(world, getPos());

            if (isConstructed()) {

                if (!world.isRemote)
                    match.buildStructure(world);

                return isConstructed();
            }

        } finally {
            isWorking = false;
        }

        return false;
    }

    @Override
    public boolean isConstructed() {
        return match != null;
    }

    @Override
    public void recheckStructure() {
        if (isWorking)
            return;

        if (isConstructed()) {
            StructureMatch newMatch = structure.recheck(world, match);

            if (newMatch == null) {
                onDestroy();
            } else {
                match = newMatch;
            }

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
