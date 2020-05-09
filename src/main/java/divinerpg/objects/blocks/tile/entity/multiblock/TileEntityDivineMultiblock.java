package divinerpg.objects.blocks.tile.entity.multiblock;

import divinerpg.DivineRPG;
import divinerpg.objects.blocks.tile.entity.base.ModUpdatableTileEntity;
import divinerpg.utils.multiblock.StructureMatch;
import divinerpg.utils.multiblock.StructurePattern;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IInteractionObject;

import javax.annotation.Nullable;

public abstract class TileEntityDivineMultiblock extends ModUpdatableTileEntity implements IMultiblockTile, IInteractionObject {
    private final StructurePattern structure;
    private String name;
    private Integer guiId;

    private StructureMatch match;

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

    /**
     * Current Structure match
     */
    @Nullable
    @Override
    public StructureMatch getMatch() {
        return match;
    }

    @Override
    public void onDestroy() {
        isWorking = true;

        if (isConstructed()) {
            getMatch().destroy(world);
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
                    getMatch().buildStructure(world);

                return isConstructed();
            }

        } finally {
            isWorking = false;
        }

        return false;
    }

    @Override
    public void recheckStructure() {
        if (isWorking)
            return;

        // was not constructed
        if (match == null) {
            match = structure.checkStructure(world, pos);

            if (match != null) {
                if (!world.isRemote) {
                    match.buildStructure(world);
                }
            }
        } else {
            StructureMatch multi = structure.checkMultiblock(world, pos);
            if (multi == null) {

                if (!world.isRemote) {
                    match.destroy(world);
                }

                match = null;
            }
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
