package divinerpg.events.server;

import divinerpg.utils.Lazy;
import divinerpg.utils.multiblock.MultiblockDescription;
import divinerpg.utils.multiblock.StructurePattern;
import divinerpg.utils.tasks.ITask;
import divinerpg.utils.tasks.ScheduledTask;
import divinerpg.utils.tasks.TaskFactory;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.block.Block;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SwapFactory extends TaskFactory<EntityStruckByLightningEvent> {
    public final static SwapFactory instance = new SwapFactory();

    /**
     * Lazy request
     */
    private Lazy<Set<StructurePattern>> ref = new Lazy<>(MultiblockDescription.instance::getAll);

    private Map<World, Set<BlockPos>> requested = new ConcurrentHashMap<>();
    private Map<UUID, SwapTask> requestedTasks = new ConcurrentHashMap<>();

    protected SwapFactory() {
        super(x -> new UUID(x.getLightning().getEntityWorld().provider.getDimension(), 0));
    }

    @Override
    protected ITask<EntityStruckByLightningEvent> createTask(UUID id, EntityStruckByLightningEvent event) {
        return new SwapTask(event.getLightning().getEntityWorld(), id, ref.getValue());
    }

    @Override
    protected boolean shouldProceed(EntityStruckByLightningEvent event) {
        return true;
    }

    @Override
    protected void checkPendings() {

        requestedTasks.forEach((uuid, swapTask) -> {
            requested.forEach((world, blockPoses) -> {
                blockPoses.forEach(pos -> swapTask.tryMerge(world, pos));
            });
        });

        requestedTasks.forEach((uuid, swapTask) -> scheduleTask(FMLCommonHandler.instance().getMinecraftServerInstance(), swapTask));
        requested.clear();
        requestedTasks.clear();
    }

    @Override
    protected IThreadListener getListener(EntityStruckByLightningEvent event) {
        return event.getLightning().getServer();
    }

    @Override
    protected int getDelay() {
        return 20;
    }

    @SubscribeEvent
    public void onlisten(EntityStruckByLightningEvent e) {
        super.listen(e);
    }

    /**
     * Request all checks
     *
     * @param world
     * @param pos
     */
    public void requestCheck(World world, BlockPos pos) {
        if (world.isRemote || world.getMinecraftServer() == null)
            return;

        Set<BlockPos> poses = requested.computeIfAbsent(world, w -> new ConcurrentSet<>());
        poses.add(pos);

        SwapTask task = new SwapTask(world, ref.getValue());
        requestedTasks.put(task.getActor(), task);
    }
}
