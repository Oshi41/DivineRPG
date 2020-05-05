package divinerpg.utils.multiblock;

import divinerpg.DivineRPG;
import divinerpg.api.Reference;
import divinerpg.enums.EnumPlaceholder;
import divinerpg.objects.blocks.tile.entity.multiblock.IMultiStructure;
import divinerpg.registry.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MultiblockDescription {
    /**
     * Map of all contaning structure infos
     */
    private static final Map<ResourceLocation, IMultiStructure> multiStructures;

    /**
     * Wekk known id of king compressor
     */
    public static final ResourceLocation KING_COMPRESSOR;

    static {
        multiStructures = new HashMap<>();
        //
        // Example
        //

        KING_COMPRESSOR = new ResourceLocation(Reference.MODID, "king_compressor");

        register(KING_COMPRESSOR,
                new Matcher()
                        .aisle(
                                "aaa",
                                "aaa",
                                "aaa")
                        .aisle(
                                "ooo",
                                "oko",
                                "ooo")
                        .aisle(
                                "pop",
                                "pAp",
                                "pop")
                        .where('a', Blocks.ANVIL.getDefaultState(), ModBlocks.structure_block.withPlaceHolder(EnumPlaceholder.ANVIL))
                        .where('o', Blocks.OBSIDIAN.getDefaultState(), ModBlocks.structure_block.withPlaceHolder(EnumPlaceholder.OBSIDIAN))
                        .where('k', ModBlocks.king_compressor_part.getDefaultState(), ModBlocks.king_compressor.getDefaultState())
                        .where('p', ModBlocks.pillar.getDefaultState(), ModBlocks.structure_block.withPlaceHolder(EnumPlaceholder.PILLAR))
                        .where('A', Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState())
                        .build());
    }

    public static void register(ResourceLocation id, IMultiStructure structure) {
        if (multiStructures.containsKey(id)) {
            DivineRPG.logger.warn(String.format("That key %s will be overwritten"));
        }

        multiStructures.put(id, structure);
    }

    @Nullable
    public static IMultiStructure findById(ResourceLocation id) {
        return multiStructures.get(id);
    }

    /**
     * Unmodifyable collection of all multiblock structures
     *
     * @return
     */
    public static Collection<IMultiStructure> getAll() {
        return Collections.unmodifiableCollection(multiStructures.values());
    }
}
