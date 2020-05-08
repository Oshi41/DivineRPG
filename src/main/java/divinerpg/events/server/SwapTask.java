package divinerpg.events.server;

import divinerpg.objects.blocks.tile.entity.multiblock.IMultiStructure;
import divinerpg.objects.blocks.tile.entity.multiblock.IMultiblockTile;
import divinerpg.utils.IStructure;
import divinerpg.utils.PositionHelper;
import divinerpg.utils.tasks.ITask;
import net.minecraft.block.Block;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SwapTask implements ITask<EntityStruckByLightningEvent> {
    private final UUID id;
    private final World world;
    private Map<IMultiStructure, Block> structureBlockMap;
    private final Map<AxisAlignedBB, IMultiStructure> possiblePoses = new ConcurrentHashMap<>();

    public SwapTask(World world, UUID id, Map<IMultiStructure, Block> structureBlockMap) {
        this.id = id;
        this.world = world;
        this.structureBlockMap = structureBlockMap;
    }

    @Override
    public UUID getActor() {
        return id;
    }

    @Override
    public void merge(EntityStruckByLightningEvent event) {
        merge(event.getLightning().getPosition());
    }

    @Override
    public boolean shouldMerge(EntityStruckByLightningEvent event) {
        Vec3d pos = new Vec3d(event.getLightning().getPosition());

        if (!possiblePoses.isEmpty()
                && world.provider.getDimension() == event.getLightning().getEntityWorld().provider.getDimension()
                && possiblePoses.keySet().stream().anyMatch(x -> x.contains(pos)))
            return true;

        return false;
    }

    @Override
    public void execute() {
        Iterator<Map.Entry<AxisAlignedBB, IMultiStructure>> iterator = possiblePoses.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<AxisAlignedBB, IMultiStructure> entry = iterator.next();

            IMultiStructure structure = entry.getValue();
            AxisAlignedBB area = entry.getKey();

            // scanning world for structure (top left corner)
            BlockPos pos = new BlockPos(area.maxX, area.maxY, area.maxZ);
            BlockPattern.PatternHelper match = structure.isMatch(world, pos);
            if (match == null) {

                // scanning world for structure (botton right corner)
                pos = new BlockPos(area.minX, area.minY, area.minZ);
                match = structure.isMatch(world, pos);
            }

            if (match == null)
                continue;

            area = PositionHelper.getArea(match);
            Iterator<BlockPos> posesIterator = BlockPos.getAllInBox(new BlockPos(area.maxX, area.maxY, area.maxZ), new BlockPos(area.minX, area.minY, area.minZ)).iterator();

            IMultiblockTile tile = null;

            // searching for tile that is already present
            while (posesIterator.hasNext()) {
                TileEntity tileEntity = world.getTileEntity(posesIterator.next());
                if (tileEntity instanceof IMultiblockTile) {
                    tile = ((IMultiblockTile) tileEntity);
                    break;
                }
            }

            // recheck structure or create new
            if (tile == null) {
                structure.constructStructure(world, match);
            } else {
                tile.recheckStructure();
            }
        }

        possiblePoses.clear();
    }

    private void merge(BlockPos pos) {
        BlockPattern.PatternHelper match = null;
        IMultiStructure structure = null;

        Iterator<Map.Entry<IMultiStructure, Block>> iterator = structureBlockMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<IMultiStructure, Block> entry = iterator.next();

            match = entry.getKey().isMatch(world, pos);
            if (match != null) {
                structure = entry.getKey();
                break;
            }
        }

        if (match == null || structure == null)
            return;

        AxisAlignedBB area = PositionHelper.getArea(match);

        if (!possiblePoses.isEmpty() && possiblePoses.keySet().stream().anyMatch(x -> x.intersects(area)))
            return;

        possiblePoses.put(area, structure);
    }
}
