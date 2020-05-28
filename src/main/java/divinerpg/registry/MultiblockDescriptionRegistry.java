package divinerpg.registry;

import divinerpg.DivineRPG;
import divinerpg.enums.EnumPlaceholder;
import divinerpg.utils.multiblock.StructureBuilder;
import divinerpg.utils.multiblock.StructurePattern;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MultiblockDescriptionRegistry {
    public final static MultiblockDescriptionRegistry instance = new MultiblockDescriptionRegistry();

    /**
     * List of all structures
     */
    private final Map<ResourceLocation, StructurePattern> possibleStructures = new HashMap<>();

    private MultiblockDescriptionRegistry() {
        register(new ResourceLocation(DivineRPG.MODID, "king_compressor"),
                new StructureBuilder()
                        .aisle(
                                " nnnnn ",
                                "nnnnnnn",
                                "nnnnnnn",
                                "nnnnnnn",
                                "nnnnnnn",
                                "nnnnnnn",
                                " nnnnn "
                        )
                        .aisle(
                                " nbbbn ",
                                "n     n",
                                "n     n",
                                "n  K  n",
                                "n     n",
                                "n     n",
                                " nbbbn "
                        )
                        .aisle(
                                "  nbn  ",
                                " n   n ",
                                " n   n ",
                                " n   n ",
                                " n   n ",
                                " n   n ",
                                "  nbn  "
                        )
                        .aisle(
                                "   n   ",
                                "  nPn  ",
                                "  nPn  ",
                                "  nPn  ",
                                "  nPn  ",
                                "  nPn  ",
                                "   n   "
                        )
                        .where(' ', null)
                        .where('n', Blocks.NETHER_BRICK.getDefaultState(), BlockRegistry.structure_block.withPlaceHolder(EnumPlaceholder.NETHER_BRICK))
                        .where('b', Blocks.IRON_BARS.getDefaultState(), BlockRegistry.structure_block.withPlaceHolder(EnumPlaceholder.IRON_BARS))
                        .where('K', null, BlockRegistry.king_compressor.getDefaultState())
                        .where('P', BlockRegistry.king_compressor_part.getDefaultState(), BlockRegistry.structure_block.withPlaceHolder(EnumPlaceholder.COMPRESSOR_PART))
                        .build()
        );
    }

    public void register(ResourceLocation id, StructurePattern pattern) {
        if (possibleStructures.containsKey(id)) {
            DivineRPG.logger.warn(String.format("Overwriting already registered structure (%s)", id.toString()));
        }

        possibleStructures.put(id, pattern);
    }

    /**
     * Searches structure by name
     *
     * @param id
     * @return
     */
    public StructurePattern findById(ResourceLocation id) {
        return possibleStructures.get(id);
    }

    public Set<StructurePattern> getAll() {
        return new HashSet<>(possibleStructures.values());
    }
}
