package divinerpg.events.server;

import divinerpg.utils.Lazy;
import divinerpg.utils.multiblock.MultiblockDescription;
import divinerpg.utils.multiblock.StructurePattern;
import divinerpg.utils.tasks.IEventTask;
import divinerpg.utils.tasks.TaskFactory;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SwapFactory extends TaskFactory<EntityStruckByLightningEvent> {
    public final static SwapFactory instance = new SwapFactory();

    /**
     * Lazy request
     */
    private Lazy<Set<StructurePattern>> ref = new Lazy<>(MultiblockDescription.instance::getAll);
    private Map<UUID, DestroyStructureEventTask> requestedTasks = new ConcurrentHashMap<>();

    protected SwapFactory() {
        super(x -> new UUID(x.getLightning().getEntityWorld().provider.getDimension(), 0));
    }

    @Override
    protected IEventTask<EntityStruckByLightningEvent> createTask(UUID id, EntityStruckByLightningEvent event) {
        return new BuildStructureEventTask(event.getLightning().getEntityWorld(), id, ref.getValue());
    }

    @Override
    protected boolean shouldProceed(EntityStruckByLightningEvent event) {
        return true;
    }

    @Override
    protected void checkPendings() {
        requestedTasks.forEach((uuid, destroyStructureTask) -> destroyStructureTask.execute());
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
    public void requestCheck(World world, @Nullable BlockPos pos, @Nullable AxisAlignedBB area) {
        if (world.isRemote || world.getMinecraftServer() == null)
            return;

        DestroyStructureEventTask task = new DestroyStructureEventTask(world, ref.getValue(), pos, area);

        if (requestedTasks.values().stream().noneMatch(x -> x.tryMerge(task))) {
            requestedTasks.put(task.getActor(), task);
        }
    }
}
