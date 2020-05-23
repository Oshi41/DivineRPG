package divinerpg.objects.blocks.structure;

import divinerpg.enums.EnumPlaceholder;
import divinerpg.events.server.SwapFactory;
import divinerpg.objects.blocks.BlockMod;
import divinerpg.objects.blocks.tile.entity.multiblock.IMultiblockTile;
import divinerpg.utils.PositionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class StructureBlock extends BlockMod {
    public static final IProperty<EnumPlaceholder> PlaceholderProperty;

    static {
        PlaceholderProperty = PropertyEnum.create("placeholder_property", EnumPlaceholder.class);
    }

    public StructureBlock() {
        super("structure_block", 3);
        setDefaultState(withPlaceHolder(EnumPlaceholder.AIR));

        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            registerStateMapper();
        }
    }

    public IBlockState withPlaceHolder(EnumPlaceholder placeHolder) {
        return getDefaultState().withProperty(PlaceholderProperty, placeHolder);
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        Block block = getInnerBlock(state);
        if (block != null)
            return block.getDefaultState();

        return super.getExtendedState(state, world, pos);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing clickFacing, float hitX, float hitY, float hitZ) {

        List<IMultiblockTile> tiles = PositionHelper.findTilesInStructureBlocks(worldIn, pos, IMultiblockTile.class, null, null, null);

        if (!tiles.isEmpty()) {
            tiles.get(0).click(playerIn);
            return true;
        } else {
            SwapFactory.instance.recheck(worldIn, pos);
            return false;
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return getInnerBlock(state).isOpaqueCube(state);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return getInnerBlock(state).isFullCube(state);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        Block block = getInnerBlock(state);
        return block.getBoundingBox(block.getDefaultState(), source, pos);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        Block block = getInnerBlock(blockState);
        return block.getCollisionBoundingBox(block.getDefaultState(), worldIn, pos);
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        SwapFactory.instance.recheck(worldIn, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        SwapFactory.instance.recheck(worldIn, pos);
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

    @SideOnly(Side.CLIENT)
    protected void registerStateMapper() {
        ModelLoader.setCustomStateMapper(this, new StructureBlockMapper());
    }

    private Block getInnerBlock(IBlockState state) {
        if (state != null
                && state.getPropertyKeys().contains(PlaceholderProperty)) {
            EnumPlaceholder value = state.getValue(PlaceholderProperty);

            if (value != null) {
                return value.getBlock();
            }
        }

        return Blocks.AIR;
    }
}
