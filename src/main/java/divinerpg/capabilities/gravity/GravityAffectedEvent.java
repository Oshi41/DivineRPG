package divinerpg.capabilities.gravity;

import divinerpg.utils.GravityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.eventhandler.GenericEvent;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Currently supporting Chunk/Entity
 *
 * @param <T>
 */
public class GravityAffectedEvent<T extends ICapabilityProvider> extends GenericEvent<T> {
    private final Set<Entity> affectedEntities;
    private final T obj;
    private final IGravity cap;

    public GravityAffectedEvent(Class<T> type, T obj, IGravity cap) {
        super(type);
        this.obj = obj;
        this.cap = cap;
        affectedEntities = new HashSet<>();

        if (cap == null)
            setCanceled(true);

        if (getObject() instanceof Chunk)
            forChunk((Chunk) getObject());

        if (getObject() instanceof Entity) {
            forEntity((Entity) getObject());
        }
    }

    @Override
    public final boolean isCancelable() {
        return true;
    }

    public T getObject() {
        return obj;
    }

    /**
     * Gets list of affected entities
     *
     * @return
     */
    public Set<Entity> getAffectedEntities() {
        return affectedEntities;
    }

    @Nonnull
    public IGravity capability() {
        return cap;
    }

    private void forChunk(Chunk chunk) {
        List<Entity> entities = Arrays.stream(chunk.getEntityLists()).flatMap(Collection::parallelStream)
                .filter(x -> GravityUtils.getGravity(x) != 0)
                .collect(Collectors.toList());

        affectedEntities.addAll(entities);
    }

    private void forEntity(Entity e) {
        if (GravityUtils.getGravity(e) != 0) {
            affectedEntities.add(e);
        }
    }
}
