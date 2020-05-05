package divinerpg.utils.multiblock;

import com.google.common.base.Predicate;
import divinerpg.api.DivineAPI;
import divinerpg.objects.blocks.tile.entity.multiblock.IMultiStructure;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.block.state.pattern.FactoryBlockPattern;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

public class Matcher implements IMultiStructure {
    private final FactoryBlockPattern factory;
    private final FactoryBlockPattern createdStructurefactory;
    private final Map<Predicate<BlockWorldState>, IBlockState> symbolMap;
    private final Map<IBlockState, IBlockState> replaceMap;
    private BlockPattern pattern;
    private BlockPattern createdStructurePattern;
    private IBlockState[][][] structure;

    public Matcher() {
        factory = FactoryBlockPattern.start();
        createdStructurefactory = FactoryBlockPattern.start();

        symbolMap = new LinkedHashMap<>();
        replaceMap = new LinkedHashMap<>();
    }

    /**
     * Adding pattern similar to FactoryBlockPattern
     *
     * @param aisle
     * @return
     */
    public Matcher aisle(String... aisle) {
        factory.aisle(aisle);
        createdStructurefactory.aisle(aisle);
        return this;
    }

    /**
     * Block patter condition
     *
     * @param symbol       - symbol from pattern
     * @param block        - structure block state
     * @param replaceBlock - replacing blockstate when structure will created
     * @return
     */
    public Matcher where(char symbol, IBlockState block, IBlockState replaceBlock) {
        Predicate<BlockWorldState> predicate = BlockWorldState.hasState(BlockStateMatcher.forBlock(block.getBlock()));

        factory.where(symbol, predicate);
        createdStructurefactory.where(symbol, BlockWorldState.hasState(BlockStateMatcher.forBlock(replaceBlock.getBlock())));

        symbolMap.put(predicate, block);
        replaceMap.put(block, replaceBlock);
        return this;
    }

    /**
     * Building pattern
     */
    public void build() {
        pattern = factory.build();
        createdStructurePattern = factory.build();
        structure = makePredicateArray();
    }

