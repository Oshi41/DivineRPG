package divinerpg.objects.blocks;

import divinerpg.enums.EnumBlockType;
import divinerpg.enums.EnumPlaceholder;
import divinerpg.objects.blocks.tile.entity.multiblock.IMultiblockTile;
import divinerpg.utils.multiblock.MultiblockDescription;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

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
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing clickFacing, float hitX, float hitY, float hitZ) {
        return searchTile(worldIn, pos, t -> t.click(playerIn)) != null;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        searchTile(worldIn, pos, IMultiblockTile::recheckStructure);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        searchTile(worldIn, pos, IMultiblockTile::recheckStructure);
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

    @Nullable
    private IMultiblockTile searchTile(World world, BlockPos pos, @Nullable Consumer<IMultiblockTile> callback) {

        BlockPattern.PatternHelper match = MultiblockDescription
                .getAll()
                .stream()
                .map(x -> x.isMatch(world, pos))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        if (match != null) {
            BlockPos topLeft = match.getFrontTopLeft();
            BlockPos bottomRight = topLeft.offset(match.getUp(), match.getHeight())
                    .offset(match.getForwards(), match.getWidth());

            Iterator<BlockPos> iterator = BlockPos.getAllInBox(topLeft, bottomRight).iterator();

            while (iterator.hasNext()) {
                TileEntity entity = world.getTileEntity(iterator.next());

                if (entity instanceof IMultiblockTile) {
                    IMultiblockTile multiEntity = (IMultiblockTile) entity;

                    if (callback == null) {
                        callback.accept(multiEntity);
                    }

                    return multiEntity;
                }
            }
        }

        return null;
    }
}
