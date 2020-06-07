package divinerpg.capabilities.gravity;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GravityProvider implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(IGravity.class)
    public static final Capability<IGravity> GravityCapability = null;
    private final IGravity instance;

    public GravityProvider(double multiplier) {
        this(multiplier, null);
    }

    public GravityProvider(double multiplier, ICapabilityProvider provider) {
        instance = new GravityHandler(provider, multiplier);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == GravityCapability;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == GravityCapability
                ? GravityCapability.cast(this.instance)
                : null;
    }

    @Override
    public NBTBase serializeNBT() {
        return GravityCapability.getStorage().writeNBT(GravityCapability, this.instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        GravityCapability.getStorage().readNBT(GravityCapability, this.instance, null, nbt);
    }
}
