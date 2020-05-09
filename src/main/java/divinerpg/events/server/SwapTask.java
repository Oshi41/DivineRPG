package divinerpg.events.server;

import divinerpg.objects.blocks.tile.entity.multiblock.IMultiblockTile;
import divinerpg.utils.PositionHelper;
import divinerpg.utils.multiblock.StructureMatch;
import divinerpg.utils.multiblock.StructurePattern;
import divinerpg.utils.tasks.ITask;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SwapTask implements ITask<EntityStruckByLightningEvent> {
    private final UUID id;
    private final World world;
    private Set<StructurePattern> structures;
    private final Map<StructureMatch, StructurePattern> findedStructures = new HashMap<>();

    public SwapTask(World world, Set<StructurePattern> structures) {
        this(world, new UUID(world.provider.getDimension(), 0), structures);
    }

    public SwapTask(World world, UUID id, Set<StructurePattern> structures) {
        this.id = id;
        this.world = world;
        this.structures = structures;
    }

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
        Set<AxisAlignedBB> recheckForTile = new HashSet<>();

        for (Map.Entry<StructureMatch, StructurePattern> entry : findedStructures.entrySet()) {
            StructurePattern pattern = entry.getValue();

            StructureMatch match = entry.getKey();
            boolean wasConstructed = match.isConstructed();
            AxisAlignedBB area = match.area;

            match = pattern.recheck(world, match);

            if (match == null) {
                if (wasConstructed) {
                    recheckForTile.add(area);
                }
            } else {
                match.changeState(world);
            }
        }

        findedStructures.clear();


        for (AxisAlignedBB area : recheckForTile) {
            IMultiblockTile tile = PositionHelper.findTile(world, area, IMultiblockTile.class);
            if (tile == null)
                continue;

            tile.recheckStructure();
        }
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

        // Checking if that position is taken
        for (Map.Entry<StructureMatch, StructurePattern> entry : findedStructures.entrySet()) {
            StructureMatch match = entry.getKey();

            if (match.area.contains(new Vec3d(pos))) {
                return false;
            }
        }

        Map<StructurePattern, StructureMatch> structures = new HashMap<>();

        // recheck pos containing multiblock
        for (StructurePattern structure : this.structures) {
            StructureMatch match = structure.checkStructure(world, pos);
            if (match == null)
                continue;

            structures.put(structure, match);
        }

        if (structures.isEmpty())
            return false;

        AtomicBoolean result = new AtomicBoolean(false);

        structures.forEach((structurePattern, match) -> {
            boolean canAdd = true;

            for (Map.Entry<StructureMatch, StructurePattern> entry : findedStructures.entrySet()) {
                StructureMatch match1 = entry.getKey();

                if (match.area.equals(match1.area) || match.area.intersects(match1.area)) {
                    canAdd = false;
                    break;
                }
            }

            if (canAdd) {
                findedStructures.put(match, structurePattern);
                result.set(true);
            }

        });

        return result.get();
    }
}
