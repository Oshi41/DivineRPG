package divinerpg.objects.blocks;

import divinerpg.enums.EnumBlockType;
import divinerpg.objects.blocks.tile.entity.multiblock.IMultiblockTile;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class MultiBlockMod extends BlockMod implements ITileEntityProvider {
    /**
     * Tile ID
     */
    private final Class<? extends TileEntity> clazz;

    public MultiBlockMod(Class<? extends TileEntity> clazz,
                         EnumBlockType blockType,
                         String name,
                         float hardness) {
        this(clazz, blockType, name, hardness, null);
    }

    public MultiBlockMod(Class<? extends TileEntity> clazz, EnumBlockType blockType, String name,
                         float hardness, CreativeTabs tab) {
        super(blockType, name, hardness, tab);

        this.clazz = clazz;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity entity = worldIn.getTileEntity(pos);
        if (entity instanceof IMultiblockTile) {
            ((IMultiblockTile) entity).onDestroy();
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity entity = worldIn.getTileEntity(pos);
        if (entity instanceof IMultiblockTile) {
            ((IMultiblockTile) entity).click(playerIn);
            return true;
        }

        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (tileEntity instanceof IMultiblockTile) {
            ((IMultiblockTile) tileEntity).recheckStructure();
        }
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        try {
            TileEntity entity = clazz.newInstance();
            return entity;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
}
