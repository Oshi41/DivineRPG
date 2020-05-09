package divinerpg.utils.multiblock;

import com.google.common.base.Predicate;
import com.google.common.cache.LoadingCache;
import divinerpg.utils.PositionHelper;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

public class StructurePattern {
    private final int fingerLength;
    private final int thumbLength;
    private final int palmLength;

    private final Set<Predicate<BlockWorldState>> structureBlocks = new HashSet<>();
    private final Set<Predicate<BlockWorldState>> buildedStructureBlocks = new HashSet<>();

    private final Predicate<BlockWorldState>[][][] structurePredicates;
    private final IBlockState[][][] structure;
    private final Predicate<BlockWorldState>[][][] buildedStructurePredicates;
    private final IBlockState[][][] buildedStructure;

    public StructurePattern(Predicate<BlockWorldState>[][][] structurePattern,
                            IBlockState[][][] structure,
                            Predicate<BlockWorldState>[][][] buildedStructurePattern,
                            IBlockState[][][] buildedStructure) {
        this.structure = structure;
        this.buildedStructure = buildedStructure;
        this.structurePredicates = structurePattern;
        this.buildedStructurePredicates = buildedStructurePattern;

        Arrays.stream(structurePattern).forEach(x1 -> Arrays.stream(x1).forEach(x2 -> structureBlocks.addAll(Arrays.asList(x2))));
        Arrays.stream(buildedStructurePattern).forEach(x1 -> Arrays.stream(x1).forEach(x2 -> buildedStructureBlocks.addAll(Arrays.asList(x2))));

        // only actual checks
        structureBlocks.remove(StructureBuilder.ANY);
        buildedStructureBlocks.remove(StructureBuilder.ANY);

        this.fingerLength = structurePattern.length;

        if (this.fingerLength > 0) {
            this.thumbLength = structurePattern[0].length;

            if (this.thumbLength > 0) {
                this.palmLength = structurePattern[0][0].length;
            } else {
                this.palmLength = 0;
            }
        } else {
            this.thumbLength = 0;
            this.palmLength = 0;
        }
    }

    /**
     * Rechecking structure
     *
     * @param world
     * @param match
     * @return
     */
    public StructureMatch recheck(World world, StructureMatch match) {
        return match.isConstructed()
                ? checkMultiblock(world, match.getCorner())
                : checkStructure(world, match.getCorner());
    }

    /**
     * Check if multiblock is valid still
     *
     * @param world - world
     * @param pos   - one of multiblock structure pos
     * @return
     */
    public StructureMatch checkMultiblock(World world, BlockPos pos) {
        return canBuildStructure(world, pos, buildedStructureBlocks, buildedStructurePredicates);
    }

    /**
     * Check wherever can build multi structure
     *
     * @param world - world
     * @param pos   - one of the structure pos
     * @return
     */
    public StructureMatch checkStructure(World world, BlockPos pos) {
        return canBuildStructure(world, pos, structureBlocks, structurePredicates);
    }

