package divinerpg.objects.blocks.vanilla;

import divinerpg.enums.EnumBlockType;
import divinerpg.objects.blocks.BlockMod;
import divinerpg.objects.blocks.tile.entity.pillar.TileEntityPillar;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;

public class Pillar extends BlockMod implements ITileEntityProvider {
    private final AxisAlignedBB AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D);

    public Pillar(String name) {
        super(EnumBlockType.ROCK, name, 5);
        setResistance(2000);
        setLightOpacity(0);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityPillar();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ))
            return false;

        if (!worldIn.isRemote) {
            transferStack(worldIn, pos, playerIn, hand, playerIn.getHeldItem(hand));
        }

        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        IItemHandlerModifiable handler = getHandler(worldIn, pos);
        if (handler != null) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i).copy();
                handler.setStackInSlot(i, ItemStack.EMPTY);
                InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
        super.onBlockClicked(worldIn, pos, playerIn);

        if (!worldIn.isRemote) {
            transferStack(worldIn, pos, playerIn, EnumHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    @Nullable
    private IItemHandlerModifiable getHandler(World world, BlockPos pos) {
        TileEntity entity = world.getTileEntity(pos);
        if (entity == null)
            return null;

        if (entity instanceof TileEntityPillar) {
            return ((TileEntityPillar) entity).getInventory();
        }

        if (entity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            IItemHandler handler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

            if (handler instanceof IItemHandlerModifiable)
                return (IItemHandlerModifiable) handler;
        }

        return null;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return super.getBoundingBox(state, source, pos);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return face == EnumFacing.DOWN
                ? BlockFaceShape.SOLID
                : BlockFaceShape.UNDEFINED;
    }

    /**
     * Trying to trasfer stack between pillar and player inventory
     *
     * @param world
     * @param pos
     * @param player
     * @param hand
     */
    private void transferStack(World world, BlockPos pos, EntityPlayer player, EnumHand hand, ItemStack heldItem) {
        IItemHandlerModifiable itemHandler = getHandler(world, pos);
        if (itemHandler != null) {

            ItemStack containigItem = itemHandler.getStackInSlot(0);
            boolean wasChanged = false;

            if (!heldItem.isEmpty() && itemHandler.isItemValid(0, heldItem)) {
                heldItem = itemHandler.insertItem(0, heldItem, false);
                player.setHeldItem(hand, heldItem);
                wasChanged = true;
            } else if (!containigItem.isEmpty() && heldItem.isEmpty()) {
                if (!player.inventory.addItemStackToInventory(containigItem)) {
                    InventoryHelper.spawnItemStack(world, player.posX, player.posY, player.posZ, containigItem);
                }
                itemHandler.setStackInSlot(0, ItemStack.EMPTY);
                wasChanged = true;
            }

            if (wasChanged) {
                IBlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, state, state, 8);
            }
        }
    }
}
