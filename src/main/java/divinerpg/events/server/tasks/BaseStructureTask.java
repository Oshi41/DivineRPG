package divinerpg.events.server.tasks;

import divinerpg.events.server.SwapFactory;
import divinerpg.utils.multiblock.StructureMatch;
import divinerpg.utils.multiblock.StructurePattern;
import divinerpg.utils.tasks.ITask;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseStructureTask implements ITask {
    private final UUID id;

    protected final World world;
    protected final Set<StructurePattern> structures;
    protected final Map<StructureMatch, StructurePattern> currentWorldStructures;


    public BaseStructureTask(World world, UUID id, Set<StructurePattern> structures) {
        this.world = world;
        this.id = id;
        this.structures = structures;
        currentWorldStructures = SwapFactory.instance.currentStructures.computeIfAbsent(world.provider.getDimension(), x -> new ConcurrentHashMap<>());
    }

    @Override
    public UUID getActor() {
        return id;
    }

    /**
     * Shared code to add multistructure
     *
     * @param match
     * @param pattern
     */
    protected void addMultiStructure(StructureMatch match, StructurePattern pattern) {
        if (match == null || pattern == null)
            return;

        if (match.isConstructed()) {
            currentWorldStructures.put(match, pattern);

            // request infinite recheck
            SwapFactory.instance.scheduleRecheck(world, pattern, match);
            return;
        }

        // schedules destroy
        SwapFactory.instance.destroy(world, match, null);
    }
}
