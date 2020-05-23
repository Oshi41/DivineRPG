package divinerpg.events.server.tasks;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import divinerpg.events.server.SwapFactory;
import divinerpg.registry.ModBlocks;
import divinerpg.utils.PositionHelper;
import divinerpg.utils.multiblock.StructureMatch;
import divinerpg.utils.multiblock.StructurePattern;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.text.StrMatcher;

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

    @Override
    public void execute() {
        LoadingCache<BlockPos, BlockWorldState> cache = BlockPattern.createLoadingCache(world, true);

        while (!recheckingStructures.isEmpty()) {
            StructureMatch match = recheckingStructures.keySet().stream().findFirst().orElse(null);
            StructurePattern pattern = recheckingStructures.get(match);
            recheckingStructures.remove(match);

            StructureMatch recheck = pattern.recheck(world, match);

            if (recheck == null) {
                SwapFactory.instance.destroy(world, match);
                recheckingPoses.removeIf(x -> match.area.contains(new Vec3d(x)));
            } else {
                SwapFactory.instance.build(world, pattern, match);
                recheckingPoses.removeIf(x -> recheck.area.contains(new Vec3d(x)));
            }
        }

        while (!recheckingPoses.isEmpty()) {
            // find current pos
            BlockPos pos = recheckingPoses.stream().findFirst().orElse(null);
            recheckingPoses.remove(pos);

            // find all connected poses
            Set<BlockPos> toCheck = PositionHelper.search(pos, ModBlocks.structure_block, Sets.newHashSet(), cache);
            recheckingPoses.removeAll(toCheck);

            boolean findActiveStructure = false;

            // find structure with that pos
            for (Map.Entry<StructureMatch, StructurePattern> entry : currentWorldStructures.entrySet()) {
                StructureMatch match = entry.getKey();
                // one of the connected pos - current structure
                if (toCheck.stream().map(Vec3d::new).anyMatch(match.area::contains)) {

                    // recheck it's status
                    StructureMatch recheck = entry.getValue().recheck(world, match);

                    if (recheck == null) {
                        SwapFactory.instance.destroy(world, match);
                        recheckingPoses.removeIf(x -> match.area.contains(new Vec3d(x)));
                    } else {
                        SwapFactory.instance.build(world, entry.getValue(), match);
                        recheckingPoses.removeIf(x -> recheck.area.contains(new Vec3d(x)));
                    }

                    findActiveStructure = true;
                    break;
                }
            }

            if (!findActiveStructure) {
                for (StructurePattern pattern : structures) {
                    // checking multiblock structure
                    StructureMatch match = pattern.checkMultiblock(world, pos);

                    if (match != null) {
                        // find result, schedule rechecking
                        SwapFactory.instance.build(world, pattern, match);

                        // find any structure and leave
                        findActiveStructure = true;
                        break;
                    }
                }
            }

            // didn't find any structures
            if (!findActiveStructure) {
                SwapFactory.instance.destroy(world, pos);
            }
        }
    }

    public void merge(BlockPos pos) {
        if (recheckingPoses.contains(pos))
            return;

        Vec3d vec3d = new Vec3d(pos);
        if (recheckingStructures.keySet().stream().anyMatch(x -> x.area.contains(vec3d)))
            return;

        Set<BlockPos> toCheck = PositionHelper.search(pos, ModBlocks.structure_block, Sets.newHashSet(), BlockPattern.createLoadingCache(world, true));
        recheckingPoses.addAll(toCheck);
    }

    public void merge(StructureMatch match, StructurePattern structure) {
        if (recheckingStructures.keySet().contains(match))
            return;

        if (recheckingStructures.keySet().stream().anyMatch(x -> x.area.equals(match.area)))
            return;

        recheckingStructures.put(match, structure);
    }
}
