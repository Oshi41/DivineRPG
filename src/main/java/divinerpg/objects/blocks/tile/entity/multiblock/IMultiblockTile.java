package divinerpg.objects.blocks.tile.entity.multiblock;

import divinerpg.utils.multiblock.StructureMatch;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.commons.lang3.text.StrMatcher;

import javax.annotation.Nullable;

public interface IMultiblockTile {
    @Nullable
    StructureMatch getMatch();

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
    default boolean isConstructed() {
        return getMatch() != null;
    }

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
