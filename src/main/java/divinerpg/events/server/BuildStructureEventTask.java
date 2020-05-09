package divinerpg.events.server;

import divinerpg.utils.multiblock.StructureMatch;
import divinerpg.utils.multiblock.StructurePattern;
import divinerpg.utils.tasks.IEventTask;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Using to swap ready structure to multiblock
 */
public class BuildStructureEventTask implements IEventTask<EntityStruckByLightningEvent> {
    private final UUID id;
    private final World world;
    private final Set<StructurePattern> structures;
    private final Map<StructureMatch, StructurePattern> findedStructures = new HashMap<>();

    public BuildStructureEventTask(World world, UUID id, Set<StructurePattern> structures) {
        this.id = id;
        this.world = world;
        this.structures = structures;
    }

    /**
     * Uniques ID for one world
     *
     * @return
     */
    @Override
    public UUID getActor() {
        return id;
    }

    @Override
    public void merge(EntityStruckByLightningEvent event) {
        tryMerge(event.getLightning().getEntityWorld(), event.getLightning().getPosition());
    }

    @Override
    public boolean shouldMerge(EntityStruckByLightningEvent event) {
        Vec3d pos = new Vec3d(event.getLightning().getPosition());

        if (!findedStructures.isEmpty()
                && world.provider.getDimension() == event.getLightning().getEntityWorld().provider.getDimension()
                && findedStructures.keySet().stream().anyMatch(x -> x.area.contains(pos)))
            return true;

        return false;
    }

    @Override
    public void execute() {
        // list  for possible boken structures
        Set<AxisAlignedBB> searchArea = new HashSet<>();

        // iterate by all structures we eant to build
        for (Map.Entry<StructureMatch, StructurePattern> entry : findedStructures.entrySet()) {
            StructurePattern pattern = entry.getValue();

            StructureMatch match = entry.getKey();
            boolean wasConstructed = match.isConstructed();
            AxisAlignedBB area = match.area;

            // rechecking if we can build structure still
            match = pattern.recheck(world, match);

            if (match == null) {
                if (wasConstructed) {
                    // looks like structure is broken, need to recheck
                    searchArea.add(area);
                }
            } else {
                // building structure - changing it's state to 'constructed'
                match.changeState(world);
            }
        }

        findedStructures.clear();

        // for any broken areas we need to schedule destruct task
        searchArea.forEach(x -> SwapFactory.instance.requestCheck(world, null, x));
    }

    /**
     * Try to merge current pos in world
     *
     * @param pos
     * @return
     */
    public boolean tryMerge(World world, BlockPos pos) {
        if (world.provider.getDimension() != this.world.provider.getDimension())
            return false;

        // If any of scheduled structures contains that position
        for (Map.Entry<StructureMatch, StructurePattern> entry : findedStructures.entrySet()) {
            StructureMatch match = entry.getKey();

            // some structure will build on that pos, we already merged this
            if (match.area.contains(new Vec3d(pos))) {
                return false;
            }
        }

        Map<StructurePattern, StructureMatch> structures = new HashMap<>();

        // loop through all possible structures
        for (StructurePattern structure : this.structures) {
            StructureMatch match = structure.checkStructure(world, pos);
            if (match == null)
                continue;

            // can build structure
            structures.put(structure, match);
        }

        if (structures.isEmpty())
            return false;

        for (Map.Entry<StructurePattern, StructureMatch> entry : structures.entrySet()) {
            boolean canAdd = true;
            StructureMatch match = entry.getValue();

            // iterating throuh all scheduled structure replacing
            for (Map.Entry<StructureMatch, StructurePattern> entry1 : findedStructures.entrySet()) {
                StructureMatch match1 = entry1.getKey();

                // if possible structure overlaps with already scheduled, returning
                if (match.area.equals(match1.area) || match.area.intersects(match1.area)) {
                    canAdd = false;
                    break;
                }
            }

            if (canAdd) {
                findedStructures.put(match, entry.getKey());
                return true;
            }
        }

        return false;
    }


}
