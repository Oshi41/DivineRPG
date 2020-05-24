package divinerpg.events.server.tasks;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import divinerpg.events.server.SwapFactory;
import divinerpg.registry.BlockRegistry;
import divinerpg.utils.PositionHelper;
import divinerpg.utils.multiblock.StructureMatch;
import divinerpg.utils.multiblock.StructurePattern;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RecheckTask extends BaseStructureTask {
    /**
     * Mostly StructureBlock poses
     */
    private final Set<BlockPos> recheckingPoses = new ConcurrentSet<>();

    /**
     * Scheduled structures
     */
    private final Map<StructureMatch, StructurePattern> recheckingStructures = new ConcurrentHashMap<>();

    public RecheckTask(World world, UUID id, Set<StructurePattern> structures) {
        super(world, id, structures);
    }

    public RecheckTask(RecheckTask source, WeakReference<StructureMatch> match, WeakReference<StructurePattern> pattern) {
        super(source.world, source.getActor(), source.structures);

        StructureMatch structureMatch = match.get();
        StructurePattern structurePattern = pattern.get();

        if (structureMatch != null
                && structurePattern != null
                && currentWorldStructures.containsKey(structureMatch)) {
            merge(structureMatch, structurePattern);
        }
    }

    @Override
    public void execute() {
        LoadingCache<BlockPos, BlockWorldState> cache = BlockPattern.createLoadingCache(world, true);

        while (!recheckingPoses.isEmpty()) {
            // find current pos
            BlockPos pos = recheckingPoses.stream().findFirst().orElse(null);
            recheckingPoses.remove(pos);

            // world wasn't loaded
            if (!world.isBlockLoaded(pos))
                continue;

            // find all connected poses
            Set<BlockPos> toCheck = PositionHelper.search(pos, BlockRegistry.structure_block, Sets.newHashSet(), cache);
            recheckingPoses.removeAll(toCheck);

            // part of structure isn't loaded
            if (toCheck.stream().anyMatch(x -> !world.isBlockLoaded(x))) {
                continue;
            }

            // find existing structure
            StructureMatch match = currentWorldStructures
                    .keySet()
                    .stream()
                    .filter(x -> PositionHelper.containsInArea(x.area, pos))
                    .findFirst()
                    .orElse(null);

            if (match != null) {
                // schedule below checks
                recheckingStructures.put(match, currentWorldStructures.get(match));
            } else {
                // there is no structure on that position
                // so just iterate through all possible multiblock descriptions and find
                // first suitable
                match = structures.stream().map(x -> recheckStructure(world, x, pos)).findFirst().orElse(null);
            }

            if (match != null) {
                final StructureMatch finalReference = match;
                // remove all linked blocks
                recheckingPoses.removeIf(x -> PositionHelper.containsInArea(finalReference.area, x));
            }
        }

        while (!recheckingStructures.isEmpty()) {
            StructureMatch match = recheckingStructures.keySet().stream().findFirst().orElse(null);
            StructurePattern pattern = recheckingStructures.get(match);
            recheckingStructures.remove(match);

            // structure isn't in a loaded chunks
            if (!world.isAreaLoaded(new StructureBoundingBox((int) match.area.minX, (int) match.area.minY, (int) match.area.minZ, (int) match.area.maxX, (int) match.area.maxY, (int) match.area.maxZ))) {
                // removing it from main list
                currentWorldStructures.remove(match);

                continue;
            }

            // recheck structure there
            StructureMatch recheck = recheckStructure(world, pattern, match.getCorner());
            if (recheck != null) {
                // remove all linked blocks
                recheckingPoses.removeIf(x -> PositionHelper.containsInArea(recheck.area, x));
            }
        }
    }

    public void merge(BlockPos pos) {
        if (recheckingPoses.contains(pos))
            return;

        if (recheckingStructures.keySet().stream().anyMatch(x -> PositionHelper.containsInArea(x.area, pos)))
            return;

        Set<BlockPos> toCheck = PositionHelper.search(pos, BlockRegistry.structure_block, Sets.newHashSet(), BlockPattern.createLoadingCache(world, true));
        recheckingPoses.addAll(toCheck);
    }

    public void merge(StructureMatch match, StructurePattern structure) {
        if (recheckingStructures.containsKey(match))
            return;

        if (recheckingStructures.keySet().stream().anyMatch(x -> x.area.equals(match.area)))
            return;

        recheckingStructures.put(match, structure);
    }

    /**
     * Rechecks current structure by that pos
     *
     * @param world   - current world
     * @param pattern - structure pattern
     * @param pos     - one of the structure block pos
     */
    @Nullable
    private StructureMatch recheckStructure(World world, StructurePattern pattern, BlockPos pos) {
        StructureMatch recheck = pattern.checkMultiblock(world, pos);

        // search for already existing structure
        StructureMatch structure = currentWorldStructures.keySet().stream().filter(x -> PositionHelper.containsInArea(x.area, pos))
                .findFirst().orElse(null);

        // structure is correct still
        if (recheck != null && recheck.equals(structure)) {
            return structure;
        }

        // need to delete structure
        if (recheck == null) {
            SwapFactory.instance.destroy(world, structure, null);
            return structure;
        } else {
            // it's correct
            addMultiStructure(recheck, pattern);
            return recheck;
        }
    }
}
