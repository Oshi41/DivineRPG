package divinerpg.events.server.tasks;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import divinerpg.objects.blocks.structure.StructureBlock;
import divinerpg.registry.BlockRegistry;
import divinerpg.utils.PositionHelper;
import divinerpg.utils.multiblock.StructureMatch;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;
import java.util.UUID;

public class DestroyTask extends BaseStructureTask {
    /**
     * List of structures we want to break
     */
    private final Set<StructureMatch> toDestroy = new ConcurrentSet<>();

    /**
     * Structure block poses storing here
     */
    private final Set<BlockPos> requestedPos = new ConcurrentSet<>();

    public DestroyTask(World world, UUID id) {
        super(world, id, null);
    }

    @Override
    public void execute() {
        LoadingCache<BlockPos, BlockWorldState> cache = BlockPattern.createLoadingCache(world, true);

        while (!requestedPos.isEmpty()) {
            BlockPos pos = requestedPos.stream().findFirst().orElse(null);
            requestedPos.remove(pos);

            Set<BlockPos> connected = PositionHelper.search(pos, BlockRegistry.structure_block, Sets.newHashSet(), cache);
            requestedPos.removeAll(connected);

            // find any match in existing structures
            StructureMatch existing = currentWorldStructures
                    .keySet()
                    .stream()
                    .filter(x -> PositionHelper.containsInArea(x.area, requestedPos.toArray(new BlockPos[0])))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                toDestroy.add(existing);
            } else {
                // just replacing old blocks
                connected.stream().filter(x -> havePlaceholder(cache.getUnchecked(x))).forEach(x -> swap(world, cache.getUnchecked(x)));
            }
        }


        while (!toDestroy.isEmpty()) {
            StructureMatch match = toDestroy.stream().findFirst().orElse(null);
            toDestroy.remove(match);

            // if was constructed - force to break
            if (match.isConstructed()) {
                match.destroy(world);
            } else {
                // or just replace all blocks in area
                PositionHelper.forEveryBlock(match.area, pos -> havePlaceholder(cache.getUnchecked(pos)))
                        .forEachRemaining(x -> swap(world, cache.getUnchecked(x)));
            }

            currentWorldStructures.remove(match);

            // removing all blocks in that area
            requestedPos.removeIf(pos -> PositionHelper.containsInArea(match.area, pos));
        }
    }

    public void merge(StructureMatch match) {
        if (toDestroy.contains(match))
            return;

        if (toDestroy.stream().anyMatch(x -> x.area.equals(match.area)))
            return;

        toDestroy.add(match);
    }

    public void merge(BlockPos pos) {
        if (requestedPos.contains(pos))
            return;

        if (toDestroy.stream().anyMatch(x -> PositionHelper.containsInArea(x.area, pos)))
            return;

        requestedPos.add(pos);
    }

    private boolean havePlaceholder(BlockWorldState state) {
        return state.getBlockState().getPropertyKeys().contains(StructureBlock.PlaceholderProperty);
    }

    private void swap(World world, BlockWorldState state) {
        world.setBlockState(state.getPos(), state.getBlockState().getValue(StructureBlock.PlaceholderProperty).getBlock().getDefaultState());
    }
}
