package divinerpg.events.server;

import divinerpg.events.server.tasks.BuildTask;
import divinerpg.events.server.tasks.DestroyTask;
import divinerpg.events.server.tasks.RecheckTask;
import divinerpg.utils.Lazy;
import divinerpg.utils.multiblock.MultiblockDescription;
import divinerpg.utils.multiblock.StructureMatch;
import divinerpg.utils.multiblock.StructurePattern;
import divinerpg.utils.tasks.IEventTask;
import divinerpg.utils.tasks.ITask;
import divinerpg.utils.tasks.TaskFactory;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SwapFactory extends TaskFactory<EntityStruckByLightningEvent> {
    public final static SwapFactory instance = new SwapFactory();

    /**
     * Lazy request
     */
    private Lazy<Set<StructurePattern>> ref = new Lazy<>(MultiblockDescription.instance::getAll);
    private Map<UUID, ITask> requestedTasks = new ConcurrentHashMap<>();

    /**
     * List of current structures
     */
    public Map<Integer, Map<StructureMatch, StructurePattern>> currentStructures = new ConcurrentHashMap<>();

    protected SwapFactory() {
        super(x -> new UUID(x.getLightning().getEntityWorld().provider.getDimension(), 0));
    }

    @Override
    protected IEventTask<EntityStruckByLightningEvent> createTask(UUID id, EntityStruckByLightningEvent event) {
        return new BuildTask(event.getLightning().getEntityWorld(), ref.getValue());
    }

    @Override
    protected boolean shouldProceed(EntityStruckByLightningEvent event) {
        return true;
    }

    @Override
    protected void checkPendings() {
        if (requestedTasks.isEmpty())
            return;

        List<ITask> queue = requestedTasks.values().stream().filter(x -> x instanceof RecheckTask)
                .collect(Collectors.toList());

        while (!queue.isEmpty()) {
            ITask task = queue.get(0);
            requestedTasks.remove(task.getActor());
            queue.remove(0);
            task.execute();
        }

        queue = requestedTasks.values().stream().filter(x -> x instanceof DestroyTask)
                .collect(Collectors.toList());

        while (!queue.isEmpty()) {
            ITask task = queue.get(0);
            requestedTasks.remove(task.getActor());
            queue.remove(0);
            task.execute();
        }

        while (!requestedTasks.isEmpty()) {
            ITask task = requestedTasks.values().stream().findFirst().orElse(null);
            task.execute();
            requestedTasks.remove(task.getActor());
        }
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

    public void destroy(World world, BlockPos pos) {
        if (world.isRemote)
            return;

        DestroyTask destroyTask = findOrCreate(new UUID(world.provider.getDimension(), 1), DestroyTask.class, x -> new DestroyTask(world, x));
        destroyTask.merge(pos);
    }

    public void destroy(World world, StructureMatch match) {
        if (world.isRemote)
            return;

        DestroyTask destroyTask = findOrCreate(new UUID(world.provider.getDimension(), 1), DestroyTask.class, x -> new DestroyTask(world, x));
        destroyTask.merge(match);
    }

    public void build(World world, StructurePattern pattern, StructureMatch match) {
        if (world.isRemote)
            return;

        UUID id = new UUID(world.provider.getDimension(), 0);
        BuildTask buildTask = getPendingTasks(BuildTask.class)
                .stream()
                .filter(x -> x.getActor().equals(id))
                .findFirst()
                .orElse(null);

        if (buildTask == null) {
            buildTask = new BuildTask(world, ref.getValue());
            scheduleTask(world.getMinecraftServer(), buildTask);
        }

        buildTask.merge(match, pattern);
    }

    public void recheck(World world, BlockPos pos) {
        if (world.isRemote)
            return;

        RecheckTask recheckTask = findOrCreate(new UUID(world.provider.getDimension(), 2), RecheckTask.class, x -> new RecheckTask(world, x, ref.getValue()));
        recheckTask.merge(pos);
    }

    private <T extends ITask> T findOrCreate(UUID id, Class<T> clazz, Function<UUID, T> newInstance) {
        return (T) requestedTasks.computeIfAbsent(id, newInstance::apply);
    }

}
