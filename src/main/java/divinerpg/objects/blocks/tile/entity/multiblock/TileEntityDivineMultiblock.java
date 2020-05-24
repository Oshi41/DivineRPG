package divinerpg.objects.blocks.tile.entity.multiblock;

import divinerpg.DivineRPG;
import divinerpg.events.server.SwapFactory;
import divinerpg.objects.blocks.tile.entity.base.ModUpdatableTileEntity;
import divinerpg.objects.blocks.tile.entity.base.rituals.IRitualConsumer;
import divinerpg.objects.blocks.tile.entity.base.rituals.IRitualDescription;
import divinerpg.objects.blocks.tile.entity.base.rituals.RitualRegistry;
import divinerpg.utils.multiblock.StructureMatch;
import divinerpg.utils.multiblock.StructurePattern;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TileEntityDivineMultiblock extends ModUpdatableTileEntity implements IMultiblockTile, IInteractionObject, IRitualConsumer {

    private final StructurePattern pattern;
    private final String name;
    private final Integer guiId;

    private StructureMatch multiblockMatch;
    private boolean working;

    private IRitualDescription current;

    /**
     * Data from NBT
     */
    @SideOnly(Side.CLIENT)
    protected boolean constructedOnServer = true;

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

        if (multiMatch != null) {
            onBuilt(multiMatch);
        } else {
            if (prevMatch != null) {
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

    // region NBT

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setBoolean("constructed", getMultiblockMatch() != null);
        return tag;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound nbt = super.writeToNBT(compound);

        if (getCurrentRitual() != null) {
            nbt.setTag("ritual", getCurrentRitual().serializeNBT());
        }

        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        if (compound.hasKey("ritual")) {
            NBTTagCompound tag = compound.getCompoundTag("ritual");
            setCurrentRitual(RitualRegistry.createById(new ResourceLocation(tag.getString("Id")), this, IRitualDescription.class));
        }
    }

    //endregion

    @Override
    public IRitualDescription getCurrentRitual() {
        return current;
    }

    @Override
    public void setCurrentRitual(IRitualDescription description) {
        current = description;
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
