package divinerpg.objects.blocks.tile.entity.multiblock;

import net.minecraft.entity.player.EntityPlayer;

public interface IMultiblockTile {
    /**
     * Called when part of structure is breaking
     */
    void onDestroy();

    /**
     * Check and build structure
     *
     * @return was built
     */
    boolean checkAndBuild();

    /**
     * Is multi structure constructed
     *
     * @return
     */
    boolean isConstructed();

    /**
     * Rechecking structure when blocks were changed
     */
    void recheckStructure();

    /**
     * When player was clicked on tile/structure
     *
     * @param player
     */
    void click(EntityPlayer player);
}
