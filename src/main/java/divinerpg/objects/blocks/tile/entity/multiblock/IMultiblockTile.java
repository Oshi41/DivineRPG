package divinerpg.objects.blocks.tile.entity.multiblock;

import divinerpg.utils.multiblock.StructureMatch;
import divinerpg.utils.multiblock.StructurePattern;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IMultiblockTile {

    /**
     * Current tile structure
     *
     * @return
     */
    StructurePattern getPattern();

    @Nullable
    StructureMatch getMultiblockMatch();

    /**
     * Called when part of structure is breaking
     */
    void onDestroy(@Nonnull StructureMatch match);

    /**
     * Called after multiblock tile
     *
     * @return was built
     */
    void onBuilt(@Nonnull StructureMatch match);

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

    /**
     * Is multi structure constructed
     *
     * @return
     */
    boolean isConstructed();
}
