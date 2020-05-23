package divinerpg.events.server.tasks;

import divinerpg.events.server.SwapFactory;
import divinerpg.utils.multiblock.StructureMatch;
import divinerpg.utils.multiblock.StructurePattern;
import divinerpg.utils.tasks.IEventTask;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
                currentWorldStructures.put(recheck, pattern);
            } else {
                SwapFactory.instance.destroy(world, match);
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

        Vec3d pos = new Vec3d(event.getLightning().getPosition());
        if (currentWorldStructures.keySet().stream().anyMatch(x -> x.area.contains(pos))) {
            return false;
        }

        return true;
    }

    public void merge(BlockPos pos) {
        // already put it there
        if (poses.contains(pos))
            return;

        // already activated them
        Vec3d vec3d = new Vec3d(pos);
        if (currentWorldStructures.keySet().stream().anyMatch(x -> x.area.contains(vec3d))) {
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
        if (requestedToBuild.keySet().contains(match))
            return;

        if (requestedToBuild.keySet().stream().anyMatch(x -> x.area.equals(match.area)))
            return;

        requestedToBuild.put(match, structure);
    }
}
