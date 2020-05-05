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

    static {
        multiStructures = new HashMap<>();

        //
        // Example
        //
        Matcher kingCompressor = new Matcher()
                .aisle(
                        "non",
                        "ooo",
                        "non")
                .aisle(
                        "non",
                        "olo",
                        "nbn")
                .aisle(
                        "non",
                        "oao",
                        "non")
                .where('n', Blocks.NETHER_BRICK.getDefaultState(), ModBlocks.structure_block.withPlaceHolder(EnumPlaceholder.NETHER_BRICK))
                .where('o', Blocks.OBSIDIAN.getDefaultState(), ModBlocks.structure_block.withPlaceHolder(EnumPlaceholder.OBSIDIAN))
                .where('l', Blocks.LAVA.getDefaultState(), ModBlocks.king_compression_still.getDefaultState())
                .where('b', Blocks.IRON_BARS.getDefaultState(), ModBlocks.structure_block.withPlaceHolder(EnumPlaceholder.ICON_BARS))
                .where('a', Blocks.AIR.getDefaultState(), ModBlocks.structure_block.getDefaultState());

        kingCompressor.build();

        register(new ResourceLocation(Reference.MODID, "king_compressor"), kingCompressor);
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
