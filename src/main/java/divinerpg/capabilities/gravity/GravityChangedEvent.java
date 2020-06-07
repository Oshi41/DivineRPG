package divinerpg.capabilities.gravity;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.eventhandler.GenericEvent;

import javax.annotation.Nonnull;

/**
 * Currently supports Chunk / Entity
 *
 * @param <T>
 */
public class GravityChangedEvent<T extends ICapabilityProvider> extends GenericEvent<T> {
    private final T object;
    private final IGravity cap;

    public GravityChangedEvent(Class<T> type, T object, IGravity cap) {
        super(type);
        this.object = object;
        this.cap = cap;

        if (cap == null)
            setCanceled(true);
    }

    @Override
    public final boolean isCancelable() {
        return true;
    }

    public T getObject() {
        return object;
    }

    @Nonnull
    public IGravity getCap() {
        return cap;
    }
}
