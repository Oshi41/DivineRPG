package divinerpg.enums;

import divinerpg.api.Reference;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public enum EnumPlaceholder implements IStringSerializable {
    AIR(Blocks.AIR.getRegistryName()),
    OBSIDIAN(Blocks.OBSIDIAN.getRegistryName()),
    NETHER_BRICK(Blocks.NETHER_BRICK.getRegistryName()),
    ICON_BARS(Blocks.IRON_BARS.getRegistryName()),
    ANVIL(Blocks.ANVIL.getRegistryName()),
    PILLAR("pillar");


    private final ResourceLocation blockId;

    EnumPlaceholder(ResourceLocation blockId) {
        this.blockId = blockId;
    }

    EnumPlaceholder(String divineName) {
        this(new ResourceLocation(Reference.MODID, divineName));
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
