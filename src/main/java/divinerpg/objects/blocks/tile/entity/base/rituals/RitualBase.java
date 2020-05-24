package divinerpg.objects.blocks.tile.entity.base.rituals;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class RitualBase implements IRitualDescription {
    private final List<Consumer<Boolean>> callbacks = new ArrayList<>();
    private final ResourceLocation id;
    private final TileEntity tile;
    private boolean isPerformed;

    protected RitualBase(ResourceLocation id, TileEntity tile) {
        this.id = id;
        this.tile = tile;
    }

    public void addCallBack(Consumer<Boolean> call) {
        callbacks.add(call);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public TileEntity getBindedTilEntity() {
        return tile;
    }

    @Override
    public boolean isPerformed() {
        return isPerformed;
    }

    @Override
    public void setIsPerformed(boolean isperformed) {
        if (isPerformed == isperformed)
            return;

        isPerformed = isperformed;

        if (!callbacks.isEmpty()) {
            callbacks.forEach(x -> x.accept(isPerformed));
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("isPerformed", isPerformed());
        compound.setString("Id", getId().toString());
        compound.setLong("Pos", tile.getPos().toLong());
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        setIsPerformed(nbt.getBoolean("isPerformed"));
    }
}
