package divinerpg.objects.blocks.vanilla;

import divinerpg.enums.EnumBlockType;
import divinerpg.objects.blocks.MultiBlockMod;
import divinerpg.objects.blocks.tile.entity.TileEntityKingCompressor;
import divinerpg.registry.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class KingCompressor extends MultiBlockMod {
    public KingCompressor(String name) {
        super(TileEntityKingCompressor.class, EnumBlockType.ROCK, name, 5);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (tileEntity instanceof TileEntityKingCompressor && !((TileEntityKingCompressor) tileEntity).keepInventory)
            InventoryHelper.dropInventoryItems(worldIn, pos, ((IInventory) tileEntity));

        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(ModBlocks.king_compressor_part);
    }
}
