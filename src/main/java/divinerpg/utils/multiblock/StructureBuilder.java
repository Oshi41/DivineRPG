package divinerpg.utils.multiblock;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.init.Blocks;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

public class StructureBuilder {
    /**
     * Single instance for air check
     */
    public static final Predicate<BlockWorldState> ANY = BlockWorldState.hasState(BlockStateMatcher.ANY);

    private static final Joiner COMMA_JOIN = Joiner.on(",");
    private final List<String[]> depth = Lists.newArrayList();
    private final Map<Character, Predicate<BlockWorldState>> structureSymbolMap = Maps.newHashMap();
    private final Map<Character, IBlockState> structureMap = Maps.newHashMap();

    private final Map<Character, Predicate<BlockWorldState>> buildedStructureSymbolMap = Maps.newHashMap();
    private final Map<Character, IBlockState> buildedStructureMap = Maps.newHashMap();
    private int aisleHeight;
    private int rowWidth;

    public StructureBuilder() {
        this.structureSymbolMap.put(' ', Predicates.alwaysTrue());
    }

    /**
     * Adds a single aisle to this pattern, going in the z axis. (so multiple calls to this will increase the z-size by
     * 1)
     */
    public StructureBuilder aisle(String... aisle) {
        if (!ArrayUtils.isEmpty(aisle) && !StringUtils.isEmpty(aisle[0])) {
            if (this.depth.isEmpty()) {
                this.aisleHeight = aisle.length;
                this.rowWidth = aisle[0].length();
            }

            if (aisle.length != this.aisleHeight) {
                throw new IllegalArgumentException("Expected aisle with height of " + this.aisleHeight + ", but was given one with a height of " + aisle.length + ")");
            } else {
                for (String s : aisle) {
                    if (s.length() != this.rowWidth) {
                        throw new IllegalArgumentException("Not all rows in the given aisle are the correct width (expected " + this.rowWidth + ", found one with " + s.length() + ")");
                    }

                    for (char c0 : s.toCharArray()) {
                        if (!this.structureSymbolMap.containsKey(c0)) {
                            this.structureSymbolMap.put(c0, null);
                        }
                    }
                }

                this.depth.add(aisle);
                return this;
            }
        } else {
            throw new IllegalArgumentException("Empty pattern for aisle");
        }
    }

    public StructureBuilder where(char symbol, IBlockState structure) {
        return where(symbol, structure, structure);
    }

    public StructureBuilder where(char symbol, IBlockState structure, IBlockState buildedStructure) {
        return where(symbol, getFromBlock(structure), structure, getFromBlock(buildedStructure), buildedStructure);
    }

    public StructureBuilder where(char symbol, Predicate<BlockWorldState> structurePredicat, IBlockState structure, Predicate<BlockWorldState> buildedStructurePredicat, IBlockState buildedStructure) {
        if (structure == null) {
            structure = Blocks.AIR.getDefaultState();
        }

        if (buildedStructure == null) {
            buildedStructure = Blocks.AIR.getDefaultState();
        }

        this.structureSymbolMap.put(symbol, structurePredicat);
        this.structureMap.put(symbol, structure);
        this.buildedStructureSymbolMap.put(symbol, buildedStructurePredicat);
        this.buildedStructureMap.put(symbol, buildedStructure);
        return this;
    }

    public StructurePattern build() {
        checkMissingPredicates(this.structureSymbolMap);
        checkMissingPredicates(this.buildedStructureSymbolMap);

        Predicate<BlockWorldState>[][][] structurePattern = (Predicate<BlockWorldState>[][][]) Array.newInstance(Predicate.class, depth.size(), aisleHeight, rowWidth);
        IBlockState[][][] structure = (IBlockState[][][]) Array.newInstance(IBlockState.class, depth.size(), aisleHeight, rowWidth);
        Predicate<BlockWorldState>[][][] buildedStructurePattern = (Predicate<BlockWorldState>[][][]) Array.newInstance(Predicate.class, depth.size(), aisleHeight, rowWidth);
        IBlockState[][][] buildedStructure = (IBlockState[][][]) Array.newInstance(IBlockState.class, depth.size(), aisleHeight, rowWidth);

        for (int i = 0; i < this.depth.size(); ++i) {
            for (int j = 0; j < this.aisleHeight; ++j) {
                for (int k = 0; k < this.rowWidth; ++k) {
                    char c = depth.get(i)[j].charAt(k);

                    structurePattern[i][j][k] = structureSymbolMap.get(c);
                    buildedStructurePattern[i][j][k] = buildedStructureSymbolMap.get(c);

                    structure[i][j][k] = structureMap.get(c);
                    buildedStructure[i][j][k] = buildedStructureMap.get(c);
                }
            }
        }


        return new StructurePattern(structurePattern, structure, buildedStructurePattern, buildedStructure);
    }

    private void checkMissingPredicates(Map<Character, Predicate<BlockWorldState>> symbolMap) {
        List<Character> list = Lists.newArrayList();

        for (Map.Entry<Character, Predicate<BlockWorldState>> entry : symbolMap.entrySet()) {
            if (entry.getValue() == null) {
                list.add(entry.getKey());
            }
        }

        if (!list.isEmpty()) {
            throw new IllegalStateException("Predicates for character(s) " + COMMA_JOIN.join(list) + " are missing");
        }
    }

    /**
     * Returns ANY if null passed
     *
     * @param state
     * @return
     */
    private Predicate<BlockWorldState> getFromBlock(IBlockState state) {
        if (state == null)
            return ANY;

        return BlockWorldState.hasState(BlockStateMatcher.forBlock(state.getBlock()));
    }
}
