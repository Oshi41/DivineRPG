package divinerpg.utils.multiblock;

import com.google.common.base.Predicate;
import com.google.common.cache.LoadingCache;
import divinerpg.objects.blocks.tile.entity.multiblock.IMultiblockTile;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNullableByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StructureMatch {
    /**
     * Area with structure blocks
     */
    public final AxisAlignedBB area;
    private boolean constructed;
    public final EnumFacing up;
    public final EnumFacing forwards;

    private final Map<BlockPos, IBlockState> structure;
    private final Map<BlockPos, Predicate<BlockWorldState>> structurePredicates;
    private final Map<BlockPos, IBlockState> buildedStructure;
    private final Map<BlockPos, Predicate<BlockWorldState>> builtStructurePredicates;

    public StructureMatch(AxisAlignedBB area,
                          Map<BlockPos, IBlockState> structure,
                          Map<BlockPos, Predicate<BlockWorldState>> structurePredicates,
                          Map<BlockPos, IBlockState> builtStructure,
                          Map<BlockPos, Predicate<BlockWorldState>> builtStructurePredicates,
                          boolean constructed,
                          EnumFacing finger,
                          EnumFacing thumb) {
        this.area = area;
        this.structure = structure;
        this.structurePredicates = structurePredicates;
        this.buildedStructure = builtStructure;
        this.builtStructurePredicates = builtStructurePredicates;
        this.constructed = constructed;
        this.up = thumb;
        this.forwards = finger;
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
        buildStructure(world, builtStructurePredicates, buildedStructure, null);
        constructed = true;
    }

    /**
     * Destroys existing structure
     *
     * @param world
     */
    public void destroy(World world) {
        buildStructure(world, structurePredicates, structure,
                // should destroy all non air blocks
                input -> {
                    IBlockState multiBlockState = buildedStructure.get(input.getPos());

                    return multiBlockState.getBlock() == input.getBlockState().getBlock()
                            &&
                            structure.get(input.getPos()).getMaterial() == Material.AIR;
                });
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
     */
    private void buildStructure(World world, Map<BlockPos, Predicate<BlockWorldState>> predicates, Map<BlockPos, IBlockState> structure, @Nullable final Predicate<BlockWorldState> shouldReplace) {
        LoadingCache<BlockPos, BlockWorldState> cache = BlockPattern.createLoadingCache(world, true);

        List<IMultiblockTile> tiles = new ArrayList<>();

        predicates.forEach((pos, condition) -> {
            boolean notMatch = !condition.test(cache.getUnchecked(pos));
            boolean canReplace = !notMatch && shouldReplace != null && shouldReplace.test(cache.getUnchecked(pos));

            if (notMatch || canReplace) {
                world.setBlockState(pos, structure.get(pos), 2);
            }

            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof IMultiblockTile) {
                tiles.add((IMultiblockTile) tileEntity);
            }
        });

        tiles.forEach(IMultiblockTile::recheckStructure);
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
