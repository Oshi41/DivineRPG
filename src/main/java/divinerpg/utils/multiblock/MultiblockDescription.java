package divinerpg.utils.multiblock;

import divinerpg.DivineRPG;
import divinerpg.api.Reference;
import divinerpg.enums.EnumPlaceholder;
import divinerpg.registry.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MultiblockDescription {
    public final static MultiblockDescription instance = new MultiblockDescription();

    /**
     * List of all structures
     */
    private final Map<ResourceLocation, StructurePattern> possibleStructures = new HashMap<>();

    private MultiblockDescription() {
        register(new ResourceLocation(Reference.MODID, "king_compressor"),
                new StructureBuilder()
                        .aisle(
                                "annnnna",
                                "nnnnnnn",
                                "nnnnnnn",
                                "nnnnnnn",
                                "nnnnnnn",
                                "nnnnnnn",
                                "annnnna"
                        )
                        .aisle(
                                "anbbbna",
                                "nAAAAAn",
                                "nAAAAAn",
                                "nAAKAAn",
                                "nAAAAAn",
                                "nAaaaAn",
                                "anbbbna"
                        )
                        .aisle(
                                "aanbnaa",
                                "anAAAna",
                                "anAAAna",
                                "anAAAna",
                                "anAAAna",
                                "anAAAna",
                                "aanbnaa"
                        )
                        .aisle(
                                "aaanaaa",
                                "aanPnaa",
                                "aanPnaa",
                                "aanPnaa",
                                "aanPnaa",
                                "aanPnaa",
                                "aaanaaa"
                        )
                        .where('a', null)
                        .where('A', null, ModBlocks.structure_block.getDefaultState())
                        .where('n', Blocks.NETHER_BRICK.getDefaultState(), ModBlocks.structure_block.withPlaceHolder(EnumPlaceholder.NETHER_BRICK))
                        .where('b', Blocks.IRON_BARS.getDefaultState(), ModBlocks.structure_block.withPlaceHolder(EnumPlaceholder.IRON_BARS))
                        .where('K', null, ModBlocks.king_compressor.getDefaultState())
                        .where('P', ModBlocks.king_compressor_part.getDefaultState(), ModBlocks.structure_block.withPlaceHolder(EnumPlaceholder.COMPRESSOR_PART))
                        .build()
        );

//        register(new ResourceLocation(Reference.MODID, "king_compressor"),
//                new StructureBuilder()
//                        .aisle(
//                                "AAA",
//                                "oMo",
//                                "ooo",
//                                "ooo",
//                                "ooo",
//                                "ooo",
//                                "ooo"
//                        )
//                        .aisle(
//                                "AAA",
//                                "ooo",
//                                "ooo",
//                                "ooo",
//                                "ooo",
//                                "ooo",
//                                "ooo"
//                        )
//                        .aisle(
//                                "AAA",
//                                "ooo",
//                                "olo",
//                                "KbK",
//                                "aaa",
//                                "aaa",
//                                "pap"
//                        )
//                        .aisle(
//                                "AAA",
//                                "ooo",
//                                "ooo",
//                                "ooo",
//                                "aaa",
//                                "aaa",
//                                "aaa"
//                        )
//                        .aisle(
//                                "aaa",
//                                "pap",
//                                "pap",
//                                "aaa",
//                                "aaa",
//                                "aaa",
//                                "aaa"
//                        )
//                        .where('a', Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState())
//                        .where('A', Blocks.ANVIL.getDefaultState(), ModBlocks.structure_block.withPlaceHolder(EnumPlaceholder.ANVIL))
//                        .where('p', ModBlocks.pillar.getDefaultState(), ModBlocks.pillar.getDefaultState())
//                        .where('b', Blocks.IRON_BARS.getDefaultState(), ModBlocks.structure_block.withPlaceHolder(EnumPlaceholder.ICON_BARS))
//                        .where('o', Blocks.OBSIDIAN.getDefaultState(), ModBlocks.structure_block.withPlaceHolder(EnumPlaceholder.OBSIDIAN))
//                        .where('M', Blocks.OBSIDIAN.getDefaultState(), ModBlocks.king_compressor.getDefaultState())
//                        .where('K', ModBlocks.king_compressor_part.getDefaultState(),  ModBlocks.structure_block.withPlaceHolder(EnumPlaceholder.COMPRESSOR_PART))
//                        .where('l', Blocks.LAVA.getDefaultState(), ModBlocks.structure_block.getDefaultState())
//                        .build());
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
