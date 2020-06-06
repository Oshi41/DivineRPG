package divinerpg.capabilities.gravity;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class GravityStorage implements Capability.IStorage<IGravity> {
    private static final String key = "Gravity";

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IGravity> capability, IGravity instance, EnumFacing side) {
        NBTTagCompound result = new NBTTagCompound();
        result.setDouble(key, instance.getGravityMultiplier());
        return result;
    }

    @Override
    public void readNBT(Capability<IGravity> capability, IGravity instance, EnumFacing side,
                        NBTBase nbt) {
        if (nbt instanceof NBTTagCompound && ((NBTTagCompound) nbt).hasKey(key)) {
            instance.setGravityMultiplier(((NBTTagCompound) nbt).getDouble(key));
        }
    }
}
