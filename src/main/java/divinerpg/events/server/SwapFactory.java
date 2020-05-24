package divinerpg.events.server;

import divinerpg.events.server.tasks.BuildTask;
import divinerpg.events.server.tasks.DestroyTask;
import divinerpg.events.server.tasks.RecheckTask;
import divinerpg.objects.blocks.tile.entity.TileEntityKingCompressor;
import divinerpg.utils.Lazy;
import divinerpg.utils.PositionHelper;
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

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public class SwapFactory extends TaskFactory<EntityStruckByLightningEvent> {
    public final static SwapFactory instance = new SwapFactory();

    /**
     * Lazy request
     */
    private Lazy<Set<StructurePattern>> ref = new Lazy<>(MultiblockDescription.instance::getAll);

    /**
     * List of current structures
     */
    public Map<Integer, Map<StructureMatch, StructurePattern>> currentStructures = new WeakHashMap<>();

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
    protected IThreadListener getListener(EntityStruckByLightningEvent event) {
        return event.getLightning().getServer();
    }

    @Override
    protected int getDelay() {
        return 20;
    }

    @Override
    protected void checkPendings() {
        super.checkPendings();
    }

    @SubscribeEvent
    public void onlisten(EntityStruckByLightningEvent e) {
        super.listen(e);
    }

    public void destroy(World world, @Nullable StructureMatch match, @Nullable BlockPos pos) {
        if (world.isRemote || (match == null && pos == null))
            return;

        DestroyTask destroyTask = findOrCreate(new UUID(world.provider.getDimension(), 1),
                DestroyTask.class,
                x -> scheduleTask(world.getMinecraftServer(), new DestroyTask(world, x)));

        if (match != null)
            destroyTask.merge(match);

        if (pos != null)
            destroyTask.merge(pos);
    }

    public void recheck(World world, @Nullable BlockPos pos, @Nullable StructurePattern pattern, @Nullable StructureMatch match) {
        if (world.isRemote || ((match == null || pattern == null) && pos == null))
            return;

        RecheckTask recheckTask = findOrCreate(new UUID(world.provider.getDimension(), 2),
                RecheckTask.class,
                x -> scheduleTask(world.getMinecraftServer(), new RecheckTask(world, x, ref.getValue()), 1, null));

        if (match != null && pattern != null)
            recheckTask.merge(match, pattern);

        if (pos != null)
            recheckTask.merge(pos);
    }

    /**
     * @param world
     * @param pattern
     * @param recheck
     */
    public void scheduleRecheck(World world, StructurePattern pattern, StructureMatch recheck) {
        if (world.isRemote)
            return;

        // using weak reference here to avoid memory leaking
        // Cause match possibly can be deleted
        WeakReference<StructurePattern> patterRef = new WeakReference<>(pattern);
        WeakReference<StructureMatch> matchRef = new WeakReference<>(recheck);

        // unique from other rechecks
        UUID uuid = new UUID(world.provider.getDimension(), 4);

        RecheckTask structureRecheck = findOrCreate(
                uuid,
                RecheckTask.class,
                x -> scheduleTask(
                        world.getMinecraftServer(),
                        new RecheckTask(world, x, ref.getValue()),
                        60,
                        iTask -> new RecheckTask(iTask, matchRef, patterRef)));

        structureRecheck.merge(recheck, pattern);
    }

    /**
     * Search or register new task
     *
     * @param id              - task ID
     * @param clazz           - class of task
     * @param registerPending - register function
     * @param <T>
     * @return
     */
    private <T extends ITask> T findOrCreate(UUID id, Class<T> clazz, Consumer<UUID> registerPending) {
        // trying to find in pendings
        T pending = findPendingById(id, clazz);
        // success
        if (pending != null)
            return pending;

        // register new task by ID
        registerPending.accept(id);

        // trying to find it
        return findPendingById(id, clazz);
    }


    /**
     * Searches tile on that place
     *
     * @param clazz   - tile entity class
     * @param worldIn - current world
     * @param pos     - structure position
     * @param <T>     - type of tile entity
     * @return
     */
    @Nullable
    public <T> T findMultiblockTile(Class<T> clazz, World worldIn, BlockPos pos) {
        Map<StructureMatch, StructurePattern> map = currentStructures.get(worldIn.provider.getDimension());
        if (map == null)
            return null;

        StructureMatch match = map.keySet().stream().filter(x -> PositionHelper.containsInArea(x.area, pos)).findFirst().orElse(null);
        if (match == null)
            return null;

        return PositionHelper.findTile(worldIn, match.area, clazz);
    }
}