    /**
     * Trying to search structure
     *
     * @param world      - world
     * @param pos        - structure position
     * @param fastSearch - unique predicates to check if block actually belongs structure
     * @param predicates - list of predicates
     * @return
     */
    private StructureMatch canBuildStructure(World world, BlockPos pos, Set<Predicate<BlockWorldState>> fastSearch, Predicate<BlockWorldState>[][][] predicates) {
        LoadingCache<BlockPos, BlockWorldState> cache = BlockPattern.createLoadingCache(world, true);

        // If no block belows to structure
        if (fastSearch.stream().noneMatch(x -> x.apply(cache.getUnchecked(pos))))
            return null;

        int max = Math.max(Math.max(this.palmLength, this.thumbLength), this.fingerLength);

        for (BlockPos blockpos : BlockPos.getAllInBox(pos, pos.add(max - 1, max - 1, max - 1))) {

            for (int i = 0; i < palmLength; ++i) {
                for (int j = 0; j < thumbLength; ++j) {
                    for (int k = 0; k < fingerLength; ++k) {
                        // block is belongs to structure
                        if (!Objects.equals(StructureBuilder.ANY, predicates[k][j][i])
                                && predicates[k][j][i].test(cache.getUnchecked(blockpos))) {

                            // checking all directions
                            for (EnumFacing enumfacing : EnumFacing.values()) {
                                for (EnumFacing enumfacing1 : EnumFacing.values()) {
                                    if (enumfacing1 != enumfacing && enumfacing1 != enumfacing.getOpposite()) {
                                        // trying to get corner of structure
                                        BlockPos cornerPos = PositionHelper.translateOffset(blockpos, enumfacing.getOpposite(), enumfacing1.getOpposite(), i, j, k);

                                        if (checkCorners(cornerPos, enumfacing, enumfacing1, predicates, cache)) {
                                            StructureMatch match = checkPatternAt(cornerPos, enumfacing, enumfacing1, predicates, cache);
                                            if (match != null)
                                                return match;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Calculates whether the given world position matches the pattern. Warning, fairly heavy function. @return a
     * BlockPattern.PatternHelper if found, null otherwise.
     */
    @Nullable
    private StructureMatch match(BlockPos pos, Predicate<BlockWorldState>[][][] predicates, LoadingCache<BlockPos, BlockWorldState> cache) {
        int i = Math.max(Math.max(this.palmLength, this.thumbLength), this.fingerLength) - 1;

        for (BlockPos blockpos : BlockPos.getAllInBox(pos, pos.add(i, i, i))) {
            for (EnumFacing enumfacing : EnumFacing.values()) {
                for (EnumFacing enumfacing1 : EnumFacing.values()) {
                    if (enumfacing1 != enumfacing && enumfacing1 != enumfacing.getOpposite()) {
                        StructureMatch match = checkPatternAt(blockpos, enumfacing, enumfacing1, predicates, cache);
                        if (match != null) {
                            return match;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Trying to check wherever pos is the corner of structure
     *
     * @return
     */
    private boolean checkCorners(BlockPos corner, EnumFacing finger, EnumFacing thumb,
                                 Predicate<BlockWorldState>[][][] predicates, LoadingCache<BlockPos, BlockWorldState> cache) {
        int f = fingerLength - 1;
        int t = thumbLength - 1;
        int p = palmLength - 1;

        Map<BlockPos, Predicate<BlockWorldState>> corners = new HashMap<BlockPos, Predicate<BlockWorldState>>() {{
            put(new BlockPos(0, 0, 0), predicates[0][0][0]);
            put(new BlockPos(p, 0, 0), predicates[0][0][p]);
            put(new BlockPos(0, t, 0), predicates[0][t][0]);
            put(new BlockPos(p, t, 0), predicates[0][t][p]);
            put(new BlockPos(0, 0, f), predicates[f][0][0]);
            put(new BlockPos(p, 0, f), predicates[f][0][p]);
            put(new BlockPos(0, t, f), predicates[f][t][0]);
            put(new BlockPos(p, t, f), predicates[f][t][p]);
        }};

        for (Map.Entry<BlockPos, Predicate<BlockWorldState>> entry : corners.entrySet()) {
            Predicate<BlockWorldState> predicate = entry.getValue();
            BlockPos offset = entry.getKey();
            offset = PositionHelper.translateOffset(corner, finger, thumb, offset.getX(), offset.getY(), offset.getZ());
            BlockWorldState state = cache.getUnchecked(offset);

            if (!predicate.test(state))
                return false;
        }

        return true;
    }

    /**
     * checks that the given pattern & rotation is at the block co-ordinates.
     */
    @Nullable
    private StructureMatch checkPatternAt(BlockPos pos, EnumFacing finger, EnumFacing thumb, Predicate<BlockWorldState>[][][] predicates, LoadingCache<BlockPos, BlockWorldState> lcache) {
        Map<BlockPos, IBlockState> structure = new HashMap<>();
        Map<BlockPos, Predicate<BlockWorldState>> structurePattern = new HashMap<>();
        Map<BlockPos, IBlockState> builtStructure = new HashMap<>();
        Map<BlockPos, Predicate<BlockWorldState>> builtStructurePattern = new HashMap<>();


        for (int i = 0; i < palmLength; ++i) {
            for (int j = 0; j < thumbLength; ++j) {
                for (int k = 0; k < fingerLength; ++k) {
                    BlockPos currentPos = PositionHelper.translateOffset(pos, finger, thumb, i, j, k);

                    if (!predicates[k][j][i].apply(lcache.getUnchecked(currentPos))) {
                        return null;
                    }

                    structure.put(currentPos, this.structure[k][j][i]);
                    builtStructure.put(currentPos, this.buildedStructure[k][j][i]);

                    structurePattern.put(currentPos, this.structurePredicates[k][j][i]);
                    builtStructurePattern.put(currentPos, this.buildedStructurePredicates[k][j][i]);
                }
            }
        }

        AxisAlignedBB size = new AxisAlignedBB(
                PositionHelper.translateOffset(pos, finger, thumb, 0, 0, 0),
                PositionHelper.translateOffset(pos, finger, thumb, palmLength - 1, thumbLength - 1, fingerLength - 1));

        return new StructureMatch(size, structure, structurePattern, builtStructure, builtStructurePattern, predicates == buildedStructurePredicates, finger, thumb);
    }
}
