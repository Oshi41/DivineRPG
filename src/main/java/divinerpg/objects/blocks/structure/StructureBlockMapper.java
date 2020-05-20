package divinerpg.objects.blocks.structure;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;

public class StructureBlockMapper extends StateMapperBase {

    @Override
    protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
        Block blockToRender = state.getBlock();

        if (state.getPropertyKeys().contains(StructureBlock.PlaceholderProperty)) {
            blockToRender = state.getValue(StructureBlock.PlaceholderProperty).getBlock();
        }

        return new ModelResourceLocation(Block.REGISTRY.getNameForObject(blockToRender), null);
    }
}
