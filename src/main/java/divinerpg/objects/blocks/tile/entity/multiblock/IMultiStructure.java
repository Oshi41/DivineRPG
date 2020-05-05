package divinerpg.objects.blocks.tile.entity.multiblock;

import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IMultiStructure {
    /**
     * Checks if multistructure is created
     *
     * @param world - in current world
     * @param pos   - by that pos
     * @return
     */
    @Nullable
    BlockPattern.PatternHelper isMatch(World world, BlockPos pos);

    /**
     * Constructing multistructure
     *
     * @param world        - in that world
     * @param isDestucting - is destructing the structure
     */
    void constructStructure(World world, BlockPos topLeft, EnumFacing finger, EnumFacing thumb, boolean isDestucting);

    /**
     * Creating structure
     *
     * @param world
     * @param pos
     * @return
     */
    default boolean createStructure(World world, BlockPos pos) {
        BlockPattern.PatternHelper helper = isMatch(world, pos);
        if (helper != null) {
            constructStructure(world, helper.getFrontTopLeft(), helper.getForwards(), helper.getUp(), false);
            return true;
        }

        return false;
    }

    /**
     * Creating structure from match
     *
     * @param world
     * @param match
     */
    default void constructStructure(World world, @Nonnull BlockPattern.PatternHelper match) {
        constructStructure(world, match.getFrontTopLeft(), match.getForwards(), match.getUp(), false);
    }
}