    /**
     * Checking if structure is matching
     *
     * @param world - world
     * @param pos   - position
     * @return
     */
    @Override
    @Nullable
    public BlockPattern.PatternHelper isMatch(World world, BlockPos pos) {
        Set<BlockPos> poses = getPossibleCorners(world, pos);

        return poses
                .stream()
                .map(x -> {
                    BlockPattern.PatternHelper result = pattern.match(world, x);

                    if (result == null) {
                        result = createdStructurePattern.match(world, x);
                    }

                    return result;
                })
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    /**
     * Constuct structure
     *
     * @param world        - world
     * @param isDestucting - is breaking the structure down
     */
    @Override
    public void constructStructure(World world, BlockPos topLeft, EnumFacing finger, EnumFacing thumb, boolean isDestucting) {
        if (world == null)
            return;

        for (int i = 0; i < pattern.getPalmLength(); ++i) {
            for (int j = 0; j < pattern.getThumbLength(); ++j) {
                for (int k = 0; k < pattern.getFingerLength(); ++k) {

                    BlockPos pos = translateOffset(topLeft, finger, thumb, i, j, k);

                    IBlockState structureState = structure[k][j][i];
                    IBlockState buildedStructureState = replaceMap.get(structureState);

                    if (isDestucting) {
                        if (world.getBlockState(pos).getBlock() == buildedStructureState.getBlock()) {
                            setBlock(world, pos, structureState);
                        }
                    } else {
                        setBlock(world, pos, buildedStructureState);
                    }
                }
            }
        }
    }

    /**
     * copy of BlockPattern.translateOffset
     *
     * @param pos
     * @param finger
     * @param thumb
     * @param palmOffset
     * @param thumbOffset
     * @param fingerOffset
     * @return
     */
    protected BlockPos translateOffset(BlockPos pos, EnumFacing finger, EnumFacing thumb, int palmOffset, int thumbOffset, int fingerOffset) {
        if (finger != thumb && finger != thumb.getOpposite()) {
            Vec3i vec3i = new Vec3i(finger.getFrontOffsetX(), finger.getFrontOffsetY(), finger.getFrontOffsetZ());
            Vec3i vec3i1 = new Vec3i(thumb.getFrontOffsetX(), thumb.getFrontOffsetY(), thumb.getFrontOffsetZ());
            Vec3i vec3i2 = vec3i.crossProduct(vec3i1);
            return pos.add(vec3i1.getX() * -thumbOffset + vec3i2.getX() * palmOffset + vec3i.getX() * fingerOffset, vec3i1.getY() * -thumbOffset + vec3i2.getY() * palmOffset + vec3i.getY() * fingerOffset, vec3i1.getZ() * -thumbOffset + vec3i2.getZ() * palmOffset + vec3i.getZ() * fingerOffset);
        } else {
            throw new IllegalArgumentException("Invalid forwards & up combination");
        }
    }

    /**
     * Returning
     *
     * @return
     */
    private IBlockState[][][] makePredicateArray() {
        Predicate<BlockWorldState>[][][] predicates = (Predicate<BlockWorldState>[][][]) DivineAPI.reflectionHelper.callMethod(factory, "makePredicateArray", () -> new Object[0]);

        IBlockState[][][] result = new IBlockState[predicates.length][][];

        for (int i = 0; i < predicates.length; i++) {
            Predicate<BlockWorldState>[][] predicates1 = predicates[i];
            result[i] = new IBlockState[predicates1.length][];

            for (int j = 0; j < predicates1.length; j++) {
                Predicate<BlockWorldState>[] predicates2 = predicates1[j];
                result[i][j] = Arrays.stream(predicates2).map(symbolMap::get).toArray(IBlockState[]::new);
            }
        }

        return result;
    }

    private void setBlock(World world, BlockPos pos, IBlockState state) {
        IBlockState old = world.getBlockState(pos);
        world.setBlockState(pos, state);
        world.notifyBlockUpdate(pos, old, state, 3);
    }

    /**
     * Searching for possible corners of structure
     *
     * @param world
     * @param pos
     * @return
     */
    private Set<BlockPos> getPossibleCorners(World world, BlockPos pos) {
        Set<BlockPos> result = new HashSet<>();

        for (EnumFacing enumfacing : EnumFacing.values()) {
            for (EnumFacing enumfacing1 : EnumFacing.values()) {
                if (enumfacing1 != enumfacing && enumfacing1 != enumfacing.getOpposite()) {

                    for (int i = 0; i < pattern.getPalmLength(); ++i) {
                        for (int j = 0; j < pattern.getThumbLength(); ++j) {
                            for (int k = 0; k < pattern.getFingerLength(); ++k) {

                                // searching from regular pre build structure pattern
                                addPossibleCorners(result, world, i, j, k, pos, structure[0][0][0].getBlock(), structure[k][j][i].getBlock(), enumfacing, enumfacing1);

                                // searching coreners from already built structure
                                addPossibleCorners(result, world, i, j, k, pos, replaceMap.get(structure[0][0][0]).getBlock(), replaceMap.get(structure[k][j][i]).getBlock(), enumfacing, enumfacing1);
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    private void addPossibleCorners(Set<BlockPos> result, World world, int i, int j, int k, BlockPos pos, Block corner, Block current, EnumFacing first, EnumFacing second) {
        // take block from world
        Block takenFromWorld = world.getBlockState(pos).getBlock();

        // structure contains current block
        if (current == takenFromWorld) {
            BlockPos possiblePos = translateOffset(pos, first, second, -i, -j, -k);

            // possible corner is same as planned
            if (world.getBlockState(possiblePos).getBlock() == corner) {
                result.add(possiblePos);
            }
        }
    }
}
