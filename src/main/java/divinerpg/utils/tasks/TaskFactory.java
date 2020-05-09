package divinerpg.utils.tasks;

import com.ibm.icu.impl.Trie2;
import divinerpg.events.server.SwapTask;
import javafx.concurrent.Task;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class TaskFactory<T extends Event> {
    private final Map<UUID, ScheduledTask<T>> playerTasks = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask<T>> pendingTasks = new ConcurrentHashMap<>();
    private Function<T, UUID> findActorFunc;

    protected TaskFactory(Function<T, UUID> findActorFunc) {
        this.findActorFunc = findActorFunc;
    }

    /**
     * Creating new task from player ID and event
     *
     * @param id
     * @param event
     * @return
     */
    protected abstract ITask<T> createTask(UUID id, T event);

    /**
     * Should we proceed current event?
     * Brand new for each player
     *
     * @param event - event
     * @return
     */
    protected abstract boolean shouldProceed(T event);

    /**
     * Gets thread listener from event
     *
     * @param event
     * @return
     */
    protected abstract IThreadListener getListener(T event);

    /**
     * Called after server tick with default delay
     */
    protected void checkPendings() {

    }

    /**
     * Get task delay
     *
     * @return
     */
    protected abstract int getDelay();

    /**
     * Should called on every implementation.
     * Create a method with @SubscribeEvent
     *
     * @param event
     */
    public void listen(T event) {
        if (event == null)
            return;

        // getting actor from event
        UUID id = findActorFunc.apply(event);
        if (id == null)
            return;

        // find task on current player
        ScheduledTask<T> task = playerTasks.get(id);

        if (task != null) {

            // check should we merge work with current event
            if (task.shouldMerge(event)) {
                // merging work and set delay
                task.getTask().merge(event);
            }
        } else {
            if (shouldProceed(event)) {
                ITask<T> newTask = createTask(id, event);

                if (newTask != null) {
                    newTask.merge(event);

                    scheduleTask(getListener(event), newTask);
                }
            }
        }
    }

    /**
     * Schedules user created task
     *
     * @param listener
     * @param task
     */
    protected void scheduleTask(IThreadListener listener, ITask<T> task) {
        if (listener == null)
            return;

        playerTasks.put(task.getActor(), new ScheduledTask<>(
                listener,
                task,
                finished -> playerTasks.remove(finished.getActor()),
                getDelay()));
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END)
            return;

        playerTasks.values().forEach(ScheduledTask::onServerTick);

        if (FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter() % getDelay() == 0) {
            checkPendings();
        }
    }

    /**
     * Returns all not finished tasks
     *
     * @param clazz
     * @param <T1>
     * @return
     */
    protected <T1 extends ITask<T>> List<T1> getPendingTasks(Class<T1> clazz) {
        return playerTasks.values().stream().filter(x -> x.notStarted() && clazz.isInstance(x.getTask()))
                .map(x -> ((T1) x.getTask())).collect(Collectors.toList());
    }

}
