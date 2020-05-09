package divinerpg.events.server;

import com.google.common.cache.LoadingCache;
import divinerpg.objects.blocks.MultiBlockMod;
import divinerpg.objects.blocks.tile.entity.multiblock.IMultiblockTile;
import divinerpg.registry.ModBlocks;
import divinerpg.utils.PositionHelper;
import divinerpg.utils.multiblock.StructureMatch;
import divinerpg.utils.multiblock.StructurePattern;
import divinerpg.utils.tasks.ITask;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Using to destroy built structure.
 */
public class DestroyStructureEventTask implements ITask {
    private final World world;
    private final UUID id;
    private final Set<StructurePattern> structures;
    private final Set<BlockPos> recheckPoses = new HashSet<>();
    private final Set<AxisAlignedBB> recheckAreas = new HashSet<>();


    public DestroyStructureEventTask(World world, Set<StructurePattern> structures, @Nullable BlockPos pos, @Nullable AxisAlignedBB area) {
        this.world = world;
        // need to differ from Build task
        this.id = new UUID(world.provider.getDimension(), 1);
        this.structures = structures;

        if (pos != null) {
            recheckPoses.add(pos);
        }

        if (area != null) {
            recheckAreas.add(area);
        }
    }

    @Override
    public UUID getActor() {
        return id;
    }

    @Override
    public void execute() {
        // optimizing for world search
        LoadingCache<BlockPos, BlockWorldState> cache = BlockPattern.createLoadingCache(world, true);

        Set<BlockPos> recheckingPoses = recheckPoses
                .stream()
                .flatMap(x -> PositionHelper.search(x, ModBlocks.structure_block, new HashSet<>(), cache).stream())
                .filter(x -> true || recheckAreas.stream().noneMatch(area -> area.contains(new Vec3d(x))))
                .collect(Collectors.toSet());

        recheckPoses.clear();

        Set<BlockPos> removingBlocks = new HashSet<>();

        while (!recheckingPoses.isEmpty()) {
            BlockPos recheckingPos = recheckingPoses.stream().findFirst().orElse(null);

            List<BlockPos> nearPoses = recheckingPoses.stream().filter(x -> x.distanceSq(recheckingPos) < 3 * 3).collect(Collectors.toList());
            recheckingPoses.removeAll(nearPoses);

            StructureMatch match = structures
                    .stream()
                    .map(x -> x.checkMultiblock(world, recheckingPos))
                    .filter(x -> x != null)
                    .findFirst()
                    .orElse(null);

            if (match != null) {
                // structure confirmed do not need to check
                recheckingPoses.removeIf(x -> match.area.contains(new Vec3d(x)));
            } else {
                removingBlocks.addAll(nearPoses);
            }

            recheckingPoses.remove(recheckingPos);
        }

        while (!recheckAreas.isEmpty()) {
            AxisAlignedBB area = recheckAreas.stream().findFirst().orElse(null);

            // remove all intersecting with that area
            recheckAreas.removeIf(x -> x.intersects(area));

            IMultiblockTile tile = PositionHelper.findTile(world, area, IMultiblockTile.class);
            if (tile != null) {
                tile.recheckStructure();
                continue;
            }

            removeAll(world, area, cache);

            recheckAreas.remove(area);
        }

        removingBlocks.forEach(x -> removeAll(world, new AxisAlignedBB(x, x), cache));
    }

    public boolean tryMerge(DestroyStructureEventTask task) {
        // different worlds
        if (!Objects.equals(task.getActor(), getActor()))
            return false;

        // adding all poses to current task
        recheckPoses.addAll(task.recheckPoses);
        recheckAreas.addAll(task.recheckAreas);

        return true;
    }

    /**
     * Performing breaking all structure blocks without multistructure
     *
     * @param world - world
     * @param area  - current area
     * @param cache - block search cache
     */
    private void removeAll(World world, AxisAlignedBB area, LoadingCache<BlockPos, BlockWorldState> cache) {
        PositionHelper.forEveryBlock(area, pos -> {
            Block block = cache.getUnchecked(pos).getBlockState().getBlock();

            if (block == ModBlocks.structure_block)
                return true;

            return block instanceof MultiBlockMod;
        }).forEachRemaining(x -> world.destroyBlock(x, true));
    }
}
