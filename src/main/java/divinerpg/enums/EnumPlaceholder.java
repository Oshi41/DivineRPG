package divinerpg.enums;

import divinerpg.DivineRPG;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public enum EnumPlaceholder implements IStringSerializable {
    AIR(Blocks.AIR.getRegistryName()),
    OBSIDIAN(Blocks.OBSIDIAN.getRegistryName()),
    NETHER_BRICK(Blocks.NETHER_BRICK.getRegistryName()),
    IRON_BARS(Blocks.IRON_BARS.getRegistryName()),
    ANVIL(Blocks.ANVIL.getRegistryName()),
    QUARTZ_STAIRS(Blocks.QUARTZ_STAIRS.getRegistryName()),
    PILLAR("pillar"),
    COMPRESSOR_PART("king_compressor_part");


    private final ResourceLocation blockId;

    EnumPlaceholder(ResourceLocation blockId) {
        this.blockId = blockId;
    }

    EnumPlaceholder(String divineName) {
        this(new ResourceLocation(DivineRPG.MODID, divineName));
    }

    /**
     * Returns block from enum
     *
     * @return
     */
    public Block getBlock() {
        return ForgeRegistries.BLOCKS.getValue(blockId);
    }

    @Override
    public String getName() {
        // It's important, lowercase only!
        return toString().toLowerCase();
    }
}
