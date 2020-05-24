package divinerpg.events.server.tasks;

import divinerpg.events.server.SwapFactory;
import divinerpg.utils.PositionHelper;
import divinerpg.utils.multiblock.StructureMatch;
import divinerpg.utils.multiblock.StructurePattern;
import divinerpg.utils.tasks.IEventTask;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BuildTask extends BaseStructureTask implements IEventTask<EntityStruckByLightningEvent> {

    private final Set<BlockPos> poses = new ConcurrentSet<>();
    private final Map<StructureMatch, StructurePattern> requestedToBuild = new ConcurrentHashMap<>();

    public BuildTask(World world, Set<StructurePattern> structures) {
        super(world, new UUID(world.provider.getDimension(), 0), structures);
    }

    @Override
    public void execute() {
        while (!requestedToBuild.isEmpty()) {
            StructureMatch match = requestedToBuild.keySet().stream().findFirst().orElse(null);
            StructurePattern pattern = requestedToBuild.get(match);
            requestedToBuild.remove(match);

            StructureMatch recheck = pattern.recheck(world, match);

            if (recheck != null) {
                recheck.buildStructure(world);
                addMultiStructure(recheck, pattern);
            } else {
                SwapFactory.instance.destroy(world, match, null);
            }
        }
    }

    @Override
    public void merge(EntityStruckByLightningEvent event) {
        merge(event.getLightning().getPosition());
    }

    @Override
    public boolean shouldMerge(EntityStruckByLightningEvent event) {
        if (event.getLightning().getEntityWorld() != world)
            return false;

        if (poses.contains(event.getLightning().getPosition()))
            return false;

        StructureMatch existing = currentWorldStructures.keySet().stream()
                .filter(x -> PositionHelper.containsInArea(x.area, event.getLightning().getPosition()))
                .findFirst().orElse(null);

        if (existing != null) {
            SwapFactory.instance.recheck(world, null, currentWorldStructures.get(existing), existing);
            return false;
        }

        return true;
    }

    public void merge(BlockPos pos) {
        // already put it there
        if (poses.contains(pos))
            return;

        // already activated them
        if (currentWorldStructures.keySet().stream().anyMatch(x -> PositionHelper.containsInArea(x.area, pos))) {
            return;
        }

        poses.add(pos);

        for (StructurePattern structure : structures) {
            StructureMatch match = structure.checkStructure(world, pos);
            if (match != null) {
                requestedToBuild.put(match, structure);
                break;
            }
        }
    }

    public void merge(StructureMatch match, StructurePattern structure) {
        if (requestedToBuild.containsKey(match))
            return;

        if (requestedToBuild.keySet().stream().anyMatch(x -> x.area.equals(match.area)))
            return;

        requestedToBuild.put(match, structure);
    }
}
