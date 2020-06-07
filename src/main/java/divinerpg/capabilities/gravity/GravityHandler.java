package divinerpg.capabilities.gravity;

import net.minecraft.entity.Entity;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

public class GravityHandler implements IGravity {
    /**
     * Capability owner
     */
    private final WeakReference<ICapabilityProvider> owner;
    private double gravity;

    /**
     * NBT ctor
     */
    public GravityHandler() {
        this(null, 0);
    }

    /**
     * Supports change gravity event
     *
     * @param owner
     * @param multiplier
     */
    public GravityHandler(ICapabilityProvider owner, double multiplier) {
        this.owner = new WeakReference<>(owner);
        setGravityMultiplier(multiplier);
    }

    @Override
    public double getGravityMultiplier() {
        return gravity;
    }

    @Override
    public void setGravityMultiplier(double value) {
        if (gravity == value)
            return;

        gravity = value;
        fireChanges();
    }

    @Nullable
    @Override
    public ICapabilityProvider getOwner() {
        return owner.get();
    }

    private void fireChanges() {
        ICapabilityProvider provider = owner.get();
        if (provider == null)
            return;

        if (provider instanceof Entity) {
            MinecraftForge.EVENT_BUS.post(new GravityChangedEvent<>(Entity.class, (Entity) provider, this));
        } else if (provider instanceof Chunk) {
            MinecraftForge.EVENT_BUS.post(new GravityChangedEvent<>(Chunk.class, (Chunk) provider, this));
        }
    }
}
