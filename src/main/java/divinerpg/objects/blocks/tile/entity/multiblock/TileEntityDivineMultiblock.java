package divinerpg.objects.blocks.tile.entity.multiblock;

import divinerpg.DivineRPG;
import divinerpg.events.server.SwapFactory;
import divinerpg.objects.blocks.tile.entity.base.ModUpdatableTileEntity;
import divinerpg.utils.multiblock.StructureMatch;
import divinerpg.utils.multiblock.StructurePattern;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public abstract class TileEntityDivineMultiblock extends ModUpdatableTileEntity implements IMultiblockTile, IInteractionObject {

    private final StructurePattern pattern;
    private final String name;
    private final Integer guiId;

    private StructureMatch multiblockMatch;
    private boolean working;

    /**
     * Data from NBT
     */
    @SideOnly(Side.CLIENT)
    protected boolean constructedOnServer;

    public TileEntityDivineMultiblock(StructurePattern pattern, String name, Integer guiId) {
        this.pattern = pattern;
        this.name = name;
        this.guiId = guiId;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        recheckStructure();
    }

    @Override
    public StructurePattern getPattern() {
        return pattern;
    }

    @Nullable
    @Override
    public StructureMatch getMultiblockMatch() {
        return multiblockMatch;
    }

    @Override
    public void onDestroy(@Nonnull StructureMatch match) {
        SwapFactory.instance.destroy(world, match, null);
    }

    @Override
    public void onBuilt(@Nonnull StructureMatch match) {
        SwapFactory.instance.recheck(world, null, getPattern(), match);
    }

    @Override
    public void recheckStructure() {
        if (working)
            return;

        working = true;
        StructureMatch prevMatch = getMultiblockMatch();
        StructureMatch multiMatch = getPattern().checkMultiblock(world, getPos());

        // changes detected
        if (!Objects.equals(prevMatch, multiMatch)) {
            if (multiMatch != null) {
                onBuilt(multiMatch);
            } else {
                onDestroy(prevMatch);
            }
        }

        multiblockMatch = multiMatch;
        working = false;

        sendBlockUpdate();
    }

    @Override
    public void click(EntityPlayer player) {
        if (guiId == null || player.getEntityWorld().isRemote)
            return;

        BlockPos pos = getPos();
        player.openGui(DivineRPG.instance, guiId, player.world, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean isConstructed() {
        return getMultiblockMatch() != null;
    }

    @Override
    public String getGuiID() {
        return TileEntity.getKey(getClass()).toString();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setBoolean("constructed", getMultiblockMatch() != null);
        return tag;
    }

    /**
     * Rechecking structure on client side
     *
     * @param net
     * @param pkt
     */
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);

        NBTTagCompound compound = pkt.getNbtCompound();
        if (compound.hasKey("constructed")) {

            if (world.isRemote) {
                constructedOnServer = compound.getBoolean("constructed");
            }
        }
    }

    /**
     * Only server side operation
     */
    private void sendBlockUpdate() {
        if (world.isRemote)
            return;

        // notify client about structure recheck
        IBlockState state = world.getBlockState(getPos());
        world.notifyBlockUpdate(getPos(), state, state, 2);
    }
}
