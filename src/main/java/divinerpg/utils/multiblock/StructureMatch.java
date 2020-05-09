package divinerpg.utils.multiblock;

import com.google.common.base.Predicate;
import com.google.common.cache.LoadingCache;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Objects;

public class StructureMatch {
    /**
     * Area with structure blocks
     */
    public final AxisAlignedBB area;
    private boolean constructed;

    private final Map<BlockPos, IBlockState> structure;
    private final Map<BlockPos, Predicate<BlockWorldState>> structurePredicates;
    private final Map<BlockPos, IBlockState> buildedStructure;
    private final Map<BlockPos, Predicate<BlockWorldState>> builtStructurePredicates;

    public StructureMatch(AxisAlignedBB area,
                          Map<BlockPos, IBlockState> structure,
                          Map<BlockPos, Predicate<BlockWorldState>> structurePredicates,
                          Map<BlockPos, IBlockState> builtStructure,
                          Map<BlockPos, Predicate<BlockWorldState>> builtStructurePredicates,
                          boolean constructed) {
        this.area = area;
        this.structure = structure;
        this.structurePredicates = structurePredicates;
        this.buildedStructure = builtStructure;
        this.builtStructurePredicates = builtStructurePredicates;
        this.constructed = constructed;
    }

    /**
     * Rechecking structure from inner state
     *
     * @param world
     */
    public void changeState(World world) {
        if (!isConstructed()) {
            buildStructure(world);
        } else {
            destroy(world);
        }
    }

    /**
     * Builds correct structure
     *
     * @param world
     */
    public void buildStructure(World world) {
        buildStructure(world, builtStructurePredicates, buildedStructure, false);
        constructed = true;
    }

    /**
     * Destroys existing structure
     *
     * @param world
     */
    public void destroy(World world) {
        buildStructure(world, structurePredicates, structure, true);
        constructed = false;
    }

    /**
     * Gets corner of structure
     *
     * @return
     */
    public BlockPos getCorner() {
        return new BlockPos(area.minX, area.minY, area.minZ);
    }

    /**
     * Creates current structure
     *
     * @param world      - world
     * @param predicates - current structure predicates. If block don't match a condition, replacing it with structure block
     * @param structure  - structure blocks by position
     * @param ignoreAir  - ignore replacing if there is an air block
     */
    private void buildStructure(World world, Map<BlockPos, Predicate<BlockWorldState>> predicates, Map<BlockPos, IBlockState> structure, boolean ignoreAir) {
        LoadingCache<BlockPos, BlockWorldState> cache = BlockPattern.createLoadingCache(world, true);

        predicates.forEach((pos, condition) -> {
            if (!condition.test(cache.getUnchecked(pos))) {

                if (ignoreAir || !world.isAirBlock(pos))
                    world.setBlockState(pos, structure.get(pos), 2);
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StructureMatch)) return false;
        StructureMatch that = (StructureMatch) o;
        return Objects.equals(area, that.area);
    }

    @Override
    public int hashCode() {
        return Objects.hash(area);
    }

    /**
     * Is fully constructed
     */
    public boolean isConstructed() {
        return constructed;
    }
}
