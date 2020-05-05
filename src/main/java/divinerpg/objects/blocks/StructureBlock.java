package divinerpg.objects.blocks;

import divinerpg.enums.EnumBlockType;
import divinerpg.enums.EnumPlaceholder;
import divinerpg.objects.blocks.tile.entity.multiblock.IMultiblockTile;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Random;

public class StructureBlock extends BlockMod {
    private static final IProperty<EnumPlaceholder> PlaceholderProperty;

    static {
        PlaceholderProperty = PropertyEnum.create("placeholder_property", EnumPlaceholder.class);
    }

    public StructureBlock() {
        super(EnumBlockType.ROCK, "structure_block", 3, null);
        setDefaultState(withPlaceHolder(EnumPlaceholder.AIR));
    }

    public IBlockState withPlaceHolder(EnumPlaceholder placeHolder) {
        return getDefaultState().withProperty(PlaceholderProperty, placeHolder);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);

        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos currentPos = pos.offset(facing);

            TileEntity tileEntity = worldIn.getTileEntity(currentPos);
            if (tileEntity instanceof IMultiblockTile) {
                ((IMultiblockTile) tileEntity).recheckStructure();
                return;
            } else {
                IBlockState neighbour = worldIn.getBlockState(currentPos);
                worldIn.notifyBlockUpdate(currentPos, neighbour, neighbour, 3);
            }
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (state.getPropertyKeys().contains(PlaceholderProperty)) {
            EnumPlaceholder placeholder = state.getValue(PlaceholderProperty);
            if (placeholder != null) {
                return Item.getItemFromBlock(placeholder.getBlock());
            }
        }

        return Items.AIR;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, PlaceholderProperty);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        if (state.getPropertyKeys().contains(PlaceholderProperty)) {
            return state.getValue(PlaceholderProperty).ordinal();
        }

        return super.getMetaFromState(state);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumPlaceholder placeholder = Arrays.stream(EnumPlaceholder.values()).filter(x -> meta == x.ordinal())
                .findFirst().orElse(EnumPlaceholder.AIR);

        return withPlaceHolder(placeholder);
    }
}
